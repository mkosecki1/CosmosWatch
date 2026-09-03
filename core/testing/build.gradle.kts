plugins {
    alias(libs.plugins.cosmoswatch.android.library)
}

android {
    namespace = "com.cosmoswatch.core.testing"
}

dependencies {
    api(libs.junit.jupiter)
    api(libs.kotlinx.coroutines.test)
    api(libs.mockk)
    api(libs.turbine)
}
