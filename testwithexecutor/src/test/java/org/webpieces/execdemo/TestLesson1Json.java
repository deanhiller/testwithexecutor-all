package org.webpieces.execdemo;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.search.RequiredSearch;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.util.futures.XFuture;
import org.webpieces.execdemo.framework.FeatureTest;
import org.webpieces.execdemo.framework.Requests;
import org.webpieces.execdemo.json.*;
import org.webpieces.execdemo.service.SendDataRequest;
import org.webpieces.execdemo.service.SendDataResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * These are working examples of tests that sometimes are better done with the BasicSeleniumTest example but are here for completeness
 * so you can test the way you would like to test
 * 
 * @author dhiller
 *
 */
public class TestLesson1Json extends FeatureTest {

	private final static Logger log = LoggerFactory.getLogger(TestLesson1Json.class);

	/**
	 * Testing a synchronous controller may be easier especially if there is no remote communication.
	 */
	@Test
	public void testSynchronousController() throws ExecutionException, InterruptedException, TimeoutException {
		//move complex request building out of the test...
		SearchRequest req = Requests.createSearchRequest();

		//calling this will see that we schedule a runnable
		SearchResponse resp = saveApi.search(req).get(5, TimeUnit.SECONDS);

		Runnable cachedRunnable = mockExecutor.getCache().get(0);
		//notice we now move the setup of the mockRemoteService to just before the Runnable is run
		mockRemoteService.setSendDefaultRetValue(XFuture.completedFuture(new SendDataResponse()));

		//let us simulate the thread pool but on the test thread
		cachedRunnable.run();

		validate(resp);
	}

	@Test
	public void testPathParams() throws ExecutionException, InterruptedException, TimeoutException {
		String id = "asdf";
		int number = 567;
		MethodResponse methodResponse = exampleRestAPI.method(id, number).get(5, TimeUnit.SECONDS);

		Assert.assertEquals(id, methodResponse.getId());
		Assert.assertEquals(number, methodResponse.getNumber());
	}

	@Test
	public void testPathParamsPost() throws ExecutionException, InterruptedException, TimeoutException {
		String id = "asdf1";
		int number = 5671;
		String something = "qwerasdfqewr";
		PostTestResponse methodResponse = exampleRestAPI.postTest(id, number, new PostTestRequest(something)).get(5, TimeUnit.SECONDS);

		Assert.assertEquals(id, methodResponse.getId());
		Assert.assertEquals(number, methodResponse.getNumber());
		Assert.assertEquals(something, methodResponse.getSomething());
	}

	private void validate(SearchResponse resp) {
		//next if you want, move assert logic into a validate method to re-use amongst tests
		Assert.assertEquals("match1", resp.getMatches().get(0));

		//deleted since we do not call search twice(that was testing metrics reporting)

		//check the mock system was called with 6
		List<SendDataRequest> params = mockRemoteService.getSendMethodParameters();
		Assert.assertEquals(2, params.size());
		Assert.assertEquals(6, params.get(0).getNum());
	}

}
