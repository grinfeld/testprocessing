package com.telemessage.qa.tools.testprocessing.annotations.data;

import com.telemessage.qa.tools.staticdata.Type;

import static com.telemessage.qa.tools.testprocessing.Constants.*;

public @interface Source {
    String value() default EMPTY_STRING;
    String filename() default EMPTY_STRING;
    int callbackSet() default EMPTY_NUMBER;
    Regex[] sourceManipulation() default {};
    boolean alphanumeric() default false;
    Type deviceType() default Type.NA;
}
