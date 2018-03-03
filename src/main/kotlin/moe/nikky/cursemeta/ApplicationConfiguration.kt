package moe.nikky.cursemeta

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.pipeline.PipelineContext
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.util.url
import kotlinx.html.*
import moe.nikky.cursemeta.addon.AddonRepo
import moe.nikky.cursemeta.addon.FileRepo
import moe.nikky.cursemeta.addon.IDCache
import moe.nikky.exceptionString
import moe.nikky.json
import moe.nikky.setup
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.PrintWriter
import java.io.StringWriter


val LOG: Logger = LoggerFactory.getLogger("cursemeta")

const val REST_ENDPOINT = "/api/addon"

fun Application.main() {

//    install(DefaultHeaders)
//    install(CallLogging)
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
        gson {
            setup()
            setPrettyPrinting()
        }
    }

    routing {
        get(REST_ENDPOINT) {
            errorAware {
                LOG.debug("Get all AddOns")
                val addons = AddonRepo.get()
                LOG.info("addon count: ${addons.count()}")

                call.respond(addons)
            }
        }

        get("$REST_ENDPOINT/{id}") {
            errorAware {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Parameter id not found")
                LOG.debug("Get AddOn entity with Id=$id")
                with(AddonRepo.get(id)) {
                    if (this == null) {
                        call.respond(
                                HttpStatusCode.NotFound,
                                ErrorMessage("AddOn with id $id not found")
                        )
                    } else {
                        call.respond(this)
                    }
                }
            }
        }

        get("$REST_ENDPOINT/{id}/description") {
            errorAware {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Parameter id not found")
                LOG.debug("Get AddOn Description with Id=$id")
                with(AddonRepo.getDescription(id)) {
                    if (this == null) {
                        call.respond(
                                HttpStatusCode.NotFound,
                                ErrorMessage("AddOn with id $id not found")
                        )
                    } else {
                        call.respondText(this, contentType= ContentType.parse("text/html"))
                    }
                }
            }
        }

        get("$REST_ENDPOINT/{addonID}/files") {
            errorAware {
                val addonID = call.parameters["addonID"]?.toInt()
                        ?: throw IllegalArgumentException("Parameter addon id not found")
                val files = FileRepo.get(addonID)
                call.respond(files)
            }
        }

        get("$REST_ENDPOINT/{addonID}/files/{fileID}") {
            errorAware {
                val addonID = call.parameters["addonID"]?.toInt()
                        ?: throw IllegalArgumentException("Parameter addon id not found")
                val fileID = call.parameters["fileID"]?.toInt()
                        ?: throw IllegalArgumentException("Parameter file id not found")
                with(FileRepo.get(addonID, fileID)) {
                    if (this == null) {
                        call.respond(
                                HttpStatusCode.NotFound,
                                ErrorMessage("File $addonID $fileID not found")
                        )
                    } else {
                        call.respond(this)
                    }
                }
            }
        }

        get("$REST_ENDPOINT/{addonID}/files/{fileID}/changelog") {
            errorAware {
                val addonID = call.parameters["addonID"]?.toInt()
                        ?: throw IllegalArgumentException("Parameter addon id not found")
                val fileID = call.parameters["fileID"]?.toInt()
                        ?: throw IllegalArgumentException("Parameter file id not found")
                with(FileRepo.getChangelog(addonID, fileID)) {
                    if (this == null) {
                        call.respond(
                                HttpStatusCode.NotFound,
                                ErrorMessage("File $addonID $fileID not found")
                        )
                    } else {
                        call.respondText(this, contentType= ContentType.parse("text/html"))
                    }
                }
            }
        }

        get("/api/ids") {
            errorAware {
                val idMap = IDCache.getIDMap()
                call.respond(idMap)
            }
        }

//        get(REST_ENDPOINT) {
//            errorAware {
//                LOG.debug("Get all Person entities")
//                call.respond(PersonRepo.getAll())
//            }
//        }
//        delete("${REST_ENDPOINT}/{id}") {
//            errorAware {
//                val id = call.parameters["id"] ?: throw IllegalArgumentException("Parameter id not found")
//                LOG.debug("Delete Person entity with Id=$id")
//                call.respondSuccessJson(PersonRepo.remove(id))
//            }
//        }
//        delete(REST_ENDPOINT) {
//            errorAware {
//                LOG.debug("Delete all Person entities")
//                PersonRepo.clear()
//                call.respondSuccessJson()
//            }
//        }
//        post(REST_ENDPOINT) {
//            errorAware {
//                val receive = call.receive<Person>()
//                println("Received Post Request: $receive")
//                call.respond(PersonRepo.add(receive))
//            }
//        }
        get("/") {
            call.respondHtml {
                head {
                    title("CurseMeta API")
                }
                body {
                    h1 { +"CurseMeta API" }
                    p {
                        +"Hello World"
                    }
                    p {
                        +"How are you doing?"
                    }
                    a(href="http://localhost:8080/api/addon/") { +"get started here"}
                }
            }
        }
    }

    AddonRepo.sync()
    LOG.info("loading IDs")
    val idMap = IDCache.getIDMap()
    val idCount = idMap.values.sumBy { it.size }
    LOG.info("loaded $idCount IDs")
    AddonRepo.get(287323)
    LOG.info("loaded addon test complete")
}

private suspend fun <R> PipelineContext<*, ApplicationCall>.errorAware(block: suspend () -> R): R? {
    return try {
        block()
    } catch (e: Exception) {
        LOG.error("caught exception", e)
        call.respond(
                HttpStatusCode.InternalServerError,
                ErrorMessage( "$e ${e.exceptionString}")
        )
        //call.respondText("""{"error":"$e"}""", ContentType.parse("application/json"), HttpStatusCode.InternalServerError)
        null
    }
}
