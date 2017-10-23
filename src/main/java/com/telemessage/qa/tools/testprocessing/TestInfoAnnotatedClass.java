package com.telemessage.qa.tools.testprocessing;

import com.squareup.javawriter.JavaWriter;
import com.telemessage.qa.tools.testprocessing.annotations.data.*;
import com.telemessage.qa.tools.testprocessing.annotations.processor.TestInfo;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Grinfeld Mikhail
 * @since 10/22/2017.
 */
public class TestInfoAnnotatedClass {

    // there is meaning for order. First, classes without any nested annotation
    static final List<Class> list = Arrays.asList(new Class[] {
            Attachment.class,
            Client.class,
            Email.class,
            Phone.class,
            Protocol.class,
            Regex.class,
            ShortCode.class,
            Source.class,
            User.class,
            Recipient.class,
            Message.class,
            MyTest.class
    });

    static final List<String> excluded = Arrays.asList(new String[] {
            "equals",
            "toString",
            "hashCode",
            "annotationType"
    });

    private final TypeElement annotatedClassElement;
    private final String classSuffix;

    public TestInfoAnnotatedClass(TypeElement classElement) {
        this.annotatedClassElement = classElement;

        TestInfo annotation = classElement.getAnnotation(TestInfo.class);
        classSuffix = annotation == null ? null : annotation.classSuffix();

        if (classSuffix == null || "".equals(classSuffix.trim())) {
            throw new IllegalArgumentException(
                    String.format("id() in @%s for class %s is null or empty! that's not allowed",
                            TestInfo.class.getSimpleName(), classElement.getQualifiedName().toString()));
        }
        System.out.println(classSuffix);
    }

    public String getClassSuffix() {
        return classSuffix;
    }

    /**
     * The original element that was annotated with @Factory
     */
    public TypeElement getTypeElement() {
        return annotatedClassElement;
    }

    public void generateCode(Elements elementUtils, Filer filer) throws IOException {

        PackageElement pkg = elementUtils.getPackageOf(annotatedClassElement);
        String prefix = pkg.getQualifiedName().toString();
        for(Class<?> clazz : list) {

            String className = clazz.getSimpleName() + classSuffix;
            JavaFileObject jfo = null;
            try {
                jfo = filer.createSourceFile(prefix + "." + className);
            } catch (Exception e) {
                continue;
            }
            Writer writer = jfo.openWriter();
            JavaWriter jw = new JavaWriter(writer);

            if (!pkg.isUnnamed()) {
                jw.emitPackage(prefix);
                jw.emitEmptyLine();
            } else {
                jw.emitPackage("");
            }

            jw.beginType(className, "class", EnumSet.of(Modifier.PUBLIC));
            jw.emitEmptyLine();
            List<FieldData> infos = new ArrayList<>();
            for (Method m : clazz.getMethods()) {
                boolean isArray = m.getReturnType().isArray();
                Class typeToCheck = isArray ? m.getReturnType().getComponentType() : m.getReturnType();

                if (isArray && typeToCheck.isPrimitive()) {
                    typeToCheck = boxedClass(typeToCheck);
                }
                String typeToBe = typeToCheck.getCanonicalName();
                if (typeToCheck.isAnnotation()) {
                    typeToBe = prefix + "." + typeToCheck.getSimpleName() + classSuffix;
                }
                if (!excluded.contains(m.getName())) {
                    infos.add(prepareFieldData(typeToBe, m.getName(), typeToCheck, isArray));
                }
            }
            infos.forEach(f -> writeFieldAndMethods(jw, f));
            writeToString(jw, infos);


            writePrivateStaticToStringForList(jw);

            jw.endType();
            jw.close();
        }
    }

    private FieldData prepareFieldData(String fieldType, String fieldName, Class annotClass, boolean isArray) {
        String type = fieldType;
        if (isArray) {
            type = "java.util.List<" + fieldType + ">";
        }
        return new FieldData(fieldName + (isArray ? "s" : ""), type, fieldType, fieldName, annotClass, isArray);
    }

    private static void writeToString(JavaWriter jw, List<FieldData> infos) throws IOException {
        try {
            jw.beginMethod("String", "toString", EnumSet.of(Modifier.PUBLIC));
            String toStringData = infos.stream()
                .map(f -> f.isArray() ?
                        "\"" + f.getName() + "s = \" + " + "listAsString(" + f.getMethod() + "())" :
                        "\"" + f.getName() + " = \" + " + "String.valueOf(" + f.getMethod() + "())")
                .collect(Collectors.joining(" + \",\" +\r\n", "return \"{\" + ", " + \"}\""));
            jw.emitStatement(toStringData);
            jw.endMethod();
        } catch (Exception ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private static void writePrivateStaticToStringForList(JavaWriter jw) throws IOException {
        jw.beginMethod("String", "listAsString", EnumSet.of(Modifier.PRIVATE, Modifier.STATIC), "java.util.List<?>", "list");
        jw.emitStatement("return list.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(\",\", \"[\", \"]\"))");
        jw.endMethod();
        jw.emitEmptyLine();
    }

    private static Class boxedClass(Class clazz) {
        if (clazz.equals(double.class))
            return Double.class;
        else if (clazz.equals(int.class))
            return Integer.class;
        else if (clazz.equals(float.class))
            return Float.class;
        else if (clazz.equals(short.class))
            return Short.class;
        else if (clazz.equals(long.class))
            return Long.class;
        else if (clazz.equals(boolean.class))
            return Boolean.class;
        else if (clazz.equals(byte.class))
            return Byte.class;
        else if(clazz.equals(char.class))
            return Character.class;
        return clazz;
    }

    private static void writeFieldAndMethods(JavaWriter jw, FieldData info) {
        try {
            String methodSuffix = info.getName().substring(0, 1).toUpperCase() + info.getName().substring(1);
            jw.emitField(info.getMethodType(), info.getMethod(), EnumSet.of(Modifier.PRIVATE));
            jw.beginMethod("void", info.getMethod(), EnumSet.of(Modifier.PUBLIC), info.getMethodType(), info.getName());
            if (info.isArray()) {
                jw.emitStatement("this.%ss = %ss == null ? new java.util.ArrayList<%s>() : %ss", info.getName(), info.getName(), info.getFieldType(), info.getName());
            } else {
                jw.emitStatement("this.%s = %s", info.getName(), info.getName());
            }
            jw.endMethod();
            jw.beginMethod(info.getMethodType(), info.getMethod(), EnumSet.of(Modifier.PUBLIC));
            jw.emitStatement("return this.%s", info.getMethod());
            jw.endMethod();
            if (info.isArray()) {
                jw.beginMethod("boolean", "add" + methodSuffix, EnumSet.of(Modifier.PUBLIC), info.getFieldType(), info.getName());
                jw.emitStatement("return %s != null ? this.%ss.add(%s) : false", info.getName(), info.getName(), info.getName());
                jw.endMethod();
            }

            jw.emitEmptyLine();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private static class FieldData {
        private String method;
        private String methodType;
        private String fieldType;
        boolean isArray;
        private String name;
        private Class annotClass;

        public FieldData(String method, String methodType, String fieldType, String name, Class annotClass, boolean isArray) {
            this.method = method;
            this.methodType = methodType;
            this.isArray = isArray;
            this.name = name;
            this.fieldType = fieldType;
            this.annotClass = annotClass;
        }

        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        public String getMethodType() { return methodType; }
        public void setMethodType(String methodType) { this.methodType = methodType; }
        public boolean isArray() { return isArray; }
        public void setArray(boolean array) { isArray = array; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getFieldType() { return fieldType; }
        public void setFieldType(String fieldType) { this.fieldType = fieldType; }
        public Class getAnnotClass() { return annotClass; }
        public void setAnnotClass(Class annotClass) { this.annotClass = annotClass; }
    }
}
