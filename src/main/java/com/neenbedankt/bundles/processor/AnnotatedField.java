package com.neenbedankt.bundles.processor;

import javax.lang.model.element.Element;

import com.neenbedankt.bundles.annotation.Argument;

class AnnotatedField implements Comparable<AnnotatedField> {
    private final String name;
    private final String key;
    private final String type;
    private final Element element;
    private final boolean required;


    public AnnotatedField(Element element) {
        this.name = element.getSimpleName().toString();
        this.key = getKey(element);
        this.type = element.asType().toString();
        this.element = element;
        Argument argument = element.getAnnotation(Argument.class);
        required = argument != null ? argument.required() : false;
    }

    public String getVariableName() {
        if (name.matches("^m[A-Z]{1}")) {
            return name.substring(1, 2).toLowerCase();
        } else if (name.matches("m[A-Z]{1}.*")) {
            return name.substring(1, 2).toLowerCase() + name.substring(2);
        }
        return name;
    }

    public String getKey() {
        return this.key;
    }

    private String getKey(Element element) {
        //FIXME dependency on the Argument annotation...
        Argument annotation = element.getAnnotation(Argument.class);
        if (annotation != null) {
            if (!"".equals(annotation.key()) && annotation.key() != null) {
                return annotation.key();
            }
        }
        return getVariableName();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Element getElement() {
        return element;
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AnnotatedField other = (AnnotatedField) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return key+"/"+type;
    }

    public String getRawType() {
        if (isArray()) {
            return type.substring(0, type.length() - 2);
        }
        return type;
    }

    public boolean isArray() {
        return type.endsWith("[]");
    }

    @Override
    public int compareTo(AnnotatedField o) {
        return getVariableName().compareTo(o.getVariableName());
    }
}