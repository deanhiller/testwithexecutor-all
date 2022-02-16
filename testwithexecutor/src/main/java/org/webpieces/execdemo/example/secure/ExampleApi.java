package org.webpieces.execdemo.example.secure;

import org.webpieces.util.futures.XFuture;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

public interface ExampleApi {

    @Path("/fetch/self")
    @GET
    public XFuture<FetchMyInfoResponse> fetchMyInfo(FetchMyInfoRequest request);

}
