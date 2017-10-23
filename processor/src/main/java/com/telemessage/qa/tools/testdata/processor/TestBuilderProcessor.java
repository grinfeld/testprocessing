package com.telemessage.qa.tools.testdata.processor;

import com.google.auto.service.AutoService;
import com.telemessage.qa.tools.annotation.TestInfo;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.*;

/**
 * @author Grinfeld Mikhail
 * @since 10/22/2017.
 */
@AutoService(Processor.class)
public class TestBuilderProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    private Map<String, TestInfoAnnotatedClass> readyFor;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        readyFor = new HashMap<>();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(TestInfo.class)) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                error(annotatedElement, "Only classes can be annotated with @%s",
                        TestInfo.class.getSimpleName());
                return true;
            }

            // We can cast it, because we know that it of ElementKind.CLASS
            TypeElement typeElement = (TypeElement) annotatedElement;

            if (readyFor.containsKey(typeElement.getQualifiedName().toString())) {
                error(typeElement, "Already Exists");
                return true;
            }

            TypeMirror root = null;
            List<? extends AnnotationValue> annotationList = null;

            List<? extends AnnotationMirror> annotationMirrors = typeElement.getAnnotationMirrors();
            for (AnnotationMirror annotationMirror : annotationMirrors) {
                // Get the ExecutableElement:AnnotationValue pairs from the annotation element
                Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues
                        = annotationMirror.getElementValues();

                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {

                    String key = entry.getKey().getSimpleName().toString();
                    Object value = entry.getValue().getValue();

                    switch (key) {
                        case "root":
                            root = (TypeMirror) value;
                            break;
                        case "annotations":
                            annotationList = (List<? extends AnnotationValue>) value;
                            break;
                    }

                }
            }
            try {
                TestInfoAnnotatedClass annotatedClass =
                        new TestInfoAnnotatedClass(typeElement, root, annotationList); // throws IllegalArgumentException
                if (!isValidClass(annotatedClass)) {
                    return true; // Error message printed, exit processing
                }
                readyFor.put(typeElement.getQualifiedName().toString(), annotatedClass);
            } catch (IllegalArgumentException e) {
                // @Factory.id() is empty
                error(typeElement, e.getMessage());
                return true;
            }
        }

        try {
            for (TestInfoAnnotatedClass clazz : readyFor.values()) {
                clazz.generateCode(elementUtils, filer);
            }
            return true;
        } catch (ClassNotFoundException | ClassCastException ioe) {
            return false;
        } catch (Exception ioe) {
            error(null, "3333 " + ioe.getMessage() + " " + ioe.getClass().getName());
        }

        return false;
    }

    private boolean isValidClass(TestInfoAnnotatedClass item) {
        // Cast to TypeElement, has more type specific methods
        TypeElement classElement = item.getTypeElement();
        if (!classElement.getModifiers().contains(Modifier.PUBLIC)) {
            error(classElement, "The class %s is not public.",
                    classElement.getQualifiedName().toString());
            return false;
        }

        // Check if an empty public constructor is given
        for (Element enclosed : classElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructorElement = (ExecutableElement) enclosed;
                if (constructorElement.getParameters().size() == 0 && constructorElement.getModifiers()
                        .contains(Modifier.PUBLIC)) {
                    // Found an empty constructor
                    return true;
                }
            }
        }

        // No empty constructor found
        error(classElement, "The class %s must provide an public empty default constructor",
                classElement.getQualifiedName().toString());
        return false;
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<>();
        annotataions.add(TestInfo.class.getCanonicalName());
        return annotataions;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
