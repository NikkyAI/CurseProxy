package moe.nikky.curseproxy

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.content.default
import io.ktor.content.static
import io.ktor.html.respondHtml
import io.ktor.request.header
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.routing
import kotlinx.html.*
import moe.nikky.curseproxy.curse.Widget.widget
import moe.nikky.curseproxy.curse.CurseUtil
import moe.nikky.curseproxy.curse.Widget
import moe.nikky.curseproxy.curse.auth.curseAuth
import moe.nikky.curseproxy.curse.files
import moe.nikky.curseproxy.curse.latestFile
import moe.nikky.curseproxy.exceptions.AddonFileNotFoundException
import moe.nikky.curseproxy.exceptions.AddonNotFoundException
import moe.nikky.curseproxy.graphql.AppSchema
import moe.nikky.encodeBase64
import org.koin.ktor.ext.inject
import java.io.File

@Suppress("unused")
fun Application.routes() {

    routing {
        val appSchema: AppSchema by inject()
        val mapper: ObjectMapper by inject()

        graphql(log, mapper, appSchema.schema)
        curse()

        static("/") {
            default("database.html")
        }

//        get("/api/ids") {
//            LOG.debug("Get all AddOns")
//            val ids = IDCache.get()
//            LOG.info("ids count: ${ids.count()}")
//
//            call.respond(ids)
//        }

//        get("/api/ids/{id}") {
//            val id = call.parameters["id"]?.toInt()
//                    ?: throw NumberFormatException("id")
//            LOG.debug("Get AddOn with id=$id")
//            with(IDCache.get(id)) {
//                call.respond(this)
//            }
//        }

        val ADDON_API = "https://addons-v2.forgesvc.net/api"

        get("/api/addon/{id}") {
            val id = call.parameters["id"]?.toInt()
                    ?: throw NumberFormatException("id")
//            var addonRequest = AddonRequest()
            val url = "$ADDON_API/addon/$id"
            val (request, response, result) = url.httpGet()
                    .curseAuth()
//                    .body()
                    .responseString()
            when (result) {
                is Result.Success -> {
                    log.info("json: ${result.value}")
                }
                is Result.Failure -> {
                    log.error("failed $request ${result.error}")
                }
            }
//            call.respondHtml {
//                widget(id, versions.toMutableList())
//            }
        }

        get("/api/widget/{id}") {
            val id = call.parameters["id"]?.toInt()
                    ?: throw NumberFormatException("id")
            val versions = call.parameters.getAll("version") ?: emptyList()
            call.respondHtml {
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

        get("/api/url/{id}/{fileid}") {
            val id = call.parameters["id"]?.toInt()
                    ?: throw NumberFormatException("id")
            val fileid = call.parameters["fileid"]?.toInt()
                    ?: throw NumberFormatException("fileid")
            val file = CurseUtil.getAddonFile(id, fileid) ?: throw AddonFileNotFoundException(id, fileid)
            call.respondRedirect(url = file.downloadURL, permanent = false)
        }

        get("/api/img/{id}") {
            val id = call.parameters["id"]?.toInt()
                    ?: throw NumberFormatException("id")
            val style = call.parameters["style"]
            val link = call.parameters.contains("link")
            val logo = call.parameters.contains("logo")

            val addon = CurseUtil.getAddon(id) ?: throw AddonNotFoundException(id)
            val versions = call.parameters.getAll("version") ?: emptyList()
            val file = addon.latestFile(versions)

            val name = addon.name
                    .replace("-", "--")
                    .replace("_", "__")
            val fileName = file.fileName.replace(addon.name, "")
                    .replace(Regex("^[\\s-._]+"), "")
                    .replace("-", "--")
                    .replace("_", "__")

            var url = "https://img.shields.io/badge/$name-$fileName-orange.svg?maxAge=3600"
            if (logo) {
                val logoData = "data:image/png;base64," + File(Widget::class.java.getResource("/anvil.png").file).encodeBase64()
                url += "&logo=$logoData"
            }
            if (link) {
                val left = "https://minecraft.curseforge.com/projects/$id"
                val right = file.downloadURL
                url += "&link=$left&link=$right"
            }
            if (style != null) {
                url += "&style=$style"
            }

            call.respondRedirect(url = url, permanent = false)
        }

        get("/api/img/{id}/files") {
            val id = call.parameters["id"]?.toInt()
                    ?: throw NumberFormatException("id")
            val style = call.parameters["style"]
            val link = call.parameters.contains("link")
            val logo = call.parameters.contains("logo")

            val addon = CurseUtil.getAddon(id) ?: throw AddonNotFoundException(id)
            val versions = call.parameters.getAll("version") ?: emptyList()
            val files = addon.files(versions)
            val count = files.count()

            val name = addon.name
                    .replace("-", "--")
                    .replace("_", "__")
            val label = "$count Files"
            var url = "https://img.shields.io/badge/$name-$label-orange.svg?maxAge=3600"
            if (logo) {
                val logoData = "data:image/png;base64," + File(Widget::class.java.getResource("/anvil.png").file).encodeBase64()
                url += "&logo=$logoData"
            }
            if (link) {
                val left = "https://minecraft.curseforge.com/projects/$id"
                val right = "https://minecraft.curseforge.com/projects/$id/files"
                url += "&link=$left&link=$right"
            }
            if (style != null) {
                url += "&style=$style"
            }

            call.respondRedirect(url = url, permanent = false)
        }

        get("/api/img/{id}/{fileid}") {
            val id = call.parameters["id"]?.toInt()
                    ?: throw NumberFormatException("id")
            val fileid = call.parameters["fileid"]?.toInt()
                    ?: throw NumberFormatException("fileid")
            val style = call.parameters["style"]
            val link = call.parameters.contains("link")
            val logo = call.parameters.contains("logo")

            val addon = CurseUtil.getAddon(id) ?: throw AddonNotFoundException(id)
            val file = CurseUtil.getAddonFile(id, fileid) ?: throw AddonFileNotFoundException(id, fileid)

            val name = addon.name
                    .replace("-", "--")
                    .replace("_", "__")
            val fileName = file.fileName.replace(addon.name, "")
                    .replace(Regex("^[\\s-._]+"), "")
                    .replace("-", "--")
                    .replace("_", "__")

            var url = "https://img.shields.io/badge/$name-$fileName-orange.svg?maxAge=3600"
            if (logo) {
                val logoData = "data:image/png;base64," + File(Widget::class.java.getResource("/anvil.png").file).encodeBase64()
                url += "&logo=$logoData"
            }
            if (link) {
                val left = "https://minecraft.curseforge.com/projects/$id"
                val right = file.downloadURL
                url += "&link=$left&link=$right"
            }
            if (style != null) {
                url += "&style=$style"
            }

            call.respondRedirect(url = url, permanent = false)
        }


        get("/api/demo/{id}") {
            val id = call.parameters["id"]?.toInt()
                    ?: throw NumberFormatException("id")
            call.respondHtml {
                body {
                    a(href = "/api/url/$id") {
                        img(src = "/api/img/$id")
                    }
                    br {}
                    img(src = "/api/img/$id?link")

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
                        +"How are you doing?"
                    }
                    a(href = "https://github.com/NikkyAI/CurseProxy/blob/master/README.md") { +"get started here" }
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
                    call.request.headers.entries().forEach { (key, value) ->
                        p {
                            +"$key = $value"
                        }
                    }
                }
            }
        }
    }
}