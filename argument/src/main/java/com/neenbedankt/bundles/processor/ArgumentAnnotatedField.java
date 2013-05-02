package com.neenbedankt.bundles.processor;

import javax.lang.model.element.Element;

import com.neenbedankt.bundles.annotation.Argument;

public class ArgumentAnnotatedField extends AnnotatedField {

    public ArgumentAnnotatedField(Element element) {
        super(element, isRequired(element), getKey(element));
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

}
