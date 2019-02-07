package moe.nikky.curseproxy.curse

import awaitStringResponse
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import moe.nikky.curseproxy.LOG
import moe.nikky.curseproxy.curse.auth.curseAuth
import moe.nikky.curseproxy.model.AddonFile
import moe.nikky.curseproxy.model.CurseAddon
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

/**
 * Created by nikky on 25/05/18.
 * @author Nikky
 * @version 1.0
 */

object CurseClient : KoinComponent {
    private val mapper: ObjectMapper by inject()
    private const val ADDON_API = "https://addons-ecs.forgesvc.net/api"

    suspend fun getAddon(projectId: Int, ignoreError: Boolean = false): CurseAddon? {
        val url = "$ADDON_API/addon/$projectId"
        val (request, response, result) = url
                .httpGet()
                .curseAuth()
                .awaitStringResponse()
        return when (result) {
            is Result.Success -> {
                mapper.readValue(result.value)
            }
            is Result.Failure -> {
                if (!ignoreError) {
                    LOG.error("failed $request $response ${result.error}")
                }
                null
            }
        }
    }

    suspend fun getAddons(projectIds: Array<Int>, ignoreErrors: Boolean = false): List<CurseAddon>? {
        val url = "$ADDON_API/addon"
        val (request, response, result) = url
                .httpPost()
                .body(mapper.writeValueAsBytes(projectIds))
                .curseAuth()
                .apply {
                    this.headers["Content-Type"] = "application/json"
//                    LOG.debug(this.cUrlString())
                }
                .awaitStringResponse()
        return when (result) {
            is Result.Success -> {
                mapper.readValue(result.value)
            }
            is Result.Failure -> {
                if (!ignoreErrors) {
                    LOG.error("failed $request $response ${result.error}")
                }
                null
            }
        }
    }

    suspend fun getAddonDescription(projectId: Int): String? {
        val url = "$ADDON_API/addon/$projectId/description"
        val (request, response, result) = url
                .httpGet()
                .curseAuth()
                .awaitStringResponse()
        return when (result) {
            is Result.Success -> {
                result.value
            }
            is Result.Failure -> {
                LOG.error("failed $request $response ${result.error}")
                null
            }
        }
    }

    suspend fun getAddonFile(projectId: Int, fileId: Int): AddonFile? {
        val url = "$ADDON_API/addon/$projectId/file/$fileId"
        val (request, response, result) = url
                .httpGet()
                .curseAuth()
                .awaitStringResponse()
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

    suspend fun getAddonFiles(projectId: Int): List<AddonFile>? {
        val url = "$ADDON_API/addon/$projectId/files"
        val (request, response, result) = url
                .httpGet()
                .curseAuth()
                .awaitStringResponse()
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

    data class AddonFileKey(
            @JsonProperty("AddonId") val addonId: Int,
            @JsonProperty("FileId") val fileId: Int
    )

    suspend fun getAddonFiles(keys: List<AddonFileKey>): Map<Int, List<AddonFile>>? {
        val url = "$ADDON_API/addon/files"
        val (request, response, result) = url
                .httpPost()
                .body(mapper.writeValueAsBytes(keys))
                .curseAuth()
                .awaitStringResponse()
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

    suspend fun getAddonChangelog(projectId: Int, fileId: Int): String? {
        val url = "$ADDON_API/addon/$projectId/file/$fileId/changelog"
        val (request, response, result) = url
                .httpGet()
                .curseAuth()
                .awaitStringResponse()
        return when (result) {
            is Result.Success -> {
                result.value
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

    suspend fun getAddonsByCriteria(
        gameId: Int,
        sectionId: Int = -1,
        categoryId: Int = -1,
        sort: AddonSortMethod = AddonSortMethod.Featured,
        isSortDescending: Boolean = true,
        gameVersions: List<String> = listOf(),
        index: Int = 0,
        pageSize: Int = 1000,
        searchFilter: String? = null): List<CurseAddon>? {
        val url = "$ADDON_API/addon/search"
        val (request, response, result) = url
            .httpGet(parameters = listOf(
                "gameID" to gameId,
                "sectionId" to sectionId,
                "categoryId" to categoryId,
                "gameVersion" to gameVersions,
                "index" to index,
                "pageSize" to pageSize,
                "searchFilter" to searchFilter,
                "sort" to sort,
                "sortDescending" to isSortDescending
            ))
            .curseAuth()
            .awaitStringResponse()
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

    suspend fun getAllAddonsByCriteria(
            gameId: Int,
            sectionId: Int = -1,
            categoryId: Int = -1,
            sort: AddonSortMethod = AddonSortMethod.Featured,
            isSortDescending: Boolean = true,
            gameVersions: List<String> = listOf(),
            pageSize: Int = 1000,
            searchFilter: String? = null): List<CurseAddon> {
        var index = 0
        val results = mutableListOf<CurseAddon>()
        while (true) {
            val page = getAddonsByCriteria(gameId, sectionId, categoryId, sort, isSortDescending, gameVersions, index, pageSize, searchFilter)
                    ?: emptyList()
            results += page

            if (page.size < pageSize) {
                break
            }
            index += pageSize - 1
        }
        return results
//        return results.distinctBy { it.id }
    }

//    fun getCategoryByID(categoryID: Int) : Category? {
//        val url = "$ADDON_API/category/$categoryID"
//        val (request, response, result) = url
//                .httpGet()
//                .curseAuth()
//                .responseString()
//        return when (result) {
//            is Result.Success -> {
//                mapper.readValue(result.value)
//            }
//            is Result.Failure -> {
//                LOG.error("failed $request $response ${result.error}")
//                null
//            }
//        }
//    }
//
//    fun getCategoryBySlug(slug: String) : Category? {
//        val url = "$ADDON_API/category"
//        val (request, response, result) = url
//                .httpGet(listOf(
//                        "slug" to slug
//                ))
//                .curseAuth()
//                .responseString()
//        return when (result) {
//            is Result.Success -> {
//                mapper.readValue(result.value)
//            }
//            is Result.Failure -> {
//                LOG.error("failed $request $response ${result.error}")
//                null
//            }
//        }
//    }
//
//    fun getCategories() : List<Category>? {
//        val url = "$ADDON_API/category"
//        val (request, response, result) = url
//                .httpGet()
//                .curseAuth()
//                .responseString()
//        return when (result) {
//            is Result.Success -> {
//                LOG.info(result.value)
//                mapper.readValue(result.value)
//            }
//            is Result.Failure -> {
//                LOG.error("failed $request $response ${result.error}")
//                null
//            }
//        }
//    }
}