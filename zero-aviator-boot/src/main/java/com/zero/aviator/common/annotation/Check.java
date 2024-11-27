package com.zero.aviator.common.annotation;

import java.lang.annotation.*;


/**
 * @author Micro-Organism
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(CheckContainer.class)
public @interface Check {

    String ex() default "";

    String msg() default "";

}
