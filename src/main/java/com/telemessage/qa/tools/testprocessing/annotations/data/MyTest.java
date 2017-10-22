package com.telemessage.qa.tools.testprocessing.annotations.data;

import com.telemessage.qa.tools.staticdata.Envs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.telemessage.qa.tools.testprocessing.Constants.EMPTY_NUMBER;
import static com.telemessage.qa.tools.testprocessing.Constants.EMPTY_STRING;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface MyTest {
    String description() default EMPTY_STRING;
    Recipient[] recipients() default {};
    Envs[] runOn() default {};
    User[] users() default {};
    Message[] message() default {};
    boolean production() default false;
    int waitForStatus() default EMPTY_NUMBER;
    int retries() default EMPTY_NUMBER;
    boolean disablePreTest() default false;
    String envType() default "charlie";
}
