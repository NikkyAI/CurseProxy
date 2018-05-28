package moe.nikky.curseproxy

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import moe.nikky.curseproxy.curse.CurseClient
import moe.nikky.curseproxy.dao.Addons.id

fun Route.curse() {

    get("/api/addon/{id}") {
        val id = call.parameters["id"]?.toInt()
                ?: throw NumberFormatException("id")
        val addon = CurseClient.getAddon(id)
                ?: call.respond(status = HttpStatusCode.NotFound, message = "addon with id $id does not exist")
        call.respond(addon)
    }

    post("/api/addon") {
        val ids = call.receive<Array<Int>>()
        val addons = CurseClient.getAddons(ids)
                ?: call.respond(status = HttpStatusCode.NotFound, message = "error")
        call.respond(addons)
    }

    get("/api/addon/{id}/description") {
        val id = call.parameters["id"]?.toInt()
                ?: throw NumberFormatException("id")
        val addon = CurseClient.getAddonDescription(id)
                ?: call.respond(status = HttpStatusCode.NotFound, message = "addon with id $id does not exist")
        call.respond(addon)
    }

    get("/api/addon/{id}/file/{file}") {
        val id = call.parameters["id"]?.toInt()
                ?: throw NumberFormatException("id")
        val fileID = call.parameters["file"]?.toInt()
                ?: throw NumberFormatException("file")
        val file = CurseClient.getAddonFile(id, fileID)
                ?: call.respond(status = HttpStatusCode.NotFound, message = "addon file with id $id:$fileID does not exist")
        call.respond(file)
    }

    get("/api/addon/{id}/file/{file}/changelog") {
        val id = call.parameters["id"]?.toInt()
                ?: throw NumberFormatException("id")
        val fileID = call.parameters["file"]?.toInt()
                ?: throw NumberFormatException("file")
        val file = CurseClient.getAddonChangelog(id, fileID)
                ?: call.respond(status = HttpStatusCode.NotFound, message = "addon file with id $id:$fileID does not exist")
        call.respond(file)
    }

    get("/api/addon/{id}/files") {
        val id = call.parameters["id"]?.toInt()
                ?: throw NumberFormatException("id")
        val file = CurseClient.getAddonFiles(id)
                ?: call.respond(status = HttpStatusCode.NotFound, message = "addon with id $id does not exist")
        call.respond(file)
    }


}