package com.telemessage.qa.tools.testprocessing.annotations.processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Grinfeld Mikhail
 * @since 10/22/2017.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface TestInfo {
    String classSuffix() default "Data";
}
