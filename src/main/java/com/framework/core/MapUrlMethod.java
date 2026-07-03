package com.framework.core;
import java.lang.reflect.Method;
import java.util.Objects;

public class MapUrlMethod {
    private String url;
    private String method;

    public MapUrlMethod(String url, String method) {
        this.url = url;
        this.method = method;
    }
    public String getUrl() { return url; }
    public String getMethod() { return method; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapUrlMethod)) return false;
        MapUrlMethod that = (MapUrlMethod) o;
        return Objects.equals(url, that.url) &&
               Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, method);
    }
}