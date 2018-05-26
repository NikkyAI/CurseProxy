package moe.nikky.curseproxy

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.locations.Locations
import io.ktor.response.respond
import moe.nikky.curseproxy.curse.CurseClient
import moe.nikky.curseproxy.curse.auth.AuthToken
import moe.nikky.curseproxy.dao.importData
import moe.nikky.curseproxy.di.mainModule
import moe.nikky.curseproxy.exceptions.*
import moe.nikky.curseproxy.model.Addon
import org.koin.Koin
import org.koin.log.PrintLogger
import org.koin.standalone.StandAloneContext.startKoin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.measureTimeMillis

val LOG: Logger = LoggerFactory.getLogger("curseproxy")

fun Application.main() {
    Koin.logger = PrintLogger()
    startKoin(listOf(mainModule))

    install(DefaultHeaders)
    install(CallLogging)
    install(Locations)
    install(ContentNegotiation) {
//        jackson {
//            configure(SerializationFeature.INDENT_OUTPUT, true)
//        }

        gson {
            //            setup()
            setPrettyPrinting()
        }
    }
    //TODO: enable in production
//    install(HttpsRedirect)
//    install(HSTS)
//    install(CORS) {
//        maxAge = Duration.ofDays(1)
//    }
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

    //TODO: startup / getting data

    AuthToken.test()


//    val matterlink = CurseClient.getAddon(287323)
//    val matterlinkFiles = CurseClient.getAddonFiles(287323)
//    log.info(matterlink.toString())
//    log.info(matterlinkFiles.toString())
//    val jei = CurseClient.getAddon(238222)
//    val jeiFiles = CurseClient.getAddonFiles(238222)
//    log.info(jei.toString())
//    log.info(jeiFiles.toString())

//    var addons: List<Addon>? = null
//    val duration = measureTimeMillis {
//        addons = CurseClient.getAllAddonsByCriteria(432)
//    }
//    LOG.info("took $duration ms")
//    log.info(addons?.size.toString())
//    val firstGameName = addons?.firstOrNull()?.gameName
//    log.info(firstGameName)

//    LOG.info("loading IDs")
//
//    LOG.info("loaded ${idMap.size} IDs")
//    LOG.info("loaded addon test complete")

    importData()


    log.info("Application setup complete")
}
