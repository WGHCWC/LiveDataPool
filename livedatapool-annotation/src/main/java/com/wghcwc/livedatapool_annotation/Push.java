package com.wghcwc.livedatapool_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wghcwc
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD})
public @interface Push {
    String tag() default "";
}

