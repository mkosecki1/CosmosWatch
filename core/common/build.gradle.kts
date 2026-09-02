plugins {
    alias(libs.plugins.cosmoswatch.android.library)
    alias(libs.plugins.cosmoswatch.hilt)
}

android {
    namespace = "com.cosmoswatch.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
