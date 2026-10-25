package com.framework.model;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

public class MySession {
    private HttpSession httpSession;

    public MySession(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    public MySession(){
    }

    public void set(String key, Object value) {
        if ( httpSession == null) {
            throw new IllegalStateException("HttpSession non initialise.");
        }
        httpSession.setAttribute(key, value);
    }

    public Object get(String key) {
        if ( httpSession == null) {
            throw new IllegalStateException("HttpSession non initialise.");
        }
        return httpSession.getAttribute(key);
    }

    public void remove(String key) {
        if ( httpSession == null) {
            throw new IllegalStateException("HttpSession non initialise.");
        }
        httpSession.removeAttribute(key);
    }

    public HttpSession getHttpSession() {
        return httpSession;
    }

    public void setHttpSession(HttpSession httpSession) {
        this.httpSession = httpSession;
    }
}