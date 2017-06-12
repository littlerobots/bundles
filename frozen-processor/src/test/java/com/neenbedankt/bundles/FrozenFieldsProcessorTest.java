package com.neenbedankt.bundles;


import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import nl.littlerobots.bundles.processor.FrozenFieldsProcessor;
import org.junit.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class FrozenFieldsProcessorTest {

    @Test
    public void testNonArrayTypes() {
        Compilation compilation = javac().
                withProcessors(new FrozenFieldsProcessor()).
                compile(JavaFileObjects.forSourceLines("nl.littlerobots.test.TestActivity", "package com.neenbedankt.bundles;\n" +
                        "\n" +
                        "import android.app.Activity.Activity;\n" +
                        "import android.os.Bundle;\n" +
                        "import android.os.Parcelable;\n" +
                        "import nl.littlerobots.bundles.annotation.Frozen;\n" +
                        "\n" +
                        "import java.util.ArrayList;\n" +
                        "import java.util.Date;\n" +
                        "\n" +
                        "public class TestActivity extends Activity {\n" +
                        "    @Frozen\n" +
                        "    int mTestInt;\n" +
                        "    @Frozen\n" +
                        "    Integer mTestInteger;\n" +
                        "    @Frozen\n" +
                        "    String mTestString;\n" +
                        "    @Frozen\n" +
                        "    Long mTestLongObject;\n" +
                        "    @Frozen\n" +
                        "    long mTestLong;\n" +
                        "    @Frozen\n" +
                        "    float mTestFloat;\n" +
                        "    @Frozen\n" +
                        "    Float mTestFloatObject;\n" +
                        "    @Frozen\n" +
                        "    Double mTestDoubleObject;\n" +
                        "    @Frozen\n" +
                        "    double mTestDouble;\n" +
                        "    @Frozen\n" +
                        "    short mShort;\n" +
                        "    @Frozen\n" +
                        "    Short mShortObject;\n" +
                        "    @Frozen\n" +
                        "    boolean mBoolean;\n" +
                        "    @Frozen\n" +
                        "    Boolean mBooleanObject;\n" +
                        "    @Frozen\n" +
                        "    Bundle mBundle;\n" +
                        "    @Frozen\n" +
                        "    Parcelable mParcelable;\n" +
                        "    @Frozen\n" +
                        "    Character mCharacterObject;\n" +
                        "    @Frozen\n" +
                        "    char mChar;\n" +
                        "    @Frozen\n" +
                        "    CharSequence mCharSequence;\n" +
                        "    @Frozen\n" +
                        "    byte mByte;\n" +
                        "    @Frozen\n" +
                        "    Byte byteObject;\n" +
                        "\n" +
                        "    @Frozen\n" +
                        "    TestParcelable mTestParcelable;\n" +
                        "    \n" +
                        "    // tests Serializable\n" +
                        "    @Frozen\n" +
                        "    Date date;\n" +
                        "\n" +
                        "    public static class TestParcelable implements Parcelable {\n" +
                        "\n" +
                        "    }\n" +
                        "}\n"));
        assertThat(compilation).succeeded();
    }

    @Test
    public void testArrayTypes() {
        Compilation compilation = javac().
                withProcessors(new FrozenFieldsProcessor()).
                compile(JavaFileObjects.forSourceLines("nl.littlerobots.test.TestActivity", "package com.neenbedankt.bundles;\n" +
                        "\n" +
                        "import android.app.Activity.Activity;\n" +
                        "import nl.littlerobots.bundles.annotation.Frozen;\n" +
                        "\n" +
                        "import java.util.ArrayList;\n" +
                        "\n" +
                        "public class TestActivity extends Activity {\n" +
                        "    @Frozen\n" +
                        "    ArrayList<String> mStringArrayList;\n" +
                        "    @Frozen\n" +
                        "    ArrayList<Integer> mIntegerArrayList;\n" +
                        "    @Frozen\n" +
                        "    ArrayList<CharSequence> mCharSquenceList;\n" +
                        "    @Frozen\n" +
                        "    int[] mIntArray;\n" +
                        "    @Frozen\n" +
                        "    long[] mLongArray;\n" +
                        "    @Frozen\n" +
                        "    double[] mDoubleArray;\n" +
                        "    @Frozen\n" +
                        "    short[] mShortArray;\n" +
                        "    @Frozen\n" +
                        "    float[] mFloatArray;\n" +
                        "    @Frozen\n" +
                        "    byte[] mByteArray;\n" +
                        "    @Frozen\n" +
                        "    boolean[] mBooleanArray;\n" +
                        "    @Frozen\n" +
                        "    char[] mCharacterArray;\n" +
                        "}\n"));
        assertThat(compilation).succeeded();
    }

    @Test
    public void testUseGetterSetter() {
        Compilation compilation = javac().
                withProcessors(new FrozenFieldsProcessor()).
                compile(JavaFileObjects.forSourceLines("nl.littlerobots.bundles.test.TestActivity", "package nl.littlerobots.bundles.test;\n" +
                        "\n" +
                        "import android.app.Activity.Activity;\n" +
                        "import java.util.Date;\n" +
                        "import nl.littlerobots.bundles.annotation.Frozen;\n" +
                        "\n" +
                        "import java.util.ArrayList;\n" +
                        "\n" +
                        "public class TestActivity extends Activity {\n" +
                        "    @Frozen\n" +
                        "    private int mTestValue;\n" +
                        "    @Frozen\n" +
                        "    private Date anotherTestValue;\n" +
                        "    public final void setTestValue(int v) {};\n" +
                        "    public final void setAnotherTestValue(Date v) {};\n" +
                        "    public final int getTestValue() {return 0;}\n;" +
                        "    public final Date getAnotherTestValue() {return new Date();}\n;" +
                        "}\n"));
        assertThat(compilation).generatedSourceFile("nl.littlerobots.bundles.test.TestActivityState").
                hasSourceEquivalentTo(JavaFileObjects.forSourceString("nl.littlerobots.bundles.test.TestActivityState", "package nl.littlerobots.bundles.test;\n" +
                        "\n" +
                        "final class TestActivityState {\n" +
                        "  private TestActivityState() {\n" +
                        "  }\n" +
                        "  static void saveInstanceState(TestActivity source, android.os.Bundle outState) {\n" +
                        "    outState.putInt(\"testValue\", source.getTestValue());\n" +
                        "    outState.putSerializable(\"anotherTestValue\", source.getAnotherTestValue());\n" +
                        "  }\n" +
                        "  static void restoreInstanceState(TestActivity target, android.os.Bundle savedInstanceState) {\n" +
                        "    if (savedInstanceState == null) {\n" +
                        "      return;\n" +
                        "    }\n" +
                        "    target.setTestValue(savedInstanceState.getInt(\"testValue\"));\n" +
                        "    target.setAnotherTestValue((java.util.Date) savedInstanceState.getSerializable(\"anotherTestValue\"));\n" +
                        "  }\n" +
                        "}\n"));
    }

}
