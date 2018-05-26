package moe.nikky.curseproxy.curse

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import moe.nikky.curseproxy.LOG
import moe.nikky.curseproxy.curse.auth.curseAuth
import moe.nikky.curseproxy.model.CurseAddon
import moe.nikky.curseproxy.model.AddonFile
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

/**
 * Created by nikky on 25/05/18.
 * @author Nikky
 * @version 1.0
 */

object CurseClient : KoinComponent {
    private val mapper: ObjectMapper by inject()
    private const val ADDON_API = "https://addons-v2.forgesvc.net/api"

    fun getAddon(projectId: Int): CurseAddon? {
        val url = "$ADDON_API/addon/$projectId"
        val (request, response, result) = url
                .httpGet()
                .curseAuth()
                .responseString()
        return when (result) {
            is Result.Success -> {
                mapper.readValue(result.value)
            }
            is Result.Failure -> {
                LOG.error("failed $request $response ${result.error}")
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
        return when (result) {
            is Result.Success -> {
                mapper.readValue(result.value)
            }
            is Result.Failure -> {
                LOG.error("failed $request $response ${result.error}")
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
        return when (result) {
            is Result.Success -> {
                mapper.readValue(result.value)
            }
            is Result.Failure -> {
                LOG.error("failed $request $response ${result.error}")
                null
            }
        }
    }

    enum class AddonSortMethod {
        Featured,
        Popularity,
        LastUpdated,
        Name,
        Author,
        TotalDownloads,
        Category,
        GameVersion
    }

    fun getAddonsByCriteria(
            gameId: Int,
            sectionId: Int = -1,
            categoryId: Int = -1,
            sort: AddonSortMethod = AddonSortMethod.Featured,
            isSortDescending: Boolean = true,
            gameVersion: String? = null,
            index: Int = 0,
            pageSize: Int = 1000,
            searchFilter: String? = null): List<CurseAddon>? {
        val url = "$ADDON_API/addon/search"
        val (request, response, result) = url
                .httpGet(parameters = listOf<Pair<String, Any?>>(
                        "gameId" to gameId,
                        "sectionId" to sectionId,
                        "categoryId" to categoryId,
                        "gameVersion" to gameVersion,
                        "index" to index,
                        "pageSize" to pageSize,
                        "searchFilter" to searchFilter,
                        "sort" to sort,
                        "sortDescending" to isSortDescending
                ))
                .curseAuth()
                .responseString()
        return when (result) {
            is Result.Success -> {
                mapper.readValue(result.value)
            }
            is Result.Failure -> {
                LOG.error("failed $request $response ${result.error}")
                null
            }
        }
    }
    fun getAllAddonsByCriteria(
            gameId: Int,
            sectionId: Int = -1,
            categoryId: Int = -1,
            sort: AddonSortMethod = AddonSortMethod.Featured,
            isSortDescending: Boolean = true,
            gameVersion: String? = null,
            pageSize: Int = 1000,
            searchFilter: String? = null): List<CurseAddon>? {
        var index = 0
        val results = mutableListOf<CurseAddon>()
        while (true) {
            val page = getAddonsByCriteria(gameId, sectionId, categoryId, sort, isSortDescending, gameVersion, index, pageSize, searchFilter)
                    ?: emptyList()
            results += page
            if (page.size < pageSize) {
                break
            }
            index += pageSize
        }
        return results
    }
}