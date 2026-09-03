plugins {
    alias(libs.plugins.cosmoswatch.android.feature)
}

android {
    namespace = "com.cosmoswatch.feature.marsphotos"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.androidx.paging.common)
}
