plugins {
    `kotlin-dsl`
}

group = "com.cosmoswatch.buildlogic"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "cosmoswatch.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "cosmoswatch.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("compose") {
            id = "cosmoswatch.compose"
            implementationClass = "ComposeConventionPlugin"
        }
        register("hilt") {
            id = "cosmoswatch.hilt"
            implementationClass = "HiltConventionPlugin"
        }
        register("testing") {
            id = "cosmoswatch.testing"
            implementationClass = "TestingConventionPlugin"
        }
    }
}
