package com.telemessage.qa.tools.testprocessing;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;

/**
 * @author Grinfeld Mikhail
 * @since 10/22/2017.
 */
@RunWith(Parameterized.class)
public class TestInfoAnnotatedClassTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        LinkedList<Object[]> ret = new LinkedList<>();

        ret.add(new Object[]{new SimpleVerifierCase()});

        return ret;
    }
    private static JavaCompiler compiler;
    private StandardJavaFileManager fileManager;
    private DiagnosticCollector<JavaFileObject> collector;
    private CompilerTestCase currentTestCase;

    public TestInfoAnnotatedClassTest(CompilerTestCase currentTestCase) {
        this.currentTestCase = currentTestCase;
    }

    @BeforeClass
    public static void initClass() throws Exception {
        //get the java compiler.
        compiler = ToolProvider.getSystemJavaCompiler();
    }

    @Before
    public void initTest() throws Exception {

        //configure the diagnostics collector.
        collector = new DiagnosticCollector<>();
        fileManager = compiler.getStandardFileManager(collector, Locale.US, Charset.forName("UTF-8"));
    }

    @Test
    public void generateCodeTest() {
        String[] files = currentTestCase.getClassesToCompile();
        try {
            //streams.
            ByteArrayOutputStream stdoutStream = new ByteArrayOutputStream();
            OutputStreamWriter stdout = new OutputStreamWriter(stdoutStream);


            JavaCompiler.CompilationTask task = compiler.getTask(stdout, fileManager, collector, null, null, fileManager.getJavaFileObjects(files));

            Boolean result = task.call();

            String stdoutS = new String(stdoutStream.toByteArray());


            //perform the verifications.
            currentTestCase.test(collector.getDiagnostics(), stdoutS, result);
        } finally {
            //CompilerTestUtils.cleanClassFiles(0, files);
        }
    }

}
