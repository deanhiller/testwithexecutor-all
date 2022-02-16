package org.webpieces.execdemo.json;

import org.webpieces.util.futures.XFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.plugin.json.Jackson;
import org.webpieces.router.api.RouterStreamHandle;
import org.webpieces.http.exception.NotFoundException;

import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.webpieces.execdemo.service.RemoteService;
import org.webpieces.execdemo.service.SendDataRequest;

@Singleton
public class JsonController implements SaveApi, ClientApi {
	
	private static final Logger log = LoggerFactory.getLogger(JsonController.class);

	private Counter counter;
	private Executor executor;
	private Provider<CallRemoteService> runnableProvider;

	@Inject
	public JsonController(MeterRegistry metrics,
						  Executor executor, Provider<CallRemoteService> runnableProvider) {
		counter = metrics.counter("testCounter");
		this.executor = executor;
		this.runnableProvider = runnableProvider;
	}
	
	public XFuture<SearchResponse> asyncJsonRequest(int id, @Jackson SearchRequest request) {
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(8);
		resp.getMatches().add("match1");
		resp.getMatches().add("match2");
		
		return XFuture.completedFuture(resp);
	}
	
	public SearchResponse jsonRequest(int id, @Jackson SearchRequest request) {
		counter.increment();
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(5);
		resp.getMatches().add("match1");
		resp.getMatches().add("match2");
		
		return resp;
	}
	
	@Override
	public SearchResponse postJson(@Jackson SearchRequest request) {
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(99);
		resp.getMatches().add("match1");
		resp.getMatches().add("match2");
		
		return resp;
	}
	
	public XFuture<SearchResponse> postAsyncJson(int id, @Jackson SearchRequest request) {
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(98);
		resp.getMatches().add("match1");
		resp.getMatches().add("match2");
		
		return XFuture.completedFuture(resp);
	}
	
	@Jackson
	public SearchResponse readOnly() {
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(1);
		return resp;
	}
	
	public SearchResponse throwNotFound(int id, @Jackson SearchRequest request) {
		throw new NotFoundException("to test it out");
	}

	//Method signature cannot have RequestContext since in microservices, we implement an api as the server
	//AND a client implements the same api AND client does not have a RequestContext!!
	@Override
	public StreamRef myStream(ResponseStreamHandle handle2) {
		RouterStreamHandle handle  = (RouterStreamHandle) handle2;
		RequestContext requestCtx = Current.getContext(); 
		
		Http2Response response = handle.createBaseResponse(requestCtx.getRequest().originalRequest, "text/plain", 200, "Ok");
		response.setEndOfStream(false);
		
		XFuture<StreamWriter> responseWriter = handle.process(response);
		return new RequestStreamEchoWriter(requestCtx, handle, responseWriter);
	}

	@Override
	public XFuture<JsonAsyncResponse> jsonAsync(@Jackson JsonAsyncRequest request) {
		JsonAsyncResponse resp = new JsonAsyncResponse();
		resp.setSomething("prefix="+request.getQuery());
		log.info("Sleeping for 5 seconds");
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//			throw new RuntimeException("failed", e);
//		}
		if(request.getQuery().contains("request2"))
			throw new RuntimeException("fail please");

		log.info("done sleeping");
		return XFuture.completedFuture(resp);
	}

	@Override
	public XFuture<SearchResponse> search(@Jackson SearchRequest request) {
		counter.increment();

		CallRemoteService runnable = runnableProvider.get();
		runnable.setData(request);
		executor.execute(runnable);

		//we no longer call the remote service here as it takes too long and holds up clients
		//so we throw it into a pool of threads instead so we don't have to wait.

		SearchResponse resp = postJson(request);
		return XFuture.completedFuture(resp);
	}

	private static class CallRemoteService implements Runnable {

		private RemoteService remoteService;

		//Sving request state in business classes is generally a no-no but Runnables
		//are a bridge sometimes so in Runnables it is required
		private SearchRequest request;

		@Inject
		public CallRemoteService(RemoteService remoteService) {
			this.remoteService = remoteService;
		}

		public void setData(SearchRequest request) {
			this.request = request;
		}

		@Override
		public void run() {
			//Now we call remoteService on a different thread in production here
			//however if you step through the test, it is on the test thread!!
			remoteService.sendData(new SendDataRequest(6)).join();
		}
	}

	private static class RequestStreamEchoWriter implements StreamWriter, StreamRef {

		private AtomicInteger total = new AtomicInteger();
		private XFuture<StreamWriter> responseWriter;
		private RouterStreamHandle handle; // in case you want to cancel the request

		public RequestStreamEchoWriter(RequestContext requestCtx, RouterStreamHandle handle,
				XFuture<StreamWriter> responseWriter2) {
			this.responseWriter = responseWriter2;
			this.handle = handle;
		}

		@Override
		public XFuture<Void> processPiece(StreamMsg data) {
			RequestContext requestCtx = Current.getContext(); 

			DataFrame f = (DataFrame) data;
			int numReceived = total.addAndGet(f.getData().getReadableSize());
			log.info("Num bytes received so far="+numReceived+" for requests="+requestCtx.getRequest().relativePath);
			
			if(data.isEndOfStream()) {
				log.info("Upload complete");
				return responseWriter.thenCompose(w -> w.processPiece(data));
			}

			//We just echo data back to whatever the client sent as the client sends it...
			return responseWriter.thenCompose(w -> w.processPiece(data));
		}

		@Override
		public XFuture<StreamWriter> getWriter() {
			//let's make it wait for our response to be written by 
			//chaining with responseWriter future here
			return responseWriter.thenApply(s -> this);
		}

		@Override
		public XFuture<Void> cancel(CancelReason reason) {
			//here if using http client, we may forward to next stream like so
			//responseStream.cancel(reason);
			//but since the responseStream and request is the same, we can just stop sending instead
			//which happens automatically since they stopped sending(ie. nothing to do here
			return XFuture.completedFuture(null);
		}

	}
}
