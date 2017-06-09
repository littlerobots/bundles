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

}
