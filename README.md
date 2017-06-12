# Readme

This is an annotation processor for making Fragment construction type safe, and making saving state easier.

There are two annotations provided, `@Argument` for fragment arguments and `@Frozen` for saving and restoring state in `onSaveInstanceState`.

# `@Argument`
Construction of Fragments can be painful if you need to pass arguments to construct them. The problem is that you need to construct a `Bundle`,
put values in it by key and set it on the `Fragment` instance. This is not only cumbersome to do, but also not very type safe.

The `@Argument` annotation helps by generating a builder class for your `Fragment`. For example:

    :::java
    public class TestFragment extends Fragment {
        @Argument
        boolean mCheese;
        @Argument(required=false)
        int mTotal;
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);        
            // Use the generated builder class to "inject" the arguments on creation
            // you can use a static import to shorten this line to injectArguments(this)
            TestFragmentBuilder.injectArguments(this);
        }
    }
    
To construct this fragment you'd do something like this:

    :::java
    TestFragment fragment = new Test2FragmentBuilder(false).build();
    TestFragment fragment2 = new Test2FragmentBuilder(false).total(10).build();

Required arguments must be passed into the builder constructor, while non-required arguments have a builder method.

# `@Frozen`
The `@Frozen` annotation aids with saving `Activity` or `Fragment` state in `onSaveInstanceState` and restoring it in `onCreate` or other places.
A class <YourActivityName>State is generated for this purpose. Here's an example:

    :::java
    package test;

    import static test.MyActivityState.restoreInstanceState;
    import static test.MyActivityState.saveInstanceState;
    import android.app.Activity;
    import android.os.Bundle;

    import com.neenbedankt.bundles.annotation.Frozen;

    public class MyActivity extends Activity {
        @Frozen
        String mName;
        @Frozen
        boolean mCheeseActive;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            restoreInstanceState(this, savedInstanceState);
        }

        @Override
        protected void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            saveInstanceState(this, outState);
        }
    }
    
# Using
Bundles is published on Maven Central. Just include the appropriate dependencies:

for `@Frozen`:

    :::groovy
    implementation 'nl.littlerobots.bundles:frozen-annotation:<latest-version>'
    annotationProcessor 'nl.littlerobots.bundles:frozen-processor:<latest-version>'
    
for `@Argument`:

    :::groovy
    implementation 'nl.littlerobots.bundles:argument-annotation:<latest-version>'
    annotationProcessor 'nl.littlerobots.bundles:argument-processor:<latest-version>'
    
# License

<pre>
This is free and unencumbered software released into the public domain.

Anyone is free to copy, modify, publish, use, compile, sell, or
distribute this software, either in source code form or as a compiled
binary, for any purpose, commercial or non-commercial, and by any
means.

In jurisdictions that recognize copyright laws, the author or authors
of this software dedicate any and all copyright interest in the
software to the public domain. We make this dedication for the benefit
of the public at large and to the detriment of our heirs and
successors. We intend this dedication to be an overt act of
relinquishment in perpetuity of all present and future rights to this
software under copyright law.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

For more information, please refer to <http://unlicense.org/>
</pre>
