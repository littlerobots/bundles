package com.neenbedankt.bundles.processor;

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
                        "import com.neenbedankt.bundles.annotation.Argument;\n" +
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
                        "import com.neenbedankt.bundles.annotation.Argument;\n" +
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
                        "import com.neenbedankt.bundles.annotation.Argument;\n" +
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
                        "    if (!args.containsKey(\"testStringArgument\")) {\n" +
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
                        "}"));
    }

    @Test
    public void testOptionaldArgument() {
        Compilation compilation = javac().
                withProcessors(new FragmentArgumentsProcessor()).
                compile(JavaFileObjects.forSourceLines("nl.littlerobots.test.TestFragment", "package nl.littlerobots.test;\n" +
                        "\n" +
                        "import android.app.Fragment;\n" +
                        "import com.neenbedankt.bundles.annotation.Argument;\n" +
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
                        "public final class TestFragmentBuilder {\n" +
                        "  private final Bundle mArguments = new Bundle();\n" +
                        "\n" +
                        "  public TestFragmentBuilder() {\n" +
                        "  }\n" +
                        "\n" +
                        "  public TestFragmentBuilder testStringArgument(String testStringArgument) {\n" +
                        "    mArguments.putString(\"testStringArgument\", testStringArgument);\n" +
                        "    return this;\n" +
                        "  }\n" +
                        "  static final void injectArguments(TestFragment fragment) {\n" +
                        "    Bundle args = fragment.getArguments();\n" +
                        "    if (args == null) {\n" +
                        "      throw new IllegalStateException(\"No arguments set\");\n" +
                        "    }\n" +
                        "    if (args.containsKey(\"testStringArgument\")) {\n" +
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
}
