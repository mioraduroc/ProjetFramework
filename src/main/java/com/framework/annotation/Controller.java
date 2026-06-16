package com.framework.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) // accessible à l'exécution
@Target(ElementType.TYPE)         // cibLe classes
// @Target(ElementType.METHOD)         // utilisable sur les méthodes
public @interface Controller {
    String value() default "/"; // paramètre obligatoire
}