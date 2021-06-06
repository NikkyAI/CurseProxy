pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
        google()
        mavenCentral()
    }
}

plugins {
    id("com.gradle.enterprise") version "3.6.1"
    id("de.fayard.refreshVersions") version "0.10.0"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
//        publishAlwaysIf(true)
        buildScanPublished {
            file("buildscan.log").appendText("${java.util.Date()} - $buildScanUri\n")
        }
    }
}

refreshVersions {
    extraArtifactVersionKeyRules(file("dependencies-rules.txt"))
}

rootProject.name = "CurseProxy"
