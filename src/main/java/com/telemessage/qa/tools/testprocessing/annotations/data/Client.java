package com.telemessage.qa.tools.testprocessing.annotations.data;

import com.telemessage.qa.tools.staticdata.SubType;
import static com.telemessage.qa.tools.testprocessing.Constants.EMPTY_STRING;

public @interface Client {
    String value() default EMPTY_STRING;
    SubType type() default SubType.NONE;
    boolean ensure() default false;
}
