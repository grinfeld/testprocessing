package com.telemessage.qa.tools.testprocessing;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Grinfeld Mikhail
 * @since 10/22/2017.
 */
public class SimpleVerifierCase implements CompilerTestCase {

    @Override
    public String[] getClassesToCompile() {
        return new String[] {
                "src/test/java/com/telemessage/qa/tools/testprocessing/TestElement.java"
        };
    }

    @Override
    public void test(List<Diagnostic<? extends JavaFileObject>> diagnostics, String stdoutS, Boolean result) {

        //no mandatory warnings or compilation errors should be found.
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
            if (diagnostic.getKind() == Diagnostic.Kind.MANDATORY_WARNING || diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                fail("Failed with message: " + diagnostic.getMessage(null));
            }
        }

        assertEquals("Files should have no compilation errors", Boolean.TRUE, result);
    }

}
