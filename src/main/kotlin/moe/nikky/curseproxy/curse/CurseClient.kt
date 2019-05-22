package moe.nikky.curseproxy.curse

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.extensions.cUrlString
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
    private const val ADDON_API = "https://addons-ecs.forgesvc.net/api/v2"

    suspend fun getAddon(projectId: Int, ignoreError: Boolean = false): CurseAddon? {
        val url = "$ADDON_API/addon/$projectId"
        val (request, response, result) = url
            .httpGet()
            .curseAuth()
            .awaitStringResponseResult()
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
            .awaitStringResponseResult()
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
            .awaitStringResponseResult()
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
            .awaitStringResponseResult()
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
            .awaitStringResponseResult()
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
            .awaitStringResponseResult()
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
            .awaitStringResponseResult()
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
        sectionId: Int? = null,
        categoryIds: List<Int>? = null,
        sort: AddonSortMethod = AddonSortMethod.Featured,
        isSortDescending: Boolean = true,
        gameVersions: List<String>? = null,
        index: Int = 0,
        pageSize: Int = 1000,
        searchFilter: String? = null
    ): List<CurseAddon>? {
        val url = "$ADDON_API/addon/search"
        val parameters = mutableListOf(
            "gameID" to gameId,
//            "gameVersion" to gameVersions,
            "sectionId" to sectionId,
            "index" to index,
            "pageSize" to pageSize,
            "searchFilter" to searchFilter,
            "sort" to sort,
            "sortDescending" to isSortDescending
        )
        gameVersions?.forEach { gameVersion ->
            parameters += "gameVersion" to gameVersion
        }
        categoryIds?.forEach { categoryId ->
            parameters += "categoryId" to categoryId
        }
//        sectionIds?.forEach { sectionId ->
//            parameters += "sectionId" to sectionId
//        }

        val (request, response, result) = url
            .httpGet(parameters = parameters.filter { (_, value) ->
                value != null
            }
//                .also { LOG.debug("parameters: $it") }
            )
            .curseAuth()
            .awaitStringResponseResult()

        LOG.debug("curl: ${request.cUrlString()}")

        return when (result) {
            is Result.Success -> {
                mapper.readValue(result.value)
            }
            is Result.Failure -> {
//                LOG.error("failed $request $response ${result.error}")
                null
            }
        }
    }

    suspend fun getAllAddonsByCriteria(
        gameId: Int,
        sectionId: Int? = null,
        categoryIds: List<Int>? = null,
        sort: AddonSortMethod = AddonSortMethod.Featured,
        isSortDescending: Boolean = true,
        gameVersions: List<String>? = null,
        pageSize: Int = 1000,
        searchFilter: String? = null
    ): List<CurseAddon> {
        require(pageSize <= 1000) { "curse api limits pagesize to 1000" }

        val n = 4
        var index = 0
        val results: MutableList<CurseAddon> = mutableListOf()
        var done = false
        while (!done) {
            results += coroutineScope {
                (0 until n).map {
                    async {
                        val page = getAddonsByCriteria(
                            gameId,
                            sectionId,
                            categoryIds,
                            sort,
                            isSortDescending,
                            gameVersions,
                            index,
                            pageSize,
                            searchFilter
                        )
                            ?: emptyList()

                        if (page.size < pageSize) {
                            done = true
                        }
                        index += pageSize - 1
                        page
                    }
                }

            }.awaitAll().flatten()
        }
        return results.toList()
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