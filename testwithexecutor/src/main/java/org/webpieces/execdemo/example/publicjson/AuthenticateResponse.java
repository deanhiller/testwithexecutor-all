package org.webpieces.execdemo.example.publicjson;

public class AuthenticateResponse {
    public boolean isAuthenticated;
    public String token;

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
