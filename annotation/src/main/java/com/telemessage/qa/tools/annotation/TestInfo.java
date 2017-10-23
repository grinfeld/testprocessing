package com.telemessage.qa.tools.annotation;

import java.lang.annotation.*;

/**
 * @author Grinfeld Mikhail
 * @since 10/22/2017
 *
 * This is annotation for annotation processor which generates list of of classes and methods to fill it from annotations.
 * Additional it creates class which behaves as wrapper for all the Data
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface TestInfo {
    String classSuffix() default "Data";
    Class<? extends Annotation>[] annotations();
    Class<? extends Annotation> root();
    String containerName() default "TestData";
}
