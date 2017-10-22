package com.telemessage.qa.tools.testprocessing.annotations.data;

import com.telemessage.qa.tools.staticdata.FlowId;
import com.telemessage.qa.tools.staticdata.PrepaidFactor;

import static com.telemessage.qa.tools.testprocessing.Constants.*;

public @interface User {
    String username() default EMPTY_STRING;
    String login() default EMPTY_STRING;
    String password() default EMPTY_STRING;
    long callback() default EMPTY_NUMBER; // defines the callback settings of the user
    FlowId flowId() default FlowId.NA;
    double balance() default EMPTY_NUMBER;
    long fallbackDelay() default EMPTY_NUMBER; // defines the time to wait (in seconds) before falling back to SMS
    int[] settings() default {};
    PrepaidFactor prepaidFactor() default PrepaidFactor.NA;
    int userType() default EMPTY_NUMBER;
    Email[] email() default {}; // despite it's configured as array - we'll use only first value. It's defined as array, because it could be set NULL as default (annotation limitation)
    Phone[] mobile() default {}; // despite it's configured as array - we'll use only first value. It's defined as array, because it could be set NULL as default (annotation limitation)
    Phone[] business() default {}; // despite it's configured as array - we'll use only first value. It's defined as array, because it could be set NULL as default (annotation limitation)
    Phone[] fax() default {}; // despite it's configured as array - we'll use only first value. It's defined as array, because it could be set NULL as default (annotation limitation)
    Phone[] home() default {}; // despite it's configured as array - we'll use only first value. It's defined as array, because it could be set NULL as default (annotation limitation)
    Client[] client() default {};
    Protocol[] protocols() default {};
    ShortCode[] shortCodes() default {};
    boolean delete() default false;
    boolean create() default false;
    String appId() default "1";
    String splitSMS() default "1";
    String timeZone() default EMPTY_STRING;
    String country() default EMPTY_STRING;
    String alphanumeric() default "";
}
