package nl.littlerobots.bundles.processor;

import javax.lang.model.element.Element;

public class FrozenAnnotatedField extends AnnotatedField {

    public FrozenAnnotatedField(Element element) {
        super(element, false, getVariableName(element.getSimpleName().toString()));
    }

}
