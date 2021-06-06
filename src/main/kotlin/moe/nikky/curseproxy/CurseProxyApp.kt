package moe.nikky.curseproxy

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.serialization.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import moe.nikky.curseproxy.data.importAddons
import moe.nikky.curseproxy.di.mainModule
import moe.nikky.curseproxy.exceptions.*
import mu.KotlinLogging
import org.koin.core.context.GlobalContext
import org.koin.ktor.ext.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Deprecated("use logger per file")
val LOG: Logger = LoggerFactory.getLogger("curseproxy")

private val logger = KotlinLogging.logger{}

val koinCtx by lazy {
    GlobalContext.get()
}

fun Application.application() {
    install(DefaultHeaders)
    install(CallLogging) {
        this@install.logger = KotlinLogging.logger {}
        level = Level.INFO
        mdc("method") {
            it.request.httpMethod.value
        }
        mdc("path") {
            it.request.path()
        }
    }
    install(Koin) {
//        printLogger()
        slf4jLogger()
        modules(mainModule)
    }
    install(ContentNegotiation) {
        json(
            json = koinCtx.get()
        )

//        jackson {
//            registerModule(KotlinModule()) // Enable Kotlin support
//            enable(SerializationFeature.INDENT_OUTPUT)
////            enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
//        }
    }
//    install(ContentNegotiation) {
//        jackson {
//            registerModule(KotlinModule()) // Enable Kotlin support
//            enable(SerializationFeature.INDENT_OUTPUT)
////            enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
//        }
//    }
//    install(Webjars) {
//        path = "/webjars" //defaults to /webjars
//    }

    //TODO: enable in production
//    install(HttpsRedirect)
//    install(HSTS)
    install(CORS) {
        maxAgeInSeconds = 12.toDuration(DurationUnit.HOURS).inSeconds.toLong()
        anyHost()
    }
//    install(Metrics) {
//        val reporter = Slf4jReporter.forRegistry(registry)
//                .outputTo(log)
//                .convertRatesTo(TimeUnit.SECONDS)
//                .convertDurationsTo(TimeUnit.MILLISECONDS)
//                .build()
//        reporter.start(10, TimeUnit.SECONDS)
//    }

    install(StatusPages) {
        exception<Throwable> { cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                StackTraceMessage(cause)
//                mapper.writeValueAsString(StackTraceMessage(cause))
            )
        }
        exception<MessageException> { cause ->
            call.respond(
                HttpStatusCode.NotAcceptable,
                cause.error()
//                mapper.writeValueAsString(cause)
            )
        }
        exception<IllegalArgumentException> { cause ->
            call.respond(
                HttpStatusCode.NotAcceptable,
                StackTraceMessage(cause)
//                mapper.writeValueAsString(StackTraceMessage(cause))
            )
        }
        exception<NumberFormatException> { cause ->
            call.respond(
                HttpStatusCode.NotAcceptable,
//                cause.error()
                StackTraceMessage(cause)
//                mapper.writeValueAsString(StackTraceMessage(cause))
            )
        }
    }

    val importerExecutor = Executors.newScheduledThreadPool(1)

    importerExecutor.scheduleWithFixedDelay({
        runBlocking(Dispatchers.IO + CoroutineName("import")) {
            importAddons()
            log.info("Addons imported")
        }
    }, 0, 3, TimeUnit.HOURS)

    // install routes
    routes()

    environment.monitor.subscribe(ApplicationStopped) {
        // cannot access logger inside here.. classloader complains
        println("Time to clean up!")

        // stop executors
        runBlocking {
            println("waiting for importer job to cancel")

            importerExecutor.shutdown()
            try {
                importerExecutor.awaitTermination(30, TimeUnit.SECONDS)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }


        println("cleanup done")
    }

    log.info("Application setup complete")
}
