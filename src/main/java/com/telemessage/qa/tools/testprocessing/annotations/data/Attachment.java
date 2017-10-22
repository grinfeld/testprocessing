package com.telemessage.qa.tools.testprocessing.annotations.data;

import static com.telemessage.qa.tools.testprocessing.Constants.EMPTY_STRING;

public @interface Attachment {
    String filename() default EMPTY_STRING;
    String mimeType() default EMPTY_STRING;
}
