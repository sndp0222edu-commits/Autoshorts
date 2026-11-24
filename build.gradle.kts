plugins {
    alias(libs.plugins.android.application) apply false
    // AGP 9 compiles Kotlin itself; this only picks the Kotlin version, which must match the Compose compiler plugin
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
