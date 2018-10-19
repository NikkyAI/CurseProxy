import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version Kotlin.version
    id("com.github.johnrengelman.shadow") version "2.0.2"
    id( "com.bmuschko.tomcat") version "2.5"
    application
    war
}

group = "moe.nikky"
version = "0.1-SNAPSHOT"

application {
    mainClassName = "io.ktor.server.netty.DevelopmentEngine"
}

war {
    webAppDirName = "webapp"
}

tomcat {
    contextPath = "/"
    httpProtocol = "org.apache.coyote.http11.Http11Nio2Protocol"
    ajpProtocol  = "org.apache.coyote.ajp.AjpNio2Protocol"
}

//tomcat {
//}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "http://dl.bintray.com/kotlin/ktor" )
    maven(url = "https://dl.bintray.com/kotlin/kotlinx" )
    maven(url=  "https://dl.bintray.com/pgutkowski/Maven" )
    maven(url = "https://dl.bintray.com/kotlin/squash" )
    maven(url = "https://jitpack.io" )
}

dependencies {
    compile(kotlin("stdlib", Kotlin.version))
    compile(kotlin("reflect", Kotlin.version))
    compile(kotlin("runtime", Kotlin.version))

    listOf("ktor", "ktor-jackson", "ktor-features", "ktor-locations", "ktor-html-builder", "ktor-server-core", "ktor-server-netty", "ktor-server-servlet").forEach {
        compile(group = "io.ktor", name = it, version = Ktor.version)
    }

//    tomcat("org.apache.tomcat.embed:tomcat-embed-core:${Tomcat.version}",
//            "org.apache.tomcat.embed:tomcat-embed-jasper:${Tomcat.version}")

    // Logging
    compile ("ch.qos.logback:logback-classic:${Logback.version}")

    // Networking
    compile (group= "com.github.kittinunf.fuel", name= "fuel", version= Fuel.version)
    compile (group= "com.github.kittinunf.fuel", name= "fuel-coroutines", version= Fuel.version)

    // GraphQL
    compile ("com.github.pgutkowski:kgraphql:${KGraphQL.version}")

    // Dependency Injection
    compile ("org.koin:koin-ktor:${Koin.version}")

    // Database
    compile ("org.jetbrains.squash:squash-h2:${Squash.version}")


    compile (group= "com.fasterxml.jackson.core", name= "jackson-databind", version= "2.9.5")
    compile (group= "com.fasterxml.jackson.module", name= "jackson-module-kotlin", version= "2.9.5")

    // Testing
    testCompile (group= "junit", name= "junit", version= "4.12")
    testCompile (group= "io.ktor", name= "ktor-server-test-host", version = Ktor.version)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
//        languageVersion = "1.3"
        jvmTarget = "1.8"
    }
}

kotlin {
    experimental.coroutines = Coroutines.ENABLE
}
