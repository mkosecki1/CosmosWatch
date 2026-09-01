pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CosmosWatch"
include(":app")
include(":core:common")
include(":core:network")
include(":core:database")
include(":core:datastore")
include(":core:ui")
include(":core:notifications")
include(":core:testing")
include(":feature:apod")
include(":feature:marsphotos")
include(":feature:neows")
include(":feature:donki")
include(":feature:settings")
