package com.telemessage.qa.tools.testprocessing.annotations.data;

import static com.telemessage.qa.tools.testprocessing.Constants.*;

public @interface Protocol {
    long id() default EMPTY_NUMBER;
    String auth() default EMPTY_STRING;
    String status() default EMPTY_STRING;
    String notifyUrl() default EMPTY_STRING;
    String startIp() default EMPTY_STRING;
    String endIp() default EMPTY_STRING;
    String address() default EMPTY_STRING;
    String domain() default EMPTY_STRING;
    String type() default EMPTY_STRING;
}
