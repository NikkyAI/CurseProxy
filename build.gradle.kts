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
    jcenter()
    maven(url = "https://dl.bintray.com/kotlin/kotlinx")
    maven(url = "http://dl.bintray.com/kotlin/ktor")
}

dependencies {
    implementation(kotlin("stdlib-jdk8", Kotlin.version))
    implementation(kotlin("reflect", Kotlin.version))
//    compile(kotlin("runtime", Kotlin.version))

    implementation(Ktor.server.netty)
//    implementation(Ktor.server.servlet)
    implementation(Ktor.features.jackson)
    implementation(Ktor.features.htmlBuilder)
//    implementation("io.ktor:ktor-webjars:_")

//    tomcat("org.apache.tomcat.embed:tomcat-embed-core:${Tomcat.version}",
//            "org.apache.tomcat.embed:tomcat-embed-jasper:${Tomcat.version}")

    // Logging
    implementation(group = "io.github.microutils", name = "kotlin-logging", version = "_")
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "_")

    // Networking
    implementation(group = "com.github.kittinunf.fuel", name = "fuel", version = "_")
    implementation(group = "com.github.kittinunf.fuel", name = "fuel-coroutines", version = "_")
    implementation(group = "com.github.kittinunf.fuel", name = "fuel-kotlinx-serialization", version = "_")

    // GraphQL
    implementation(group = "com.apurebase", name = "kgraphql", version = "_")

    // Dependency Injection
    implementation(group = "org.koin", name = "koin-ktor", version = "_")
    implementation("org.koin:koin-logger-slf4j:_")

    // Database
    implementation(group = "com.squareup.sqldelight", name = "sqlite-driver", version = "_")

    // JSON
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-runtime", version = Serialization.version)

    implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "_")
    implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = "_")

    // coroutines
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-debug", version = "_")

    // graphiql
    // does not help with hosting a static html+js blob
//    implementation("org.webjars:graphiql:_")

    // Testing
    testImplementation(group = "junit", name = "junit", version = "_")
    testImplementation(Ktor.server.testHost)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        apiVersion = "1.3"
        languageVersion = "1.3"
        jvmTarget = "1.8"
    }
}

tasks.withType<Wrapper> {
    gradleVersion = Gradle.version
    distributionType = Gradle.distributionType // Wrapper.DistributionType.ALL
}