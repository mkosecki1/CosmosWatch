plugins {
    alias(libs.plugins.cosmoswatch.android.library)
    alias(libs.plugins.cosmoswatch.compose)
}

android {
    namespace = "com.cosmoswatch.core.ui"
}

dependencies {
    implementation(libs.materialkolor)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
