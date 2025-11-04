package com.tiffin.common.annotation;

import java.lang.annotation.*;

/**
 * Custom NonNull annotation for null safety
 * Used to mark parameters, return values, and fields that should never be null
 */
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NonNull {
    String value() default "";
}