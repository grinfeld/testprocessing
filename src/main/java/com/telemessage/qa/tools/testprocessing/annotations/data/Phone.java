package com.telemessage.qa.tools.testprocessing.annotations.data;


import com.telemessage.qa.tools.staticdata.PhoneType;

import static com.telemessage.qa.tools.testprocessing.Constants.EMPTY_STRING;

public @interface Phone {
    String value() default EMPTY_STRING;
    String country() default EMPTY_STRING;
    String area() default EMPTY_STRING;
    PhoneType type() default PhoneType.MOBILE;
    boolean excludeKosher() default false;
}
