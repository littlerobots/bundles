Version 2.0
-----------
* Renamed group id and package to `nl.littlerobots.bundles.*`
* Processors and annotations are now split into separate modules, use `annotationProcessor` or `kapt` to include the processor
* For `@Argument` by default all arguments are considered to be non-null by default. The generated builder will check this at runtime.
In addition all arguments are annotated with `@android.support.annotation.NonNull`. Defaulting to nullable arguments is (for example, for migration) is possible
by adding `argument.default.nullable=true` as processor arguments.
* `@NonNull`, `@Nullable` (in any package) and `android.support.annotation.*` annotations will be copied over to the generated fragment builder when using `@Argument`
This allows for lint checks like `@ColorRes` to work on the builders.
* `@Argument` fields can now be private if a setter is provided. `@Frozen` fields can now be private when both a setter and a getter are provided.
 The main usecase for this is to support Kotlin, which will generate getters and setters in byte code. The annotations still work on fields only and a field is still required.
 