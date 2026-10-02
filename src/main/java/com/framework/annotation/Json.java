package com.framework.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Json {
    String value(); // ex: "/home"
    String method() default "GET" ;
    boolean isJson() default false ;
}
