package org.webpieces.execdemo.example.secure;

import org.webpieces.ctx.api.Current;
import org.webpieces.execdemo.example.publicjson.AuthController;
import org.webpieces.plugin.json.Jackson;
import org.webpieces.util.futures.XFuture;

public class ExampleJsonController implements ExampleApi {
    @Override
    public XFuture<FetchMyInfoResponse> fetchMyInfo(@Jackson FetchMyInfoRequest request) {
        String userId = (String) Current.request().requestState.get(AuthController.USER_ID_KEY);

        //lookup anything based on userId and read from database here

        FetchMyInfoResponse response = new FetchMyInfoResponse();
        response.setFirstName("Dean");
        response.setLastName("Coolio");
        response.setCoolRating(-5);

        return XFuture.completedFuture(response);
    }
}
