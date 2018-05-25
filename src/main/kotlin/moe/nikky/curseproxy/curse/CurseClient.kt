package moe.nikky.curseproxy.curse

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import moe.nikky.curseproxy.curse.auth.AuthToken
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
    val ADDON_API = "https://addons-v2.forgesvc.net/api"

    fun getAddon(id: Int): Addon? {
        val url = "$ADDON_API/addon/$id"
        val (request, response, result) = url
                .httpGet()
                .curseAuth()
                .responseString()
        return when(result) {
            is Result.Success -> {
                Koin.logger.debug("addon json: ${result.value}")
//                return result.value
                mapper.readValue(result.value)
            }
            is Result.Failure -> {
                Koin.logger.log("failed $request $response ${result.error}")
                null
            }
        }
    }

    fun getAddonFiles(id: Int): List<AddonFile>? {
        val url = "$ADDON_API/addon/$id/files"
        val (request, response, result) = url
                .httpGet()
                .curseAuth()
                .responseString()
        return when(result) {
            is Result.Success -> {
                Koin.logger.debug("files json: ${result.value}")
//                return result.value
                mapper.readValue(result.value)
            }
            is Result.Failure -> {
                Koin.logger.log("failed $request $response ${result.error}")
                null
            }
        }

    }
}