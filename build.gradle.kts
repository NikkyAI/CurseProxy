import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
    id("com.squareup.sqldelight")
    application
    idea
}

group = "moe.nikky"
version = "0.1-SNAPSHOT"

application {
//    mainClassName = "io.ktor.server.netty.DevelopmentEngine"
    mainClassName = "moe.nikky.curseproxy.MainKt"
    // seems to not apply to runShadowJar ?
    applicationDefaultJvmArgs = listOf(
            "-Dkotlinx.coroutines.debug=on"
    )
}

sqldelight {
    database("CurseDatabase"){
        packageName = "moe.nikky.curseproxy.data"
        sourceFolders = listOf("sqldelight")
        schemaOutputDirectory = file("src/main/sqldelight/migrations")
    }
}

val databaseSource = project.buildDir.resolve("sqldelight").resolve("CurseDatabase")

kotlin {
    sourceSets {
        getByName("main") {
            kotlin.srcDir(databaseSource)
        }
    }
}

idea {
    module {
        generatedSourceDirs.add(databaseSource)
    }
}

//war {
//    webAppDirName = "webapp"
//}

//tomcat {
//    contextPath = "/"
//    httpProtocol = "org.apache.coyote.http11.Http11Nio2Protocol"
//    ajpProtocol = "org.apache.coyote.ajp.AjpNio2Protocol"
//}

repositories {
    mavenCentral()
//    jcenter() // TODO: remove once
}

dependencies {
    implementation(kotlin("stdlib-jdk8", "_"))
    implementation(kotlin("reflect", "_"))

    implementation(Ktor.server.netty)
    implementation(Ktor.features.jackson)
    implementation(Ktor.features.htmlBuilder)
    implementation("io.ktor:ktor-serialization:_")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:_")

//    implementation("io.ktor:ktor-webjars:_")
//    implementation("org.webjars.npm:graphql-playground:_")

//    tomcat("org.apache.tomcat.embed:tomcat-embed-core:${Tomcat.version}",
//            "org.apache.tomcat.embed:tomcat-embed-jasper:${Tomcat.version}")

    // Logging
    implementation(group = "io.github.microutils", name = "kotlin-logging", version = "_")
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "_")

    // Networking
    implementation(Ktor.client.okHttp)
    implementation(Ktor.client.json)
    implementation(Ktor.client.serialization)
    implementation(Ktor.client.logging)

    // GraphQL
    implementation("com.expediagroup:graphql-kotlin-server:_")
    implementation("com.graphql-java:graphql-java-extended-scalars:_")

    // Dependency Injection
    implementation("io.insert-koin:koin-ktor:_")
    implementation("io.insert-koin:koin-logger-slf4j:_")

    // Database
    implementation(group = "com.squareup.sqldelight", name = "sqlite-driver", version = "_") // TODO: remove
    //TODO: add redis

    // JSON
    implementation(KotlinX.serialization.json)

    implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "_")
    implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = "_")

    // coroutines
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-jdk8", version = "_")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-slf4j", version = "_")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-debug", version = "_")

    // graphiql
    // does not help with hosting a static html+js blob
//    implementation("org.webjars:graphiql:_")

    // Testing
    testImplementation(group = "junit", name = "junit", version = "_")
    testImplementation(Ktor.server.testHost)
}

kotlin {
    sourceSets.all {
        languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        languageSettings.useExperimentalAnnotation("kotlin.time.ExperimentalTime")
    }
}
tasks.withType<KotlinCompile> {
    kotlinOptions {
//        apiVersion = "1.5"
//        languageVersion = "1.5"
        apiVersion = "1.4"
        languageVersion = "1.4"
        jvmTarget = "1.8" // TODO: install newer java and use JAVA 15/16
    }
}

