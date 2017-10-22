package com.telemessage.qa.tools.testprocessing.annotations.data;

import static com.telemessage.qa.tools.testprocessing.Constants.EMPTY_STRING;

public @interface ShortCode {
    String shortCode() default EMPTY_STRING;
    String countryId() default EMPTY_STRING;
    String providerId() default EMPTY_STRING;
}
