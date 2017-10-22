package com.telemessage.qa.tools.testprocessing;

import com.squareup.javawriter.JavaWriter;
import com.telemessage.qa.tools.testprocessing.annotations.data.*;
import com.telemessage.qa.tools.testprocessing.annotations.processor.TestInfo;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

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

            for (Method m : clazz.getMethods()) {
                boolean isArray = m.getReturnType().isArray();
                Class typeToCheck = isArray ? m.getReturnType().getComponentType() : m.getReturnType();

                if (isArray && typeToCheck.isPrimitive()) {
                    typeToCheck = boxedClass(typeToCheck);
                }
                String typeToBe = typeToCheck.getCanonicalName();
                if (typeToCheck.isAnnotation()) {
                    typeToBe = prefix + "." + typeToCheck.getSimpleName() + classSuffix;
                    System.out.println("ToBe: " + typeToBe);
                }
                writeFieldAndMethods(jw, typeToBe, m.getName(), isArray);
            }

            jw.endType();
            jw.close();
        }

    }

    private Class boxedClass(Class clazz) {
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

    private void writeFieldAndMethods(JavaWriter jw, String fieldType, String fieldName, boolean isArray) throws IOException {
        if (excluded.contains(fieldName))
            return;
        String type = fieldType;
        if (isArray) {
            type = "java.util.List<" + fieldType + ">";
        }
        jw.emitField(type, fieldName, EnumSet.of(Modifier.PRIVATE));
        jw.emitEmptyLine();
        jw.beginMethod("void", fieldName, EnumSet.of(Modifier.PUBLIC), type, fieldName);
        if (isArray) {
            jw.emitStatement("this.%s = %s == null ? new java.util.ArrayList<%s>() : %s", fieldName, fieldName, fieldType, fieldName);
        } else {
            jw.emitStatement("this.%s = %s", fieldName, fieldName);
        }
        jw.endMethod();
        jw.beginMethod(type, fieldName, EnumSet.of(Modifier.PUBLIC));
        jw.emitStatement("return this.%s", fieldName);
        jw.endMethod();
        jw.emitEmptyLine();
    }
}
