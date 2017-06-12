package nl.littlerobots.bundles.processor;

import nl.littlerobots.bundles.internal.com.squareup.javawriter.JavaWriter;

import javax.annotation.processing.AbstractProcessor;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class BaseProcessor extends AbstractProcessor {

    private static final Map<String, String> ARGUMENT_TYPES = new HashMap<>(20);

    static {
        ARGUMENT_TYPES.put("java.lang.String", "String");
        ARGUMENT_TYPES.put("int", "Int");
        ARGUMENT_TYPES.put("java.lang.Integer", "Int");
        ARGUMENT_TYPES.put("long", "Long");
        ARGUMENT_TYPES.put("java.lang.Long", "Long");
        ARGUMENT_TYPES.put("double", "Double");
        ARGUMENT_TYPES.put("java.lang.Double", "Double");
        ARGUMENT_TYPES.put("short", "Short");
        ARGUMENT_TYPES.put("java.lang.Short", "Short");
        ARGUMENT_TYPES.put("float", "Float");
        ARGUMENT_TYPES.put("java.lang.Float", "Float");
        ARGUMENT_TYPES.put("byte", "Byte");
        ARGUMENT_TYPES.put("java.lang.Byte", "Byte");
        ARGUMENT_TYPES.put("boolean", "Boolean");
        ARGUMENT_TYPES.put("java.lang.Boolean", "Boolean");
        ARGUMENT_TYPES.put("char", "Char");
        ARGUMENT_TYPES.put("java.lang.Character", "Char");
        ARGUMENT_TYPES.put("java.lang.CharSequence", "CharSequence");
        ARGUMENT_TYPES.put("android.os.Bundle", "Bundle");
        ARGUMENT_TYPES.put("android.os.Parcelable", "Parcelable");
    }

    protected void error(Element element, String message, Object... args) {
        processingEnv.getMessager().printMessage(Kind.ERROR, String.format(message, args), element);
    }

    protected String getOperation(AnnotatedField arg) {
        String op = ARGUMENT_TYPES.get(arg.getRawType());
        if (op != null) {
            if (arg.isArray()) {
                return op + "Array";
            } else {
                return op;
            }
        }

        Elements elements = processingEnv.getElementUtils();
        TypeMirror type = arg.getElement().asType();
        Types types = processingEnv.getTypeUtils();
        String[] arrayListTypes = new String[]{String.class.getName(), Integer.class.getName(), CharSequence.class.getName()};
        String[] arrayListOps = new String[]{"StringArrayList", "IntegerArrayList", "CharSequenceArrayList"};
        for (int i = 0; i < arrayListTypes.length; i++) {
            TypeMirror tm = getArrayListType(arrayListTypes[i]);
            if (types.isAssignable(type, tm)) {
                return arrayListOps[i];
            }
        }

        if (types.isAssignable(type, getWildcardType(ArrayList.class.getName(), "android.os.Parcelable"))) {
            return "ParcelableArrayList";
        }
        TypeMirror sparseParcelableArray = getWildcardType("android.util.SparseArray", "android.os.Parcelable");

        if (types.isAssignable(type, sparseParcelableArray)) {
            return "SparseParcelableArray";
        }

        if (types.isAssignable(type, elements.getTypeElement(Serializable.class.getName()).asType())) {
            return "Serializable";
        }

        if (types.isAssignable(type, elements.getTypeElement("android.os.Parcelable").asType())) {
            return "Parcelable";
        }

        return null;
    }

    private TypeMirror getWildcardType(String type, String elementType) {
        TypeElement arrayList = processingEnv.getElementUtils().getTypeElement(type);
        TypeMirror elType = processingEnv.getElementUtils().getTypeElement(elementType).asType();
        return processingEnv.getTypeUtils().getDeclaredType(arrayList, processingEnv.getTypeUtils().getWildcardType(elType, null));
    }

    private TypeMirror getArrayListType(String elementType) {
        TypeElement arrayList = processingEnv.getElementUtils().getTypeElement("java.util.ArrayList");
        TypeMirror elType = processingEnv.getElementUtils().getTypeElement(elementType).asType();
        return processingEnv.getTypeUtils().getDeclaredType(arrayList, elType);
    }

    protected void writePutArguments(JavaWriter jw, String sourceVariable, String bundleVariable, AnnotatedField arg, boolean nullCheck) throws IOException {
        String op = getOperation(arg);

        if (op == null) {
            error(arg.getElement(), "Don't know how to put %s in a Bundle", arg.getElement().asType().toString());
            return;
        }
        if ("Serializable".equals(op)) {
            processingEnv.getMessager().printMessage(Kind.WARNING, String.format("%1$s will be stored as Serializable", arg.getName()), arg.getElement());
        }

        if (nullCheck && !arg.getElement().asType().getKind().isPrimitive()) {
            jw.beginControlFlow("if (" + sourceVariable + " == null)");
            jw.emitStatement("throw new IllegalStateException(\"%1$s must not be null\")", arg.getKey());
            jw.endControlFlow();
        }
        jw.emitStatement("%4$s.put%1$s(\"%2$s\", %3$s)", op, arg.getKey(), sourceVariable, bundleVariable);
    }


    protected void writePackage(JavaWriter jw, TypeElement type) throws IOException {
        PackageElement pkg = processingEnv.getElementUtils().getPackageOf(type);
        if (!pkg.isUnnamed()) {
            jw.emitPackage(pkg.getQualifiedName().toString());
        } else {
            jw.emitPackage("");
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    protected boolean hasSetter(Element element) {
        return findSetter(element) != null;
    }

    protected boolean hasGetter(Element element) {
        return findGetter(element) != null;
    }

    protected Element findSetter(Element element) {
        String name = "set" + uppercaseFirstLetter(element.getSimpleName().toString());
        String varName = "set" + uppercaseFirstLetter(AnnotatedField.getVariableName(element.getSimpleName().toString()));


        List<? extends Element> elements = processingEnv.getElementUtils().getAllMembers((TypeElement) element.getEnclosingElement());
        for (Element e : elements) {
            if (e.getKind() == ElementKind.METHOD && (e.getSimpleName().toString().equals(name) || e.getSimpleName().toString().equals(varName))) {
                ExecutableElement method = (ExecutableElement) e;
                if (method.getParameters().size() == 1 && method.getParameters().get(0).asType() == element.asType()) {
                    return e;
                }
            }
        }
        return null;
    }

    protected Element findGetter(Element element) {
        String name = "get" + uppercaseFirstLetter(element.getSimpleName().toString());
        String varName = "get" + uppercaseFirstLetter(AnnotatedField.getVariableName(element.getSimpleName().toString()));


        List<? extends Element> elements = processingEnv.getElementUtils().getAllMembers((TypeElement) element.getEnclosingElement());
        for (Element e : elements) {
            if (e.getKind() == ElementKind.METHOD && (e.getSimpleName().toString().equals(name) || e.getSimpleName().toString().equals(varName))) {
                ExecutableElement method = (ExecutableElement) e;
                if (method.getParameters().size() == 0 && method.getReturnType() == element.asType()) {
                    return e;
                }
            }
        }
        return null;
    }

    private String uppercaseFirstLetter(String name) {
        if (name.length() == 1) {
            return name.toUpperCase();
        } else {
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
    }
}
