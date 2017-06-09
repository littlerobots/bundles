package nl.littlerobots.bundles.processor;

import nl.littlerobots.bundles.annotation.Frozen;
import nl.littlerobots.bundles.internal.com.squareup.javawriter.JavaWriter;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@SupportedAnnotationTypes("nl.littlerobots.bundles.annotation.Frozen")
public class FrozenFieldsProcessor extends BaseProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        Set<? extends Element> elements = env.getElementsAnnotatedWith(Frozen.class);
        Map<TypeElement, Set<AnnotatedField>> fieldsByType = new HashMap<TypeElement, Set<AnnotatedField>>(100);
        for (Element element : elements) {
            if (element.getModifiers().contains(Modifier.FINAL) ||
                element.getModifiers().contains(Modifier.STATIC) ||
                element.getModifiers().contains(Modifier.PROTECTED) ||
                element.getModifiers().contains(Modifier.PRIVATE)) {
                error(element, "Field must not be private, protected, static or final");
                continue;
            }
            Set<AnnotatedField> fields = fieldsByType.get(element.getEnclosingElement());
            if (fields == null) {
                fields = new LinkedHashSet<>(10);
                fieldsByType.put((TypeElement)element.getEnclosingElement(), fields);
            }
            fields.add(new FrozenAnnotatedField(element));
        }
        for (Entry<TypeElement, Set<AnnotatedField>> entry : fieldsByType.entrySet()) {
            JavaFileObject jfo;
            try {
                jfo = processingEnv.getFiler().createSourceFile(entry.getKey().getQualifiedName() + "State", entry.getKey());
                Writer writer = jfo.openWriter();
                JavaWriter jw = new JavaWriter(writer);

                writePackage(jw, entry.getKey());

                jw.beginType(entry.getKey().getQualifiedName() + "State", "class", EnumSet.of(Modifier.FINAL));
                jw.beginMethod(null, entry.getKey().getSimpleName().toString()+"State", EnumSet.of(Modifier.PRIVATE));
                jw.endMethod();

                writeOnSaveInstanceState(jw, entry.getKey(), entry.getValue());
                writeOnRestoreInstanceState(jw, entry.getKey(), entry.getValue());
                jw.endType();

                jw.close();
            } catch (IOException e) {
                error(entry.getKey(), "Could not create state support class", e);
            }

        }
        return true;
    }

    private void writeOnRestoreInstanceState(JavaWriter jw, TypeElement key, Set<AnnotatedField> fields) throws IOException {
        jw.beginMethod("void", "restoreInstanceState", EnumSet.of(Modifier.STATIC), key.getQualifiedName().toString(), "target", "android.os.Bundle", "savedInstanceState");
        jw.beginControlFlow("if (savedInstanceState == null)");
        jw.emitStatement("return");
        jw.endControlFlow();

        for (AnnotatedField field : fields) {
            String op = getOperation(field);
            if (op == null) {
                error(field.getElement(), "Can't write injector, the bundle getter is unknown");
                return;
            }
            String cast = "Serializable".equals(op) ? "("+field.getType()+") " :  "";
            jw.emitStatement("target.%1$s = %4$ssavedInstanceState.get%2$s(\"%3$s\")", field.getName(), op, field.getKey(), cast);
        }
        jw.endMethod();
    }

    private void writeOnSaveInstanceState(JavaWriter jw, TypeElement key, Set<AnnotatedField> fields) throws IOException {
        jw.beginMethod("void", "saveInstanceState", EnumSet.of(Modifier.STATIC), key.getQualifiedName().toString(), "source", "android.os.Bundle", "outState");
        for (AnnotatedField field : fields) {
            writePutArguments(jw, String.format("source.%s", field.getName()), "outState", field, false);
        }
        jw.endMethod();
    }

}
