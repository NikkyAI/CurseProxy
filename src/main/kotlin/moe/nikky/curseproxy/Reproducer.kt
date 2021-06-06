package moe.nikky.curseproxy

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {

    val server = embeddedServer(
        factory = Netty,
        port = System.getenv("PORT")?.toIntOrNull() ?: 8081,
        host = System.getenv("HOST") ?: "localhost",
        watchPaths = listOf("jvm/main"),
        module = Application::configuration,
        configure = {

        }
    ).start(false)

    Runtime.getRuntime().addShutdownHook(Thread {
        server.stop(1, 5, TimeUnit.SECONDS)
    })
    Thread.currentThread().join()
}

fun Application.configuration() {
    install(DefaultHeaders)
}