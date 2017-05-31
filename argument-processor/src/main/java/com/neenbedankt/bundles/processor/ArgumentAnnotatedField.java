package com.neenbedankt.bundles.processor;

import com.neenbedankt.bundles.annotation.Argument;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.List;

public class ArgumentAnnotatedField extends AnnotatedField {

    private final List<AnnotationMirror> sourceAnnotations;
    private boolean nullableAnnotation = false;
    private boolean nonNullAnnotation = false;

    public ArgumentAnnotatedField(Element element) {
        super(element, isRequired(element), getKey(element));
        sourceAnnotations = collectSourceAnnotations(element);
    }

    private List<AnnotationMirror> collectSourceAnnotations(Element element) {
        List<AnnotationMirror> annotationMirrors = new ArrayList<>(element.getAnnotationMirrors().size());
        for (AnnotationMirror am : element.getAnnotationMirrors()) {
            if (am.getAnnotationType().asElement().getSimpleName().toString().equals("Nullable")) {
                nullableAnnotation = true;
                annotationMirrors.add(am);
            } else if (am.getAnnotationType().asElement().getSimpleName().toString().equals("NonNull")) {
                nonNullAnnotation = true;
                annotationMirrors.add(am);
            } else if (am.getAnnotationType().asElement().toString().startsWith("android.support.annotation.")) {
                annotationMirrors.add(am);
            }
        }
        return annotationMirrors;
    }

    public List<AnnotationMirror> getSourceAnnotations() {
        return sourceAnnotations;
    }

    private static String getKey(Element element) {
        Argument annotation = element.getAnnotation(Argument.class);
        String field = element.getSimpleName().toString();
        if (!"".equals(annotation.key())) {
            return annotation.key();
        }
        return getVariableName(field);
    }

    private static boolean isRequired(Element element) {
        Argument annotation = element.getAnnotation(Argument.class);
        return annotation.required();
    }

    public boolean hasNullableAnnotation() {
        return nullableAnnotation;
    }

    public boolean hasNonNullAnnotation() {
        return nonNullAnnotation;
    }

}
