package moe.nikky.curseproxy

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.html.respondHtml
import io.ktor.http.URLProtocol
import io.ktor.http.content.default
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.request.header
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.routing
import kotlinx.coroutines.runBlocking
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.br
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.img
import kotlinx.html.meta
import kotlinx.html.p
import kotlinx.html.small
import kotlinx.html.span
import kotlinx.html.styleLink
import kotlinx.html.title
import kotlinx.serialization.json.Json
import moe.nikky.curseproxy.curse.CurseClient
import moe.nikky.curseproxy.curse.Widget
import moe.nikky.curseproxy.curse.files
import moe.nikky.curseproxy.curse.latestFile
import moe.nikky.curseproxy.exceptions.AddonFileNotFoundException
import moe.nikky.curseproxy.exceptions.AddonNotFoundException
import moe.nikky.curseproxy.graphql.AppSchema
import moe.nikky.encodeBase64
import org.koin.ktor.ext.inject
import voodoo.data.curse.FileID
import voodoo.data.curse.ProjectID
import java.io.File

@Suppress("unused")
fun Application.routes() {

    routing {
        val appSchema: AppSchema by inject()
        val json: Json by inject()
        graphql(log, json, appSchema.schema)
        curse()

        static("/") {
            files("static")
            default("index.html")
        }

        get("/test/exception") {
            throw AddonFileNotFoundException(ProjectID(1234), FileID(5678))
        }

        get("/api/widget/{id}") {
            val id = ProjectID(call.parameters["id"]?.toInt()
                ?: throw NumberFormatException("id"))
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
                                        href = addon.websiteUrl
                                        +addon.name
                                    }
                                    small { +" by ${addon.authors.joinToString { it.name }}" }
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
                                                href = file.downloadUrl
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
            val id = ProjectID(call.parameters["id"]?.toInt()
                ?: throw NumberFormatException("id"))
            val addon = CurseClient.getAddon(id) ?: throw AddonNotFoundException(id)
            val versions = call.parameters.getAll("version") ?: emptyList()
            val file = addon.latestFile(versions)
            call.respondRedirect(url = file.downloadUrl, permanent = false)
        }

        get("/api/url/{id}/{fileid}") {
            val id = ProjectID(call.parameters["id"]?.toInt()
                ?: throw NumberFormatException("id"))
            val fileid = FileID(call.parameters["fileid"]?.toInt()
                ?: throw NumberFormatException("fileid"))
            val file = CurseClient.getAddonFile(id, fileid) ?: throw AddonFileNotFoundException(id, fileid)
            call.respondRedirect(url = file.downloadUrl, permanent = false)
        }

        get("/api/img/{id}") {
            val id = ProjectID(call.parameters["id"]?.toInt()
                ?: throw NumberFormatException("id"))
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
                port = 443
                path("badge", "$name-$fileName-orange.svg")
                parameters["maxAge"] = "3600"

                if (logo) {
                    val logoData =
                        "data:image/png;base64," + File(Widget::class.java.getResource("/anvil.png").file).encodeBase64()
                    parameters["logo"] = logoData
                }
                if (link) {
                    val left = "https://minecraft.curseforge.com/projects/$id"
                    val right = file.downloadUrl
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
            val id = ProjectID(call.parameters["id"]?.toInt()
                ?: throw NumberFormatException("id"))
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
                port = 443
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
            val id = ProjectID(call.parameters["id"]?.toInt()
                ?: throw NumberFormatException("id"))
            val fileid = FileID(call.parameters["fileid"]?.toInt()
                ?: throw NumberFormatException("fileid"))
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
                port = 443
                path("badge", "$name-$fileName-orange.svg")
                parameters["maxAge"] = "3600"

                if (logo) {
                    val logoData =
                        "data:image/png;base64," + File(Widget::class.java.getResource("/anvil.png").file).encodeBase64()
                    parameters["logo"] = logoData
                }
                if (link) {
                    val left = "https://minecraft.curseforge.com/projects/$id"
                    val right = file.downloadUrl
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