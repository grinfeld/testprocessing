package com.telemessage.qa.tools.testdata.processor;

import com.telemessage.qa.tools.annotation.TestInfo;

/**
 * @author Grinfeld Mikhail
 * @since 10/22/2017.
 */
@TestInfo(
    annotations = {
        TestInterface1.class,
        TestInterface2.class,
        TestInterface3.class,
        TestInterface4.class,
    },
    root = TestInterface1.class
)
public class TestElement {
}
