package org.webpieces.execdemo.example.publicjson;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import org.webpieces.plugin.json.Jackson;
import org.webpieces.util.exceptions.SneakyThrow;
import org.webpieces.util.futures.XFuture;

public class AuthController implements AuthApi {
    //HMAC
    public static final Algorithm ALGORITHM = Algorithm.HMAC256("some very long secret key SYMETTRICAL.  " +
            "we can switch to asymettrical but really have no need.   When we switch this key, " +
            "all clients are logged out at the same time UNLESS you overlap the keys." +
            "security keys should be rotated but most small companies do not do this");

    public static final String USER_ID_KEY = "userId";

    @Override
    public XFuture<AuthenticateResponse> authenticate(@Jackson AuthenticateRequest request) {

        if("dean".equals(request.username) && "password".equals(request.password)) {
            return generateToken();
        }

        AuthenticateResponse authenticateResponse = new AuthenticateResponse();
        authenticateResponse.setAuthenticated(false);
        return XFuture.completedFuture(authenticateResponse);
    }

    private XFuture<AuthenticateResponse> generateToken() {
        try {
            String token = JWT.create()
                    .withIssuer("auth0")
                    .sign(ALGORITHM);

            AuthenticateResponse response = new AuthenticateResponse();
            response.setAuthenticated(true);
            response.setToken(token);
            return XFuture.completedFuture(response);
        } catch (JWTCreationException exception){
            throw SneakyThrow.sneak(exception);
        }
    }
}
