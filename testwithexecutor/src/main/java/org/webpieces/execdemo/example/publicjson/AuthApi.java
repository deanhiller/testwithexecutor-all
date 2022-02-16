package org.webpieces.execdemo.example.publicjson;

import org.webpieces.util.futures.XFuture;

public interface AuthApi {

    public XFuture<AuthenticateResponse> authenticate(AuthenticateRequest request);
}
