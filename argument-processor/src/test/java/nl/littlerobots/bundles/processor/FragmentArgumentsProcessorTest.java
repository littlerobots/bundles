package nl.littlerobots.bundles.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;


public class FragmentArgumentsProcessorTest {

    @Test
    public void testFrameworkFragment() {
        Compilation compilation = javac().
                withProcessors(new FragmentArgumentsProcessor()).
                compile(JavaFileObjects.forSourceLines("nl.littlerobots.test.TestFragment", "package nl.littlerobots.test;\n" +
                        "\n" +
                        "import android.app.Fragment;\n" +
                        "import nl.littlerobots.bundles.annotation.Argument;\n" +
                        "\n" +
                        "public class TestFragment extends Fragment {\n" +
                        "    @Argument\n" +
                        "    String mTestStringArgument;\n" +
                        "}\n"));
        assertThat(compilation).succeeded();
    }

    @Test
    public void testSupportFragment() {
        Compilation compilation = javac().
                withProcessors(new FragmentArgumentsProcessor()).
                compile(JavaFileObjects.forSourceLines("nl.littlerobots.test.TestFragment", "package nl.littlerobots.test;\n" +
                        "\n" +
                        "import android.support.v4.app.Fragment;\n" +
                        "import nl.littlerobots.bundles.annotation.Argument;\n" +
                        "\n" +
                        "public class TestFragment extends Fragment {\n" +
                        "    @Argument\n" +
                        "    String mTestStringArgument;\n" +
                        "}\n"));
        assertThat(compilation).succeeded();
    }

    @Test
    public void testRequiredArgument() {
        Compilation compilation = javac().
                withProcessors(new FragmentArgumentsProcessor()).
                compile(JavaFileObjects.forSourceLines("nl.littlerobots.test.TestFragment", "package nl.littlerobots.test;\n" +
                        "\n" +
                        "import android.app.Fragment;\n" +
                        "import nl.littlerobots.bundles.annotation.Argument;\n" +
                        "\n" +
                        "public class TestFragment extends Fragment {\n" +
                        "    @Argument\n" +
                        "    String mTestStringArgument;\n" +
                        "}\n"));
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("nl.littlerobots.test.TestFragmentBuilder").
                hasSourceEquivalentTo(JavaFileObjects.forSourceString("nl.littlerobots.test.TestFragmentBuilder", "package nl.littlerobots.test;\n" +
                        "\n" +
                        "import android.os.Bundle;\n" +
                        "import android.support.annotation.NonNull;\n" +
                        "public final class TestFragmentBuilder {\n" +
                        "  private final Bundle mArguments = new Bundle();\n" +
                        "\n" +
                        "  public TestFragmentBuilder(@NonNull String testStringArgument) {\n" +
                        "    if (testStringArgument == null) {\n" +
                        "      throw new IllegalStateException(\"testStringArgument must not be null\");\n" +
                        "    }\n" +
                        "    mArguments.putString(\"testStringArgument\", testStringArgument);\n" +
                        "  }\n" +
                        "  public static TestFragment newTestFragment(@NonNull String testStringArgument) {\n" +
                        "    return new TestFragmentBuilder(testStringArgument).build();\n" +
                        "  }\n" +
                        "  static final void injectArguments(TestFragment fragment) {\n" +
                        "    Bundle args = fragment.getArguments();\n" +
                        "    if (args == null) {\n" +
                        "      throw new IllegalStateException(\"No arguments set\");\n" +
                        "    }\n" +
                        "    boolean containsKey;\n" +
                        "    containsKey = args.containsKey(\"testStringArgument\");\n" +
                        "    if (!containsKey) {\n" +
                        "      throw new IllegalStateException(\"required argument testStringArgument is not set\");\n" +
                        "    }\n" +
                        "    if (args.getString(\"testStringArgument\") == null) {\n" +
                        "      throw new IllegalStateException(\"testStringArgument must not be null\");\n" +
                        "    }\n" +
                        "    fragment.mTestStringArgument = args.getString(\"testStringArgument\");\n" +
                        "  }\n" +
                        "  public TestFragment build() {\n" +
                        "    TestFragment fragment = new TestFragment();\n" +
                        "    fragment.setArguments(mArguments);\n" +
                        "    return fragment;\n" +
                        "  }\n" +
                        "  public <F extends TestFragment> F build(F fragment) {\n" +
                        "    fragment.setArguments(mArguments);\n" +
                        "    return fragment;\n" +
                        "  }\n" +
                        "}\n"));
    }

    @Test
    public void testDefaultNullableOption() {
        Compilation compilation = javac().
                withProcessors(new FragmentArgumentsProcessor()).
                withOptions("-Aargument.default.nullable=true").
                compile(JavaFileObjects.forSourceLines("nl.littlerobots.test.TestFragment", "package nl.littlerobots.test;\n" +
                        "\n" +
                        "import android.app.Fragment;\n" +
                        "import nl.littlerobots.bundles.annotation.Argument;\n" +
                        "\n" +
                        "public class TestFragment extends Fragment {\n" +
                        "    @Argument\n" +
                        "    String mTestStringArgument;\n" +
                        "}\n"));
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("nl.littlerobots.test.TestFragmentBuilder").
                hasSourceEquivalentTo(JavaFileObjects.forSourceString("nl.littlerobots.test.TestFragmentBuilder", "package nl.littlerobots.test;\n" +
                        "\n" +
                        "import android.os.Bundle;\n" +
                        "public final class TestFragmentBuilder {\n" +
                        "  private final Bundle mArguments = new Bundle();\n" +
                        "\n" +
                        "  public TestFragmentBuilder(String testStringArgument) {\n" +
                        "    mArguments.putString(\"testStringArgument\", testStringArgument);\n" +
                        "  }\n" +
                        "  public static TestFragment newTestFragment(String testStringArgument) {\n" +
                        "    return new TestFragmentBuilder(testStringArgument).build();\n" +
                        "  }\n" +
                        "  static final void injectArguments(TestFragment fragment) {\n" +
                        "    Bundle args = fragment.getArguments();\n" +
                        "    if (args == null) {\n" +
                        "      throw new IllegalStateException(\"No arguments set\");\n" +
                        "    }\n" +
                        "    boolean containsKey;\n" +
                        "    containsKey = args.containsKey(\"testStringArgument\");\n" +
                        "    if (!containsKey) {\n" +
                        "      throw new IllegalStateException(\"required argument testStringArgument is not set\");\n" +
                        "    }\n" +
                        "    fragment.mTestStringArgument = args.getString(\"testStringArgument\");\n" +
                        "  }\n" +
                        "  public TestFragment build() {\n" +
                        "    TestFragment fragment = new TestFragment();\n" +
                        "    fragment.setArguments(mArguments);\n" +
                        "    return fragment;\n" +
                        "  }\n" +
                        "  public <F extends TestFragment> F build(F fragment) {\n" +
                        "    fragment.setArguments(mArguments);\n" +
                        "    return fragment;\n" +
                        "  }\n" +
                        "}\n"));
    }

    @Test
    public void testOptionalArgument() {
        Compilation compilation = javac().
                withProcessors(new FragmentArgumentsProcessor()).
                compile(JavaFileObjects.forSourceLines("nl.littlerobots.test.TestFragment", "package nl.littlerobots.test;\n" +
                        "\n" +
                        "import android.app.Fragment;\n" +
                        "import nl.littlerobots.bundles.annotation.Argument;\n" +
                        "\n" +
                        "public class TestFragment extends Fragment {\n" +
                        "    @Argument(required=false)\n" +
                        "    String mTestStringArgument;\n" +
                        "}\n"));
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("nl.littlerobots.test.TestFragmentBuilder").
                hasSourceEquivalentTo(JavaFileObjects.forSourceString("nl.littlerobots.test.TestFragmentBuilder", "package nl.littlerobots.test;\n" +
                        "\n" +
                        "import android.os.Bundle;\n" +
                        "import android.support.annotation.NonNull;\n" +
                        "public final class TestFragmentBuilder {\n" +
                        "  private final Bundle mArguments = new Bundle();\n" +
                        "\n" +
                        "  public TestFragmentBuilder() {\n" +
                        "  }\n" +
                        "\n" +
                        "  public TestFragmentBuilder testStringArgument(@NonNull String testStringArgument) {\n" +
                        "    if (testStringArgument == null) {\n" +
                        "      throw new IllegalStateException(\"testStringArgument must not be null\");\n" +
                        "    }\n" +
                        "    mArguments.putString(\"testStringArgument\", testStringArgument);\n" +
                        "    return this;\n" +
                        "  }\n" +
                        "  static final void injectArguments(TestFragment fragment) {\n" +
                        "    Bundle args = fragment.getArguments();\n" +
                        "    if (args == null) {\n" +
                        "      throw new IllegalStateException(\"No arguments set\");\n" +
                        "    }\n" +
                        "    boolean containsKey;\n" +
                        "    containsKey = args.containsKey(\"testStringArgument\");\n" +
                        "    if (containsKey) {\n" +
                        "      if (args.getString(\"testStringArgument\") == null) {\n" +
                        "        throw new IllegalStateException(\"testStringArgument must not be null\");\n" +
                        "      }\n" +
                        "      fragment.mTestStringArgument = args.getString(\"testStringArgument\");\n" +
                        "    }\n" +
                        "  }\n" +
                        "  public TestFragment build() {\n" +
                        "    TestFragment fragment = new TestFragment();\n" +
                        "    fragment.setArguments(mArguments);\n" +
                        "    return fragment;\n" +
                        "  }\n" +
                        "  public <F extends TestFragment> F build(F fragment) {\n" +
                        "    fragment.setArguments(mArguments);\n" +
                        "    return fragment;\n" +
                        "  }\n" +
                        "}"));
    }

    @Test
    public void testCopyNullableAnnotationToBuilder() {
        Compilation compilation = javac().
                withProcessors(new FragmentArgumentsProcessor()).
                compile(JavaFileObjects.forSourceLines("nl.littlerobots.test.TestFragment", "package nl.littlerobots.test;\n" +
                        "\n" +
                        "import android.app.Fragment;\n" +
                        "import nl.littlerobots.bundles.annotation.Argument;\n" +
                        "\n" +
                        "public class TestFragment extends Fragment {\n" +
                        "    @Argument\n" +
                        "    @Nullable\n" +
                        "    String mTestStringArgument;\n" +
                        "    @Argument(required=false)\n" +
                        "    @Nullable\n" +
                        "    String mTestStringArgument2;\n" +
                        "}\n"));
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("nl.littlerobots.test.TestFragmentBuilder").
                hasSourceEquivalentTo(JavaFileObjects.forSourceString("nl.littlerobots.test.TestFragmentBuilder", "package nl.littlerobots.test;\n" +
                        "\n" +
                        "import android.os.Bundle;\n" +
                        "public final class TestFragmentBuilder {\n" +
                        "  private final Bundle mArguments = new Bundle();\n" +
                        "\n" +
                        "  public TestFragmentBuilder(@Nullable String testStringArgument) {\n" +
                        "    if (testStringArgument == null) {\n" +
                        "      throw new IllegalStateException(\"testStringArgument must not be null\");\n" +
                        "    }\n" +
                        "    mArguments.putString(\"testStringArgument\", testStringArgument);\n" +
                        "  }\n" +
                        "  public static TestFragment newTestFragment(@Nullable String testStringArgument) {\n" +
                        "    return new TestFragmentBuilder(testStringArgument).build();\n" +
                        "  }\n" +
                        "\n" +
                        "  public TestFragmentBuilder testStringArgument2(@Nullable String testStringArgument2) {\n" +
                        "    if (testStringArgument2 == null) {\n" +
                        "      throw new IllegalStateException(\"testStringArgument2 must not be null\");\n" +
                        "    }\n" +
                        "    mArguments.putString(\"testStringArgument2\", testStringArgument2);\n" +
                        "    return this;\n" +
                        "  }\n" +
                        "  static final void injectArguments(TestFragment fragment) {\n" +
                        "    Bundle args = fragment.getArguments();\n" +
                        "    if (args == null) {\n" +
                        "      throw new IllegalStateException(\"No arguments set\");\n" +
                        "    }\n" +
                        "    boolean containsKey;\n" +
                        "    containsKey = args.containsKey(\"testStringArgument\");\n" +
                        "    if (!containsKey) {\n" +
                        "      throw new IllegalStateException(\"required argument testStringArgument is not set\");\n" +
                        "    }\n" +
                        "    fragment.mTestStringArgument = args.getString(\"testStringArgument\");\n" +
                        "    containsKey = args.containsKey(\"testStringArgument2\");\n" +
                        "    if (containsKey) {\n" +
                        "      fragment.mTestStringArgument2 = args.getString(\"testStringArgument2\");\n" +
                        "    }\n" +
                        "  }\n" +
                        "  public TestFragment build() {\n" +
                        "    TestFragment fragment = new TestFragment();\n" +
                        "    fragment.setArguments(mArguments);\n" +
                        "    return fragment;\n" +
                        "  }\n" +
                        "  public <F extends TestFragment> F build(F fragment) {\n" +
                        "    fragment.setArguments(mArguments);\n" +
                        "    return fragment;\n" +
                        "  }\n" +
                        "}\n"));
    }

    @Test
    public void testCopyNonNullAnnotationToBuilder() {
        Compilation compilation = javac().
                withProcessors(new FragmentArgumentsProcessor()).
                compile(JavaFileObjects.forSourceLines("nl.littlerobots.test.TestFragment", "package nl.littlerobots.test;\n" +
                        "\n" +
                        "import android.app.Fragment;\n" +
                        "import nl.littlerobots.bundles.annotation.Argument;\n" +
                        "\n" +
                        "public class TestFragment extends Fragment {\n" +
                        "    @Argument\n" +
                        "    String mTestStringArgument;\n" +
                        "    @Argument(required=false)\n" +
                        "    @NonNull\n" +
                        "    int mTestPrimitive;\n" +
                        "    @Argument\n" +
                        "    @NonNull\n" +
                        "    Integer mTestInteger;\n" +
                        "}\n"));
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("nl.littlerobots.test.TestFragmentBuilder").
                hasSourceEquivalentTo(JavaFileObjects.forSourceString("nl.littlerobots.test.TestFragmentBuilder", "package nl.littlerobots.test;\n" +
                        "\n" +
                        "import android.os.Bundle;\n" +
                        "import android.support.annotation.NonNull;\n" +
                        "public final class TestFragmentBuilder {\n" +
                        "  private final Bundle mArguments = new Bundle();\n" +
                        "\n" +
                        "  public TestFragmentBuilder(@nl.littlerobots.test.NonNull Integer testInteger, @NonNull String testStringArgument) {\n" +
                        "    if (testInteger == null) {\n" +
                        "      throw new IllegalStateException(\"testInteger must not be null\");\n" +
                        "    }\n" +
                        "    mArguments.putInt(\"testInteger\", testInteger);\n" +
                        "    if (testStringArgument == null) {\n" +
                        "      throw new IllegalStateException(\"testStringArgument must not be null\");\n" +
                        "    }\n" +
                        "    mArguments.putString(\"testStringArgument\", testStringArgument);\n" +
                        "  }\n" +
                        "  public static TestFragment newTestFragment(@nl.littlerobots.test.NonNull Integer testInteger, @NonNull String testStringArgument) {\n" +
                        "    return new TestFragmentBuilder(testInteger, testStringArgument).build();\n" +
                        "  }\n" +
                        "\n" +
                        "  public TestFragmentBuilder testPrimitive(@nl.littlerobots.test.NonNull int testPrimitive) {\n" +
                        "    mArguments.putInt(\"testPrimitive\", testPrimitive);\n" +
                        "    return this;\n" +
                        "  }\n" +
                        "  static final void injectArguments(TestFragment fragment) {\n" +
                        "    Bundle args = fragment.getArguments();\n" +
                        "    if (args == null) {\n" +
                        "      throw new IllegalStateException(\"No arguments set\");\n" +
                        "    }\n" +
                        "    boolean containsKey;\n" +
                        "    containsKey = args.containsKey(\"testInteger\");\n" +
                        "    if (!containsKey) {\n" +
                        "      throw new IllegalStateException(\"required argument testInteger is not set\");\n" +
                        "    }\n" +
                        "    fragment.mTestInteger = args.getInt(\"testInteger\");\n" +
                        "    containsKey = args.containsKey(\"testPrimitive\");\n" +
                        "    if (containsKey) {\n" +
                        "      fragment.mTestPrimitive = args.getInt(\"testPrimitive\");\n" +
                        "    }\n" +
                        "    containsKey = args.containsKey(\"testStringArgument\");\n" +
                        "    if (!containsKey) {\n" +
                        "      throw new IllegalStateException(\"required argument testStringArgument is not set\");\n" +
                        "    }\n" +
                        "    if (args.getString(\"testStringArgument\") == null) {\n" +
                        "      throw new IllegalStateException(\"testStringArgument must not be null\");\n" +
                        "    }\n" +
                        "    fragment.mTestStringArgument = args.getString(\"testStringArgument\");\n" +
                        "  }\n" +
                        "  public TestFragment build() {\n" +
                        "    TestFragment fragment = new TestFragment();\n" +
                        "    fragment.setArguments(mArguments);\n" +
                        "    return fragment;\n" +
                        "  }\n" +
                        "  public <F extends TestFragment> F build(F fragment) {\n" +
                        "    fragment.setArguments(mArguments);\n" +
                        "    return fragment;\n" +
                        "  }\n" +
                        "}"));
    }

    @Test
    public void testProperNullCheckForPrimitiveBundleGetters() {
        Compilation compilation = javac().
                withProcessors(new FragmentArgumentsProcessor()).
                compile(JavaFileObjects.forSourceLines("nl.littlerobots.test.TestFragment", "package nl.littlerobots.test;\n" +
                        "\n" +
                        "import android.app.Fragment;\n" +
                        "import nl.littlerobots.bundles.annotation.Argument;\n" +
                        "\n" +
                        "public class TestFragment extends Fragment {\n" +
                        "    @Argument\n" +
                        "    Integer mTestPrimitive;\n" +
                        "}\n"));
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("nl.littlerobots.test.TestFragmentBuilder").
                hasSourceEquivalentTo(JavaFileObjects.forSourceString("nl.littlerobots.test.TestFragmentBuilder", "package nl.littlerobots.test;\n" +
                        "\n" +
                        "import android.os.Bundle;\n" +
                        "import android.support.annotation.NonNull;\n" +
                        "public final class TestFragmentBuilder {\n" +
                        "  private final Bundle mArguments = new Bundle();\n" +
                        "\n" +
                        "  public TestFragmentBuilder(@NonNull Integer testPrimitive) {\n" +
                        "    if (testPrimitive == null) {\n" +
                        "      throw new IllegalStateException(\"testPrimitive must not be null\");\n" +
                        "    }\n" +
                        "    mArguments.putInt(\"testPrimitive\", testPrimitive);\n" +
                        "  }\n" +
                        "  public static TestFragment newTestFragment(@NonNull Integer testPrimitive) {\n" +
                        "    return new TestFragmentBuilder(testPrimitive).build();\n" +
                        "  }\n" +
                        "  static final void injectArguments(TestFragment fragment) {\n" +
                        "    Bundle args = fragment.getArguments();\n" +
                        "    if (args == null) {\n" +
                        "      throw new IllegalStateException(\"No arguments set\");\n" +
                        "    }\n" +
                        "    boolean containsKey;\n" +
                        "    containsKey = args.containsKey(\"testPrimitive\");\n" +
                        "    if (!containsKey) {\n" +
                        "      throw new IllegalStateException(\"required argument testPrimitive is not set\");\n" +
                        "    }\n" +
                        "    fragment.mTestPrimitive = args.getInt(\"testPrimitive\");\n" +
                        "  }\n" +
                        "  public TestFragment build() {\n" +
                        "    TestFragment fragment = new TestFragment();\n" +
                        "    fragment.setArguments(mArguments);\n" +
                        "    return fragment;\n" +
                        "  }\n" +
                        "  public <F extends TestFragment> F build(F fragment) {\n" +
                        "    fragment.setArguments(mArguments);\n" +
                        "    return fragment;\n" +
                        "  }\n" +
                        "}"));
    }

    @Test
    public void testUseSetterForProperties() {
        Compilation compilation = javac().
                withProcessors(new FragmentArgumentsProcessor()).
                compile(JavaFileObjects.forSourceLines("nl.littlerobots.test.TestFragment", "package nl.littlerobots.test;\n" +
                        "\n" +
                        "import android.app.Fragment;\n" +
                        "import nl.littlerobots.bundles.annotation.Argument;\n" +
                        "\n" +
                        "public class TestFragment extends Fragment {\n" +
                        "    @Argument\n" +
                        "    private Integer mTestPrimitive;\n" +
                        "    public final void setTestPrimitive(Integer value) {};" +
                        "}\n"));
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("nl.littlerobots.test.TestFragmentBuilder").
                hasSourceEquivalentTo(JavaFileObjects.forSourceString("nl.littlerobots.test.TestFragmentBuilder", "package nl.littlerobots.test;\n" +
                        "\n" +
                        "import android.os.Bundle;\n" +
                        "import android.support.annotation.NonNull;\n" +
                        "public final class TestFragmentBuilder {\n" +
                        "  private final Bundle mArguments = new Bundle();\n" +
                        "\n" +
                        "  public TestFragmentBuilder(@NonNull Integer testPrimitive) {\n" +
                        "    if (testPrimitive == null) {\n" +
                        "      throw new IllegalStateException(\"testPrimitive must not be null\");\n" +
                        "    }\n" +
                        "    mArguments.putInt(\"testPrimitive\", testPrimitive);\n" +
                        "  }\n" +
                        "  public static TestFragment newTestFragment(@NonNull Integer testPrimitive) {\n" +
                        "    return new TestFragmentBuilder(testPrimitive).build();\n" +
                        "  }\n" +
                        "  static final void injectArguments(TestFragment fragment) {\n" +
                        "    Bundle args = fragment.getArguments();\n" +
                        "    if (args == null) {\n" +
                        "      throw new IllegalStateException(\"No arguments set\");\n" +
                        "    }\n" +
                        "    boolean containsKey;\n" +
                        "    containsKey = args.containsKey(\"testPrimitive\");\n" +
                        "    if (!containsKey) {\n" +
                        "      throw new IllegalStateException(\"required argument testPrimitive is not set\");\n" +
                        "    }\n" +
                        "    fragment.setTestPrimitive(args.getInt(\"testPrimitive\"));\n" +
                        "  }\n" +
                        "  public TestFragment build() {\n" +
                        "    TestFragment fragment = new TestFragment();\n" +
                        "    fragment.setArguments(mArguments);\n" +
                        "    return fragment;\n" +
                        "  }\n" +
                        "  public <F extends TestFragment> F build(F fragment) {\n" +
                        "    fragment.setArguments(mArguments);\n" +
                        "    return fragment;\n" +
                        "  }\n" +
                        "}"));
    }
}
