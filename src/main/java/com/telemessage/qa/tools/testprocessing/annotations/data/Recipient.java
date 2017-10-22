package com.telemessage.qa.tools.testprocessing.annotations.data;

import com.telemessage.qa.tools.staticdata.Envs;
import com.telemessage.qa.tools.staticdata.SubType;
import com.telemessage.qa.tools.staticdata.Type;

import static com.telemessage.qa.tools.testprocessing.Constants.*;

public @interface Recipient {
    Envs[] env() default {};
    short[] status() default {};
    String[] statusString() default {};
    int overrideProvider() default EMPTY_NUMBER; // This is the provider we want to use when sending to this recipient
    int provider() default EMPTY_NUMBER;         // The expected provider id to handle this recipient
    Source[] source() default {};
    Type destType() default Type.NA;
    Email[] email() default {}; // despite it's configured as array - we'll use only first value. It's defined as array, because it could be set NULL as default (annotation limitation)
    Phone[] phone() default {}; // despite it's configured as array - we'll use only first value. It's defined as array, because it could be set NULL as default (annotation limitation)
    // String destCountry() default EMPTY_STRING;
    Type testType() default Type.NA;
    SubType[] testSubType() default {};
    String destination() default EMPTY_STRING;
    String device() default EMPTY_STRING;
    double credits() default EMPTY_NUMBER;
}
