package moe.nikky.curseproxy

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.response.*
import io.ktor.routing.get
import io.ktor.routing.routing
import kotlinx.html.*
import moe.nikky.curseproxy.Widget.widget
import moe.nikky.curseproxy.exceptions.AddonNotFoundException
import moe.nikky.curseproxy.exceptions.MessageException
import moe.nikky.curseproxy.exceptions.MissingParameterException
import moe.nikky.curseproxy.exceptions.StackTraceMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import voodoo.curse.AddOn
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.File
import javax.imageio.ImageIO
import java.awt.AlphaComposite
import java.awt.Graphics2D


val LOG: Logger = LoggerFactory.getLogger("curseproxy")

const val REST_ENDPOINT = "/api"

fun Application.main() {

    install(DefaultHeaders)
    install(CallLogging)
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
    install(ContentNegotiation) {
//        jackson {
//            configure(SerializationFeature.INDENT_OUTPUT, true)
//        }

        gson {
//            setup()
            setPrettyPrinting()
            disableHtmlEscaping()
        }
    }

    routing {
        get("/api/ids") {
            LOG.debug("Get all AddOns")
            val ids = IDCache.get()
            LOG.info("ids count: ${ids.count()}")

            call.respond(ids)
        }

        get("/api/ids/{id}") {
            val id = call.parameters["id"]?.toInt()
                    ?: throw NumberFormatException("id")
            LOG.debug("Get AddOn with id=$id")
            with(IDCache.get(id)) {
                call.respond(this)
            }
        }

        get("/api/widget/{id}") {
            val id = call.parameters["id"]?.toInt()
                    ?: throw NumberFormatException("id")
            val versions = call.parameters.getAll("version") ?: emptyList()
            call.respondHtml{
                widget(id, versions.toMutableList())
            }
        }

        get("/api/widget.css") {
            call.respondFile(File(Widget::class.java.getResource("/css/widget.css").file))
        }

        get("/api/url/{id}") {
            val id = call.parameters["id"]?.toInt()
                    ?: throw NumberFormatException("id")
            val addon = CurseUtil.getAddon(id) ?: throw AddonNotFoundException(id)
            val versions = call.parameters.getAll("version") ?: emptyList()
            val file = addon.latestFile(versions)
            call.respondRedirect(url = file.downloadURL, permanent = false)
        }
        get("/api/img/{id}") {
            val id = call.parameters["id"]?.toInt()
                    ?: throw NumberFormatException("id")
            val fcolor = call.parameters["fcolor"] ?: "FFFFFF"
            val bcolor = call.parameters["bcolor"] ?: "f05523"
            val font = call.parameters["font"] ?: "arial"
            val size = call.parameters["size"] ?: "12"
            val addon = CurseUtil.getAddon(id) ?: throw AddonNotFoundException(id)
            val versions = call.parameters.getAll("version") ?: emptyList()
            val file = addon.latestFile(versions)

//            val url = "https://img.shields.io/badge/Matter%20Link-1.12.2--1.1.5-orange.svg"

            val name = addon.name.replace("-", "--")
            val fileName = file.fileName.replace(addon.name, "").replace("-", "--")
            val url = "https://img.shields.io/badge/$name-$fileName-orange.svg"
            LOG.info("redirect: '$url'")

            call.respondRedirect(url = url, permanent = false)
        }

        get("/api/demo/{id}") {
            val id = call.parameters["id"]?.toInt()
                    ?: throw NumberFormatException("id")
            call.respondHtml {
                body {
                    a(href = "/api/url/$id") {
                        img (src = "/api/img/$id")
                    }
                }
            }
        }

        get("/") {
            call.respondHtml {
                head {
                    title("CurseProxy API")
                }
                body {
                    h1 { +"CurseProxy API" }
                    p {
                        +"Hello World"
                    }
                    p {
                        +"How are you doing?"
                    }
                    a(href = "/api/ids/") { +"get started here" }
                }
            }
        }
        get("/debug/") {
            val scheme = call.request.header("X-Forwarded-Proto") ?: call.request.local.scheme
            val host = call.request.header("Host") ?: "${call.request.local.host}:${call.request.local.port}"
            call.respondHtml {
                head {
                    title("CurseProxy API")
                }
                body {
                    h1 { +"CurseProxy API debug" }
                    p {
                        +"Hello World"
                    }
                    p {
                        +"scheme = $scheme"
                    }
                    p {
                        +"host = $host"
                    }
                    h2 { +"call.request.local" }
                    listOf(
                            "scheme = ${call.request.local.scheme}",
                            "version = ${call.request.local.version}",
                            "port = ${call.request.local.port}",
                            "host = ${call.request.local.host}",
                            "uri = ${call.request.local.uri}",
                            "method = ${call.request.local.method}"
                    ).forEach { p { +it } }

                    h2 { +"Headers" }
                    call.request.headers.entries().forEach {(key, value) ->
                        p {
                            +"$key = $value"
                        }
                    }
                }
            }
        }
    }
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

    LOG.info("loading IDs")
    val idMap = IDCache.get()
    LOG.info("loaded ${idMap.size} IDs")
//    LOG.info("loaded addon test complete")
}
