package com.telemessage.qa.tools.testdata.processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Grinfeld Mikhail
 * @since 10/23/2017.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface TestInterface1 {
    TestInterface2 i1();
    TestInterface3[] i2() default {};
    String str1() default "";
    String[] str2() default {};
    int num1();
}
