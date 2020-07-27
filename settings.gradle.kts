import de.fayard.dependencies.bootstrapRefreshVersionsAndDependencies

pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
        google()
        mavenCentral()
    }
}
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies.classpath("de.fayard:dependencies:0.5.8")
}

plugins {
    id("com.gradle.enterprise").version("3.1.1")
}

bootstrapRefreshVersionsAndDependencies(
        listOf(rootDir.resolve("dependencies-rules.txt").readText())
)

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
//        publishAlwaysIf(true)
    }
}

rootProject.name = "CurseProxy"
