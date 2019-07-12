package moe.nikky.curseproxy

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.locations.Locations
import io.ktor.response.respond
import kotlinx.coroutines.*
import kotlinx.coroutines.debug.DebugProbes
import moe.nikky.curseproxy.data.AddonsImporter
import moe.nikky.curseproxy.di.mainModule
import moe.nikky.curseproxy.exceptions.*
import org.koin.core.Koin
import org.koin.log.PrintLogger
import org.koin.standalone.StandAloneContext.startKoin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.TimeUnit

val LOG: Logger = LoggerFactory.getLogger("curseproxy")

fun Application.main() {
    Koin.logger = PrintLogger()
    startKoin(listOf(mainModule))

    install(DefaultHeaders)
    install(CallLogging)
    install(Locations)
    install(ContentNegotiation) {
        jackson {
            registerModule(KotlinModule()) // Enable Kotlin support
            enable(SerializationFeature.INDENT_OUTPUT)
//            enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
        }
    }
    //TODO: enable in production
//    install(HttpsRedirect)
//    install(HSTS)
    install(CORS) {
        maxAge = Duration.ofDays(1)
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

    DebugProbes.install()
    DebugProbes.sanitizeStackTraces = true


    GlobalScope.launch(Dispatchers.IO + CoroutineName("import")) {
        val importer = AddonsImporter()
        while (true) {
            importer.import(log)
            log.info("Addons imported")
            delay(TimeUnit.HOURS.toMillis(3))
        }
    }

    log.info("Application setup complete")
}
