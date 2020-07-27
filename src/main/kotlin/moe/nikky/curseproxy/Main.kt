package moe.nikky.curseproxy

import io.ktor.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*
import kotlinx.coroutines.debug.DebugProbes
import java.lang.IllegalStateException
import java.util.concurrent.TimeUnit

// start embedded server and close gracefully
fun main(args: Array<String>) {
    System.setProperty(DEBUG_PROPERTY_NAME, DEBUG_PROPERTY_VALUE_ON)

//    runBlocking {
//        println("   'runBlocking': I'm working in thread ${Thread.currentThread().name}")
//
//        val job = launch(CoroutineName("my-custom-name")) {
//            println("   'runBlocking': I'm working in thread ${Thread.currentThread().name}")
//        }
//
//        job.join()
//    }

    val server = embeddedServer(
        factory = Netty,
        port = System.getenv("PORT")?.toIntOrNull() ?: 8081,
        host = System.getenv("HOST") ?: "localhost",
        watchPaths = listOf("jvm/main"),
        module = Application::application,
        configure = {

        }
    ).start(false)

    Runtime.getRuntime().addShutdownHook(Thread {
        server.stop(1, 5, TimeUnit.SECONDS)
    })
    Thread.currentThread().join()
}
