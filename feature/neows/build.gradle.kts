plugins {
    alias(libs.plugins.cosmoswatch.android.feature)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.cosmoswatch.feature.neows"
}

dependencies {
    implementation(project(":core:network"))
}
