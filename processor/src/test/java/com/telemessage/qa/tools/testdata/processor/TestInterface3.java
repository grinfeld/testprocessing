package com.telemessage.qa.tools.testdata.processor;

/**
 * @author Grinfeld Mikhail
 * @since 10/23/2017.
 */
public @interface TestInterface3 {
    TestInterface4[] i4() default {};
    double doub();
}
