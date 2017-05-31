package com.neenbedankt.bundles.processor;

import com.neenbedankt.bundles.annotation.Argument;
import com.squareup.javawriter.JavaWriter;

import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

@SupportedAnnotationTypes("com.neenbedankt.bundles.annotation.Argument")
@SupportedOptions(value = FragmentArgumentsProcessor.OPT_DEFAULT_NULLABLE)
public class FragmentArgumentsProcessor extends BaseProcessor {

    final static String OPT_DEFAULT_NULLABLE = "argument.default.nullable";

    private Set<ArgumentAnnotatedField> collectArgumentsForType(Types typeUtil, TypeElement type,
                                                                Map<TypeElement, Set<Element>> fieldsByType, boolean requiredOnly, boolean processSuperClass) {
        Set<ArgumentAnnotatedField> arguments = new TreeSet<>();
        if (processSuperClass) {
            TypeMirror superClass = type.getSuperclass();
            if (superClass.getKind() != TypeKind.NONE) {
                arguments.addAll(collectArgumentsForType(typeUtil, (TypeElement) typeUtil.asElement(superClass),
                        fieldsByType, requiredOnly, true));
            }
        }
        Set<Element> fields = fieldsByType.get(type);
        if (fields == null) {
            return arguments;
        }
        for (Element element : fields) {
            if (requiredOnly) {
                Argument arg = element.getAnnotation(Argument.class);
                if (!arg.required()) {
                    continue;
                }
            }
            arguments.add(new ArgumentAnnotatedField(element));
        }
        return arguments;
    }

    @Override
    public boolean process(Set<? extends TypeElement> type, RoundEnvironment env) {
        Elements elementUtils = processingEnv.getElementUtils();
        Types typeUtils = processingEnv.getTypeUtils();
        Filer filer = processingEnv.getFiler();
        TypeElement fragmentType = elementUtils.getTypeElement("android.app.Fragment");
        TypeElement supportFragmentType = elementUtils.getTypeElement("android.support.v4.app.Fragment");
        TypeElement supportNonNull = elementUtils.getTypeElement("android.support.annotation.NonNull");

        boolean defaultNonNull = supportNonNull != null && !Boolean.parseBoolean(processingEnv.getOptions().get(OPT_DEFAULT_NULLABLE));

        Map<TypeElement, Set<Element>> fieldsByType = new HashMap<>(100);

        for (Element element : env.getElementsAnnotatedWith(Argument.class)) {
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

            if (!((fragmentType != null && typeUtils.isSubtype(enclosingElement.asType(), fragmentType.asType())) ||
                    (supportFragmentType != null && typeUtils.isSubtype(enclosingElement.asType(), supportFragmentType.asType())))) {
                error(element, "@Argument can only be used on fragment fields (%s.%s)",
                        enclosingElement.getQualifiedName(), element);
                continue;
            }

            if (element.getModifiers().contains(Modifier.FINAL) || element.getModifiers().contains(Modifier.STATIC)
                    || element.getModifiers().contains(Modifier.PRIVATE)
                    || element.getModifiers().contains(Modifier.PROTECTED)) {
                error(element, "@Argument fields must not be private, protected, final or static (%s.%s)",
                        enclosingElement.getQualifiedName(), element);
                continue;
            }
            Set<Element> fields = fieldsByType.get(enclosingElement);
            if (fields == null) {
                fields = new LinkedHashSet<>(10);
                fieldsByType.put(enclosingElement, fields);
            }
            fields.add(element);
        }

        for (Entry<TypeElement, Set<Element>> entry : fieldsByType.entrySet()) {
            try {
                String builder = entry.getKey().getSimpleName() + "Builder";
                List<Element> originating = new ArrayList<>(10);
                originating.add(entry.getKey());
                TypeMirror superClass = entry.getKey().getSuperclass();
                while (superClass.getKind() != TypeKind.NONE) {
                    TypeElement element = (TypeElement) typeUtils.asElement(superClass);
                    if (element.getQualifiedName().toString().startsWith("android.")) {
                        break;
                    }
                    originating.add(element);
                    superClass = element.getSuperclass();
                }
                JavaFileObject jfo = filer.createSourceFile(entry.getKey().getQualifiedName() + "Builder",
                        originating.toArray(new Element[originating.size()]));
                Writer writer = jfo.openWriter();
                JavaWriter jw = new JavaWriter(writer);
                writePackage(jw, entry.getKey());
                jw.emitImports("android.os.Bundle");

                Set<ArgumentAnnotatedField> required = collectArgumentsForType(typeUtils, entry.getKey(), fieldsByType, true,
                        true);
                Set<ArgumentAnnotatedField> allArguments = collectArgumentsForType(typeUtils, entry.getKey(), fieldsByType,
                        false, true);

                if (defaultNonNull) {
                    for (ArgumentAnnotatedField arg : allArguments) {
                        if (markAsNonNullDefault(arg)) {
                            jw.emitImports("android.support.annotation.NonNull");
                            break;
                        }
                    }
                }

                jw.beginType(builder, "class", EnumSet.of(Modifier.PUBLIC, Modifier.FINAL));
                jw.emitField("Bundle", "mArguments", EnumSet.of(Modifier.PRIVATE, Modifier.FINAL), "new Bundle()");
                jw.emitEmptyLine();

                String[] args = new String[required.size() * 2];
                int index = 0;
                for (ArgumentAnnotatedField arg : required) {
                    args[index++] = getArgumentAnnotations(arg, defaultNonNull) + arg.getType();
                    args[index++] = arg.getVariableName();
                }
                jw.beginMethod(null, builder, EnumSet.of(Modifier.PUBLIC), args);

                for (ArgumentAnnotatedField arg : required) {
                    writePutArguments(jw, arg.getVariableName(), "mArguments", arg, arg.hasNonNullAnnotation() || defaultNonNull);
                }

                jw.endMethod();

                if (!required.isEmpty()) {
                    writeNewFragmentWithRequiredMethod(builder, entry.getKey(), jw, args);
                }

                Set<ArgumentAnnotatedField> optionalArguments = new HashSet<>(allArguments);
                optionalArguments.removeAll(required);

                for (ArgumentAnnotatedField arg : optionalArguments) {
                    writeBuilderMethod(builder, jw, arg, defaultNonNull);
                }

                writeInjectMethod(jw, entry.getKey(),
                        collectArgumentsForType(typeUtils, entry.getKey(), fieldsByType, false, false));
                writeBuildMethod(jw, entry.getKey());
                writeBuildSubclassMethod(jw, entry.getKey());
                jw.endType();
                jw.close();
            } catch (IOException e) {
                error(entry.getKey(), "Unable to write builder for type %s: %s", entry.getKey(), e.getMessage());
                throw new RuntimeException(e);
            }
        }

        return true;
    }

    private boolean markAsNonNullDefault(ArgumentAnnotatedField arg) {
        return !arg.hasNullableAnnotation() && !arg.hasNonNullAnnotation() && !arg.getElement().asType().getKind().isPrimitive();
    }

    private String getArgumentAnnotations(ArgumentAnnotatedField arg, boolean defaultNonNull) {
        StringBuilder argumentAnnotations = new StringBuilder();
        List<AnnotationMirror> annotations = arg.getSourceAnnotations();
        for (AnnotationMirror am : annotations) {
            argumentAnnotations.append("@").
                    append(am.getAnnotationType().asElement().toString()).
                    append(" ");

        }
        if (defaultNonNull && markAsNonNullDefault(arg)) {
            argumentAnnotations.append("@NonNull ");
        }
        return argumentAnnotations.toString();
    }

    private void writeNewFragmentWithRequiredMethod(String builder, TypeElement element, JavaWriter jw, String[] args)
            throws IOException {
        jw.beginMethod(element.getQualifiedName().toString(), "new" + element.getSimpleName(),
                EnumSet.of(Modifier.STATIC, Modifier.PUBLIC), args);
        StringBuilder argNames = new StringBuilder();
        for (int i = 1; i < args.length; i += 2) {
            argNames.append(args[i]);
            if (i < args.length - 1) {
                argNames.append(", ");
            }
        }
        jw.emitStatement("return new %1$s(%2$s).build()", builder, argNames);
        jw.endMethod();
    }

    private void writeBuildMethod(JavaWriter jw, TypeElement element) throws IOException {
        jw.beginMethod(element.getSimpleName().toString(), "build", EnumSet.of(Modifier.PUBLIC));
        jw.emitStatement("%1$s fragment = new %1$s()", element.getSimpleName().toString());
        jw.emitStatement("fragment.setArguments(mArguments)");
        jw.emitStatement("return fragment");
        jw.endMethod();
    }

    private void writeBuildSubclassMethod(JavaWriter jw, TypeElement element) throws IOException {
        jw.beginMethod("<F extends " + element.getSimpleName().toString() + "> F", "build", EnumSet.of(Modifier.PUBLIC), "F", "fragment");
        jw.emitStatement("fragment.setArguments(mArguments)");
        jw.emitStatement("return fragment");
        jw.endMethod();
    }

    private void writeInjectMethod(JavaWriter jw, TypeElement element, Set<ArgumentAnnotatedField> allArguments)
            throws IOException {
        jw.beginMethod("void", "injectArguments", EnumSet.of(Modifier.STATIC, Modifier.FINAL),
                element.getSimpleName().toString(), "fragment");

        jw.emitStatement("Bundle args = fragment.getArguments()");
        jw.beginControlFlow("if (args == null)");
        jw.emitStatement("throw new IllegalStateException(\"No arguments set\")");
        jw.endControlFlow();

        for (AnnotatedField type : allArguments) {
            String op = getOperation(type);
            if (op == null) {
                error(element, "Can't write injector, the bundle getter is unknown");
                return;
            }
            String cast = "Serializable".equals(op) ? "(" + type.getType() + ") " : "";
            if (!type.isRequired()) {
                jw.beginControlFlow("if (args.containsKey("+JavaWriter.stringLiteral(type.getKey())+"))");
            } else {
                jw.beginControlFlow("if (!args.containsKey("+JavaWriter.stringLiteral(type.getKey())+"))");
                jw.emitStatement("throw new IllegalStateException(\"required argument %1$s is not set\")", type.getKey());
                jw.endControlFlow();
            }

            jw.emitStatement("fragment.%1$s = %4$sargs.get%2$s(\"%3$s\")", type.getName(), op, type.getKey(), cast);

            if (!type.isRequired()) {
                jw.endControlFlow();
            }
        }
        jw.endMethod();
    }

    private void writeBuilderMethod(String type, JavaWriter writer, ArgumentAnnotatedField arg, boolean defaultNonNull) throws IOException {
        writer.emitEmptyLine();
        writer.beginMethod(type, arg.getVariableName(), EnumSet.of(Modifier.PUBLIC), getArgumentAnnotations(arg, defaultNonNull) + arg.getType(),
                arg.getVariableName());
        writePutArguments(writer, arg.getVariableName(), "mArguments", arg, arg.hasNonNullAnnotation() || defaultNonNull);
        writer.emitStatement("return this");
        writer.endMethod();
    }
}
