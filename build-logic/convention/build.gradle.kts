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
            implementationClass = "com.cosmoswatch.buildlogic.convention.AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "cosmoswatch.android.feature"
            implementationClass = "com.cosmoswatch.buildlogic.convention.AndroidFeatureConventionPlugin"
        }
        register("compose") {
            id = "cosmoswatch.compose"
            implementationClass = "com.cosmoswatch.buildlogic.convention.ComposeConventionPlugin"
        }
        register("hilt") {
            id = "cosmoswatch.hilt"
            implementationClass = "com.cosmoswatch.buildlogic.convention.HiltConventionPlugin"
        }
        register("testing") {
            id = "cosmoswatch.testing"
            implementationClass = "com.cosmoswatch.buildlogic.convention.TestingConventionPlugin"
        }
    }
}
