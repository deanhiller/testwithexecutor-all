package org.webpieces.execdemo.example.secure;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.webpieces.ctx.api.RouterHeader;
import org.webpieces.execdemo.example.publicjson.AuthController;
import org.webpieces.http.exception.ForbiddenException;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.XFuture;

public class JsonAuthFilter extends RouteFilter<Void> {
    @Override
    public void initialize(Void initialConfig) {
    }

    @Override
    public XFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {

        RouterHeader header = meta.getCtx().getRequest().getSingleHeader("Authorization");
        if(header == null)
            throw new ForbiddenException("need auth header");
        String token = header.getValue().trim();
        try {
            JWTVerifier verifier = JWT.require(AuthController.ALGORITHM)
                    .withIssuer("auth0")
                    .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);
            String user = decodeJson(jwt);
            meta.getCtx().getRequest().requestState.put(AuthController.USER_ID_KEY, user);
        } catch (JWTVerificationException exception){
            throw new ForbiddenException("Invalid token", exception);
        }

        //invoke next filter or controller
        return nextFilter.invoke(meta);
    }

    private String decodeJson(DecodedJWT jwt) {
        //get user 'dean' from jwt token.  I was lazy here
        return "dean";
    }
}
