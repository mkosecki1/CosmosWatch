plugins {
    alias(libs.plugins.cosmoswatch.android.feature)
}

android {
    namespace = "com.cosmoswatch.feature.donki"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.androidx.paging.common)
}
