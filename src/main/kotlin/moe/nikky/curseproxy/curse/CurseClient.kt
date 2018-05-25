package moe.nikky.curseproxy.curse

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import moe.nikky.curseproxy.curse.auth.curseAuth
import moe.nikky.curseproxy.model.Addon
import moe.nikky.curseproxy.model.AddonFile
import org.koin.Koin
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

/**
 * Created by nikky on 25/05/18.
 * @author Nikky
 * @version 1.0
 */

object CurseClient : KoinComponent {
    private val mapper: ObjectMapper by inject()
    private val ADDON_API = "https://addons-v2.forgesvc.net/api"

    fun getAddon(projectId: Int): Addon? {
        val url = "$ADDON_API/addon/$projectId"
        val (request, response, result) = url
                .httpGet()
                .curseAuth()
                .responseString()
        return when(result) {
            is Result.Success -> {
                Koin.logger.debug("addon json: ${result.value}")
                mapper.readValue(result.value)
            }
            is Result.Failure -> {
                Koin.logger.log("failed $request $response ${result.error}")
                null
            }
        }
    }

    fun getAddonFiles(projectId: Int): List<AddonFile>? {
        val url = "$ADDON_API/addon/$projectId/files"
        val (request, response, result) = url
                .httpGet()
                .curseAuth()
                .responseString()
        return when(result) {
            is Result.Success -> {
                Koin.logger.debug("files json: ${result.value}")
                mapper.readValue(result.value)
            }
            is Result.Failure -> {
                Koin.logger.log("failed $request $response ${result.error}")
                null
            }
        }
    }
    fun getAddonFile(projectId: Int, fileId: Int): AddonFile? {
        val url = "$ADDON_API/addon/$projectId/file/$fileId"
        val (request, response, result) = url
                .httpGet()
                .curseAuth()
                .responseString()
        return when(result) {
            is Result.Success -> {
                Koin.logger.debug("file json: ${result.value}")
                mapper.readValue(result.value)
            }
            is Result.Failure -> {
                Koin.logger.log("failed $request $response ${result.error}")
                null
            }
        }
    }
}