package moe.nikky.curseproxy

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.html.HtmlContent
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.content.default
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.request.header
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.routing
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import moe.nikky.curseproxy.curse.*
import moe.nikky.curseproxy.curse.Widget.widget
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
            files("static")
            default("index.html")
        }

        get("/test/exception") {
            throw AddonFileNotFoundException(1234, 5678)
        }

        get("/api/widget/{id}") {
            val id = call.parameters["id"]?.toInt()
                ?: throw NumberFormatException("id")
            val versions: MutableList<String> = call.parameters.getAll("version")?.toMutableList() ?: mutableListOf()

            call.respondHtml {
                val addon = runBlocking { CurseClient.getAddon(id) } ?: throw AddonNotFoundException(id)
                val files = runBlocking { CurseClient.getAddonFiles(id) } ?: emptyList()

                if (versions.isEmpty()) {
                    val sorted = files.map { it.gameVersion.sortedWith(VersionComparator.reversed()).first() }
                        .sortedWith(VersionComparator.reversed())
                    LOG.info("sorted: $sorted")
                    versions.add(sorted.first())
                }

                val fileMap = files.groupBy { it.gameVersion.sortedWith(VersionComparator.reversed()).first() }
                    .mapValues {
                        it.value.sortedByDescending { it.fileDate }
                    }.toSortedMap(VersionComparator.reversed())

//        val sorted = files.sortedWith(compareByDescending(VersionComparator) { it.gameVersion.sortedWith(VersionComparator).last() })
                fileMap.forEach { key, list ->
                    LOG.info("version: $key")
//            LOG.info("list: $list")
                    list.forEach {
                        LOG.info("file: ${it.fileName}")
                    }
                }

                head {
                    meta {
                        name = "viewport"
                        content = "width=device-width,initial-scale=1"
                    }
                    styleLink("/api/widget.css")
                }
                body("bg-transparent") {
                    div {
                        attributes["id"] = "widget"
                        div("wrapper clearfix") {
                            val attachment = addon.attachments?.find { it.isDefault }!!

                            div("thumb") {
                                img(alt = attachment.description, src = attachment.thumbnailUrl) {
                                }
                            }
                            div("meta") {
                                span("line lead") {
                                    a {
                                        title = addon.name
                                        target = "_blank"
                                        attributes["id"] = "title-link"
                                        href = addon.webSiteURL
                                        +addon.name
                                    }
                                    small { +" by ${addon.primaryAuthorName}" }
                                }
                                span("line smaller") {
                                    +"${addon.downloadCount.toInt()} Downloads"
                                }
                                span("line small") {
                                    +addon.summary
                                }
                                span("line small") {
                                    +"version info etc.."
                                }
                                div("line bottom clearfix") {
                                    versions.forEach { version ->
                                        val file = fileMap[version]?.first()
                                        if (file != null) {
                                            a(classes = "files-button") {
                                                href = file.downloadURL
                                                target = "_blank"
                                                attributes["id"] = "download-button"
                                                +"Download ${file.fileName}"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        get("/api/url/{id}") {
            val id = call.parameters["id"]?.toInt()
                ?: throw NumberFormatException("id")
            val addon = CurseClient.getAddon(id) ?: throw AddonNotFoundException(id)
            val versions = call.parameters.getAll("version") ?: emptyList()
            val file = addon.latestFile(versions)
            call.respondRedirect(url = file.downloadURL, permanent = false)
        }

        get("/api/url/{id}/{fileid}") {
            val id = call.parameters["id"]?.toInt()
                ?: throw NumberFormatException("id")
            val fileid = call.parameters["fileid"]?.toInt()
                ?: throw NumberFormatException("fileid")
            val file = CurseClient.getAddonFile(id, fileid) ?: throw AddonFileNotFoundException(id, fileid)
            call.respondRedirect(url = file.downloadURL, permanent = false)
        }

        get("/api/img/{id}") {
            val id = call.parameters["id"]?.toInt()
                ?: throw NumberFormatException("id")
            log.info(call.parameters.entries().joinToString())
            val style = call.parameters["style"]
            val colorA = call.parameters["colorA"]
            val colorB = call.parameters["colorB"]
            val link = call.parameters.contains("link")
            val logo = call.parameters.contains("logo")

            val addon = CurseClient.getAddon(id) ?: throw AddonNotFoundException(id)
            val versions = call.parameters.getAll("version") ?: emptyList()
            val file = addon.latestFile(versions)

            val name = addon.name
                .replace("-", "--")
                .replace("_", "__")
            val fileName = file.fileName.replace(addon.name, "")
                .replace(Regex("^[\\s-._]+"), "")
                .replace("-", "--")
                .replace("_", "__")

            call.respondRedirect(permanent = false) {
                protocol = URLProtocol.HTTPS
                host = "img.shields.io"
                path("badge", "$name-$fileName-orange.svg")
                parameters["maxAge"] = "3600"

                if (logo) {
                    val logoData =
                        "data:image/png;base64," + File(Widget::class.java.getResource("/anvil.png").file).encodeBase64()
                    parameters["logo"] = logoData
                }
                if (link) {
                    val left = "https://minecraft.curseforge.com/projects/$id"
                    val right = file.downloadURL
                    parameters["link"] = left
                    parameters["link"] = right
                }
                style?.let { value ->
                    parameters["style"] = value
                }
                colorA?.let { value ->
                    parameters["colorA"] = value
                }
                colorB?.let { value ->
                    parameters["colorB"] = value
                }
            }
        }

        get("/api/img/{id}/files") {
            val id = call.parameters["id"]?.toInt()
                ?: throw NumberFormatException("id")
            val style = call.parameters["style"]
            val colorA = call.parameters["colorA"]
            val colorB = call.parameters["colorB"]
            val link = call.parameters.contains("link")
            val logo = call.parameters.contains("logo")

            val addon = CurseClient.getAddon(id) ?: throw AddonNotFoundException(id)
            val versions = call.parameters.getAll("version") ?: emptyList()
            val files = addon.files(versions)
            val count = files.count()

            val name = addon.name
                .replace("-", "--")
                .replace("_", "__")
            val label = "$count Files"
            call.respondRedirect(permanent = false) {
                protocol = URLProtocol.HTTPS
                host = "img.shields.io"
                path("badge", "$name-$label-orange.svg")
                parameters["maxAge"] = "3600"

                if (logo) {
                    val logoData =
                        "data:image/png;base64," + File(Widget::class.java.getResource("/anvil.png").file).encodeBase64()
                    parameters["logo"] = logoData
                }
                if (link) {
                    val left = "https://minecraft.curseforge.com/projects/$id"
                    val right = "https://minecraft.curseforge.com/projects/$id/files"
                    parameters["link"] = left
                    parameters["link"] = right
                }
                style?.let { value ->
                    parameters["style"] = value
                }
                colorA?.let { value ->
                    parameters["colorA"] = value
                }
                colorB?.let { value ->
                    parameters["colorB"] = value
                }
            }
        }

        get("/api/img/{id}/{fileid}") {
            val id = call.parameters["id"]?.toInt()
                ?: throw NumberFormatException("id")
            val fileid = call.parameters["fileid"]?.toInt()
                ?: throw NumberFormatException("fileid")
            val style = call.parameters["style"]
            val colorA = call.parameters["colorA"]
            val colorB = call.parameters["colorB"]
            val link = call.parameters.contains("link")
            val logo = call.parameters.contains("logo")

            val addon = CurseClient.getAddon(id) ?: throw AddonNotFoundException(id)
            val file = CurseClient.getAddonFile(id, fileid) ?: throw AddonFileNotFoundException(id, fileid)

            val name = addon.name
                .replace("-", "--")
                .replace("_", "__")
            val fileName = file.fileName.replace(addon.name, "")
                .replace(Regex("^[\\s-._]+"), "")
                .replace("-", "--")
                .replace("_", "__")

            call.respondRedirect(permanent = false) {
                protocol = URLProtocol.HTTPS
                host = "img.shields.io"
                path("badge", "$name-$fileName-orange.svg")
                parameters["maxAge"] = "3600"

                if (logo) {
                    val logoData =
                        "data:image/png;base64," + File(Widget::class.java.getResource("/anvil.png").file).encodeBase64()
                    parameters["logo"] = logoData
                }
                if (link) {
                    val left = "https://minecraft.curseforge.com/projects/$id"
                    val right = file.downloadURL
                    parameters["link"] = left
                    parameters["link"] = right
                }
                style?.let { value ->
                    parameters["style"] = value
                }
                colorA?.let { value ->
                    parameters["colorA"] = value
                }
                colorB?.let { value ->
                    parameters["colorB"] = value
                }
            }
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