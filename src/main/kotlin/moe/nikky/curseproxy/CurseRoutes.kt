package moe.nikky.curseproxy

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import moe.nikky.curseproxy.curse.CurseClient

fun Route.curse() {

    get("/api/addon/{id}") {
        val id = call.parameters["id"]?.toInt()
            ?: throw NumberFormatException("id")
        CurseClient.getAddon(id)?.let { addon ->
            call.respond(addon)
        } ?: call.respond(status = HttpStatusCode.NotFound, message = "addon with id $id does not exist")
    }

    post("/api/addon") {
        val ids = call.receive<List<Int>>()
        CurseClient.getAddons(ids, true)?.let { addons ->
            call.respond(addons)
        } ?: call.respond(status = HttpStatusCode.NotFound, message = "error")
    }

    get("/api/addon/{id}/description") {
        val id = call.parameters["id"]?.toInt()
            ?: throw NumberFormatException("id")
        CurseClient.getAddonDescription(id)?.let { description ->
            call.respondText(description, ContentType.Text.Html)
        } ?: call.respond(status = HttpStatusCode.NotFound, message = "description of addon with id $id does not exist")
    }

    get("/api/addon/{id}/file/{file}") {
        val id = call.parameters["id"]?.toInt()
            ?: throw NumberFormatException("id")
        val fileID = call.parameters["file"]?.toInt()
            ?: throw NumberFormatException("file")
        CurseClient.getAddonFile(id, fileID)?.let { file ->
            call.respond(file)
        } ?: call.respond(status = HttpStatusCode.NotFound, message = "addon file with id $id:$fileID does not exist")
    }

    get("/api/addon/{id}/file/{file}/changelog") {
        val id = call.parameters["id"]?.toInt()
            ?: throw NumberFormatException("id")
        val fileID = call.parameters["file"]?.toInt()
            ?: throw NumberFormatException("file")
        CurseClient.getAddonChangelog(id, fileID)?.let { changelog ->
            call.respondText(changelog, ContentType.Text.Html)
        } ?: call.respond(status = HttpStatusCode.NotFound, message = "addon file with id $id:$fileID does not exist")
    }

    get("/api/addon/{id}/files") {
        val id = call.parameters["id"]?.toInt()
            ?: throw NumberFormatException("id")
        CurseClient.getAddonFiles(id)?.let { files ->
            call.respond(files)
        } ?: call.respond(status = HttpStatusCode.NotFound, message = "addon with id $id does not exist")
    }
}