package com.telemessage.qa.tools.testdata.processor;

import com.squareup.javawriter.JavaWriter;
import com.telemessage.qa.tools.annotation.TestInfo;

import javax.annotation.processing.Filer;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Grinfeld Mikhail
 * @since 10/22/2017.
 */
class TestInfoAnnotatedClass {

    private static final List<String> excluded = Arrays.asList("equals",
            "toString",
            "hashCode",
            "annotationType");

    private TypeElement annotatedClassElement;
    private String classSuffix;
    private List<? extends AnnotationValue> annotations;
    private TypeMirror root;
    private String container;

    private static Class<? extends Annotation> getClass(AnnotationValue av) {
        try {
            return (Class<? extends Annotation>) Class.forName(av.getValue().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Failed to get class");
    }

    TestInfoAnnotatedClass(TypeElement classElement, TypeMirror root, List<? extends AnnotationValue> annotations) {
        TestInfo annotation = classElement.getAnnotation(TestInfo.class);
        this.annotatedClassElement = classElement;
        this.classSuffix = annotation.classSuffix();
        this.container = annotation.containerName();
        this.root = root;
        this.annotations = annotations;

        if ("".equals(classSuffix.trim())) {
            throw new IllegalArgumentException(
                    String.format("id() in @%s for class %s is null or empty! that's not allowed",
                            TestInfo.class.getSimpleName(), classElement.getQualifiedName().toString()));
        }

        if(this.annotations == null || this.annotations.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("list of annotations is empty in @%s for class %s is null or empty! that's not allowed",
                            TestInfo.class.getSimpleName(), classElement.getQualifiedName().toString()));
        }
    }

    /**
     * The original element that was annotated with @Factory
     * @return type element of annotated class
     */
    TypeElement getTypeElement() {
        return annotatedClassElement;
    }

    void generateCode(Elements elementUtils, Filer filer) throws IOException, ClassNotFoundException {

        PackageElement pkg = elementUtils.getPackageOf(annotatedClassElement);
        String prefix = pkg.getQualifiedName().toString();
        for(AnnotationValue typeElement : annotations) {
            Class<? extends Annotation> clazz = (Class<? extends Annotation>) Class.forName(typeElement.getValue().toString());
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
            for (Method m :  clazz.getMethods()) {
                boolean isArray = m.getReturnType().isArray();
                Class typeToCheck = isArray ? m.getReturnType().getComponentType() : m.getReturnType();
                if (typeToCheck.isPrimitive()) {
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

            writeFieldsSetterAndGetter(jw, infos);
            writeToString(jw, infos);

            writeFillMethod(jw, infos, className, clazz);

            writePrivateStaticToStringForList(jw);
            writeInitialValueMethod(jw);

            jw.endType();
            jw.close();
        }
    }

    private FieldData prepareFieldData(String fieldType, String fieldName, Class annotClass, boolean isArray) {
        String type = fieldType;
        if (isArray) {
            type = "java.util.List<" + fieldType + ">";
        }
        return new FieldData(fieldName, type, fieldType, fieldName, annotClass, isArray);
    }

    private void writeFillMethod(JavaWriter jw, List<FieldData> infos, String currentClassName, Class annotCLass) throws IOException {
        jw.beginMethod(currentClassName, "fill", EnumSet.of(Modifier.PUBLIC), annotCLass.getCanonicalName(), "me");
        jw.emitStatement("if(me == null) return this");
        Map<String, FieldData> mapData = infos.stream().collect(Collectors.toMap(FieldData::getName, Function.identity(), (k1, k2) -> k1));
        for (Method m : annotCLass.getMethods()) {
            FieldData fd = mapData.get(m.getName());
            if (fd != null && m.getReturnType() != null && !m.getReturnType().equals(Void.class)) {
                if (!fd.isArray() &&  !m.getReturnType().isAnnotation()) {
                    appendfillNoAnnotationType(jw, m.getReturnType(), fd);
                } else if (m.getReturnType().isAnnotation()) {
                    appendFillWithAnnotation(jw, m.getReturnType(), fd, classSuffix);
                } else if (fd.isArray()) {
                    appendFillForArrayElement(jw, fd, m.getReturnType().getComponentType(), classSuffix);
                }
            }
        }
        jw.emitStatement("return this");
        jw.endMethod();
        jw.emitEmptyLine();
    }

    private static void appendFillForArrayElement(JavaWriter jw, FieldData fd, Class type, String classSuffix) throws IOException {
        String objClassName = type.getSimpleName() + classSuffix;
        String method = fd.method.substring(0,1).toUpperCase() + fd.method.substring(1);
        jw.beginControlFlow("if(me.%s != null)", fd.getMethod() + "()");
        jw.beginControlFlow("for (%s el : me.%s)", type.getCanonicalName(), fd.getMethod() + "()");
        if (type.isAnnotation()) {
            jw.emitStatement("this.add%s(el != null ? new %s().fill(el) : null)", method, objClassName);
        } else {
            jw.emitStatement("%s e = java.util.Objects.equals(el, initialValue(%s.class)) ? null : el",
                    boxedClass(type).getCanonicalName(), type.getCanonicalName());

            jw.emitStatement("if (e != null) this.add%s(e)", method);
        }
        jw.endControlFlow();
        jw.endControlFlow();
    }

    private static void appendFillWithAnnotation(JavaWriter jw, Class returnType, FieldData fd, String classSuffix) throws IOException {
        String objClassName = returnType.getSimpleName() + classSuffix;
        jw.emitStatement("%s(new %s().fill(%s))", fd.method, objClassName, "me." + fd.method + "()");
    }

    private static void appendfillNoAnnotationType(JavaWriter jw, Class returnType, FieldData fd) throws IOException {
        jw.emitStatement("if (!java.util.Objects.equals(%s, initialValue(" + returnType.getCanonicalName() + ".class))) %s(%s)",
            "me." + fd.method + "()", fd.method, "me." + fd.method + "()");
    }

    private static void writeFieldsSetterAndGetter(JavaWriter jw, List<FieldData> infos) throws IOException {
        for (FieldData f : infos) {
            writeFieldAndMethods(jw, f);
        }
    }

    private static void writeToString(JavaWriter jw, List<FieldData> infos) throws IOException {
        jw.beginMethod("String", "toString", EnumSet.of(Modifier.PUBLIC));
        String toStringData = infos.stream()
            .map(f -> f.isArray() ?
                    "\"" + f.getName() + "s = \" + " + "listAsString(" + f.getMethod() + "())" :
                    "\"" + f.getName() + " = \" + " + "String.valueOf(" + f.getMethod() + "())")
            .collect(Collectors.joining(" + \",\" +\r\n", "return \"{\" + ", " + \"}\""));
        jw.emitStatement(toStringData);
        jw.endMethod();
        jw.emitEmptyLine();
    }

    private static void writePrivateStaticToStringForList(JavaWriter jw) throws IOException {
        jw.beginMethod("String", "listAsString", EnumSet.of(Modifier.PRIVATE, Modifier.STATIC), "java.util.List<?>", "list");
        jw.emitStatement("return java.util.Optional.ofNullable(list).orElseGet(java.util.ArrayList::new).stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(\",\", \"[\", \"]\"))");
        jw.endMethod();
        jw.emitEmptyLine();
    }

    private static void writeFieldAndMethods(JavaWriter jw, FieldData info) throws IOException {
        String methodSuffix = info.getName().substring(0, 1).toUpperCase() + info.getName().substring(1);
        jw.emitField(info.getMethodType(), info.getMethod(), EnumSet.of(Modifier.PRIVATE));
        jw.beginMethod("void", info.getMethod(), EnumSet.of(Modifier.PUBLIC), info.getMethodType(), info.getName());
        if (info.isArray()) {
            jw.emitStatement("this.%s = %s == null ? new java.util.ArrayList<%s>() : %s", info.getName(), info.getName(), info.getFieldType(), info.getName());
        } else {
            jw.emitStatement("this.%s = %s", info.getName(), info.getName());
        }
        jw.endMethod();
        jw.beginMethod(info.getMethodType(), info.getMethod(), EnumSet.of(Modifier.PUBLIC));
        jw.emitStatement("return this.%s", info.getMethod());
        jw.endMethod();
        if (info.isArray()) {
            jw.beginMethod("boolean", "add" + methodSuffix, EnumSet.of(Modifier.PUBLIC), info.getFieldType(), info.getName());
            jw.emitStatement("return %s != null ? this.%s.add(%s) : false", info.getName(), info.getName(), info.getName());
            jw.endMethod();
        }

        jw.emitEmptyLine();
    }

    private static void writeInitialValueMethod(JavaWriter jw) throws IOException {
        jw.beginMethod("Object", "initialValue",  EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), "Class", "clazz");
        jw.emitStatement("if (clazz.equals(double.class)) return (double)-1");
        jw.emitStatement("if (clazz.equals(int.class)) return (int)-1");
        jw.emitStatement("if (clazz.equals(float.class)) return (float)-1");
        jw.emitStatement("if (clazz.equals(short.class)) return (short)-1");
        jw.emitStatement("if (clazz.equals(long.class)) return (long)-1");
        jw.emitStatement("if (clazz.equals(boolean.class)) return (boolean)false");
        jw.emitStatement("if (clazz.equals(byte.class)) return (byte)-1");
        jw.emitStatement("if (clazz.equals(char.class)) return (char)'\\0'");
        jw.emitStatement("if (clazz.equals(String.class)) return \"\"");
        jw.emitStatement("if (clazz.isEnum()) try { return Class.forName(clazz.getCanonicalName() + \".NA\"); } catch (Exception ignore){}");
        jw.emitStatement("return null");
        jw.endMethod();
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

    private static class FieldData {
        private String method;
        private String methodType;
        private String fieldType;
        boolean isArray;
        private String name;
        private Class annotClass;

        FieldData(String method, String methodType, String fieldType, String name, Class annotClass, boolean isArray) {
            this.method = method;
            this.methodType = methodType;
            this.isArray = isArray;
            this.name = name;
            this.fieldType = fieldType;
            this.annotClass = annotClass;
        }

        String getMethod() { return method; }
        void setMethod(String method) { this.method = method; }
        String getMethodType() { return methodType; }
        void setMethodType(String methodType) { this.methodType = methodType; }
        boolean isArray() { return isArray; }
        void setArray(boolean array) { isArray = array; }
        String getName() { return name; }
        void setName(String name) { this.name = name; }
        String getFieldType() { return fieldType; }
        void setFieldType(String fieldType) { this.fieldType = fieldType; }
        Class getAnnotClass() { return annotClass; }
        void setAnnotClass(Class annotClass) { this.annotClass = annotClass; }
    }
}
