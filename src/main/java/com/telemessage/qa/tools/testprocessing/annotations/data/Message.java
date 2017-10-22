package com.telemessage.qa.tools.testprocessing.annotations.data;

import com.telemessage.qa.tools.staticdata.FlowId;
import com.telemessage.qa.tools.staticdata.Relation;
import com.telemessage.qa.tools.staticdata.Type;

import static com.telemessage.qa.tools.testprocessing.Constants.EMPTY_NUMBER;
import static com.telemessage.qa.tools.testprocessing.Constants.EMPTY_STRING;

public @interface Message {
    String text() default EMPTY_STRING;
    String assertText() default EMPTY_STRING;
    String subject() default EMPTY_STRING;
    String from() default EMPTY_STRING;
    String alphanumeric() default EMPTY_STRING;
    String originator() default EMPTY_STRING;
    Type originatorType() default Type.SMS;
    String conferenceNumber() default EMPTY_STRING;
    String deliveryReceiptURL() default EMPTY_STRING;
    long callback() default EMPTY_NUMBER;
    short overallStatus() default EMPTY_NUMBER;
    String overallStatusString() default EMPTY_STRING;
    String sendFile() default EMPTY_STRING;
    String statusFile() default EMPTY_STRING;
    FlowId flowId() default FlowId.NA;
    String extMessageId() default EMPTY_STRING;
    Attachment[] attachments() default {};
    Relation relation() default Relation.NA;
    long sendAt() default EMPTY_NUMBER;
    long expiredAt() default EMPTY_NUMBER;
}
