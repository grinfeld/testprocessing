package com.telemessage.qa.tools.testdata.processor;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;

/**
 * SPI for testing compilation errors.
 * @author juancavallotti (http://jcavallotti.blogspot.co.il/2013/05/how-to-unit-test-annotation-processor.html)
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
    public void generateCodeTest() throws IOException {
        String[] files = currentTestCase.getClassesToCompile();
        // streams
        ByteArrayOutputStream stdoutStream = new ByteArrayOutputStream();
        OutputStreamWriter stdout = new OutputStreamWriter(stdoutStream);

        JavaCompiler.CompilationTask task = compiler.getTask(stdout, fileManager, collector, null, null, fileManager.getJavaFileObjects(files));

        Boolean result = task.call();

        String stdoutS = new String(stdoutStream.toByteArray());

        //perform the verifications.
        currentTestCase.test(collector.getDiagnostics(), stdoutS, result);
    }

    @AfterClass
    public static void finishTest() throws IOException {
        // cleanClassFiles("target/generated-test-sources/");
        // cleanClassFiles("target/test-classes/com/telemessage/qa/tools/testprocessing/");
    }

    private static void cleanClassFiles(String initPath) throws IOException {
        File dir = Paths.get(initPath).toFile();
        if (dir.exists()) {
            Files.find(Paths.get(initPath),
                    Integer.MAX_VALUE,
                    (filePath, fileAttr) -> fileAttr.isRegularFile())
                    .forEach(f -> {
                        File file = f.toFile();
                        if (file.exists())
                            file.delete();
                    });
        }
    }

}
