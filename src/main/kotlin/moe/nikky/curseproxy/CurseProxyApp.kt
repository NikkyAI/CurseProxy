package moe.nikky.curseproxy

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.*
import io.ktor.response.respond
import kotlinx.coroutines.*
import kotlinx.coroutines.debug.DebugProbes
import moe.nikky.curseproxy.data.AddonsImporter
import moe.nikky.curseproxy.di.mainModule
import moe.nikky.curseproxy.exceptions.*
import mu.KotlinLogging
import org.koin.ktor.ext.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.days

val LOG: Logger = LoggerFactory.getLogger("curseproxy")

@OptIn(ExperimentalTime::class)
fun Application.application() {
//    Koin.logger = PrintLogger()
//    startKoin(listOf(mainModule))

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
    install(ContentNegotiation) {
        jackson {
            registerModule(KotlinModule()) // Enable Kotlin support
            enable(SerializationFeature.INDENT_OUTPUT)
//            enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
        }
    }
    install(Koin) {
//        printLogger()
        slf4jLogger()
        modules(mainModule)
    }
//    install(Webjars) {
//        path = "/webjars" //defaults to /webjars
//    }

    //TODO: enable in production
//    install(HttpsRedirect)
//    install(HSTS)
    install(CORS) {
        maxAgeDuration = 1.days
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
            )
        }
        exception<AddonNotFoundException> { cause ->
            call.respond(
                    HttpStatusCode.NotFound,
                    cause
            )
        }
        exception<AddonFileNotFoundException> { cause ->
            call.respond(
                    HttpStatusCode.NotFound,
                    cause
            )
        }
        exception<MissingParameterException> { cause ->
            call.respond(
                    HttpStatusCode.NotAcceptable,
                    cause
            )
        }
        exception<MessageException> { cause ->
            call.respond(
                    HttpStatusCode.NotAcceptable,
                    cause
            )
        }
        exception<IllegalArgumentException> { cause ->
            call.respond(
                    HttpStatusCode.NotAcceptable,
                    StackTraceMessage(cause)
            )
        }
        exception<NumberFormatException> { cause ->
            call.respond(
                    HttpStatusCode.NotAcceptable,
                    StackTraceMessage(cause)
            )
        }
    }


    val importerJob = GlobalScope.launch(Dispatchers.IO + CoroutineName("import")) {
        val importer = AddonsImporter()
        while (true) {
            importer.import()
            log.info("Addons imported")
            delay(TimeUnit.HOURS.toMillis(3))
        }
    }

    // install routes
    routes()

    environment.monitor.subscribe(ApplicationStopped) {
        // cannot access logger inside here.. classloader complains
        println("Time to clean up!")

        // cleanup redis connection pool
        runBlocking {
            println("waiting for importer job to cancel")
            importerJob.cancelAndJoin()
        }


        println("cleanup done")
    }

    log.info("Application setup complete")
}
