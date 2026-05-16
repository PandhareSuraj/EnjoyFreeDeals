// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

val externalBuildRoot = file(
    providers.environmentVariable("ENJOYFREEDEALS_BUILD_DIR")
        .orElse("${System.getProperty("user.home")}\\.gradle-enjoyfreedeals-build")
        .get()
)

layout.buildDirectory.set(externalBuildRoot.resolve("root"))

subprojects {
    layout.buildDirectory.set(externalBuildRoot.resolve(name))
}
