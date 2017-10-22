package com.telemessage.qa.tools.testprocessing.annotations.data;

import static com.telemessage.qa.tools.testprocessing.Constants.EMPTY_STRING;

public @interface Email {
    String value() default EMPTY_STRING;
    String prefix() default EMPTY_STRING;
    String domain() default EMPTY_STRING;
}
