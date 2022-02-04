package org.webpieces.execdemo.framework;

import org.webpieces.execdemo.json.SearchRequest;

public class Requests {
    public static SearchRequest createSearchRequest() {
        SearchRequest req = new SearchRequest();
        req.setQuery("my query");
        return req;
    }
}
