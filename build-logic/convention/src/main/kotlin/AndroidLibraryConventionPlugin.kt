package com.cosmoswatch.buildlogic.convention

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Shared setup for every Android library module (core:*, feature:*).
 * Applies the Android Library plugin (AGP 9.x brings Kotlin support in
 * automatically — applying org.jetbrains.kotlin.android separately here
 * conflicts with it, "kotlin" extension already registered) and sets the
 * compileSdk/minSdk/Java compatibility common to the whole project.
 * Per-module settings (namespace) stay in each module's own build.gradle.kts.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")

            extensions.configure<LibraryExtension> {
                compileSdk {
                    version = release(37)
                }

                defaultConfig {
                    minSdk = 26
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
            }
        }
    }
}
