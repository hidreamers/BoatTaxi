pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // PayPal SDK repository
        maven { url = uri("https://cardinalcommerceprod.jfrog.io/artifactory/android") }
    }
}

rootProject.name = "BoatTaxie"
include(":app")
