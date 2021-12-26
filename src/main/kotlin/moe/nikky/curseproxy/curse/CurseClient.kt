package moe.nikky.curseproxy.curse

import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.extensions.cUrlString
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.coroutines.awaitObjectResponseResult
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.serialization.kotlinxDeserializerOf
import com.github.kittinunf.result.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.list
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import moe.nikky.curseproxy.LOG
import moe.nikky.curseproxy.model.AddonFile
import moe.nikky.curseproxy.model.Addon
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by nikky on 25/05/18.
 * @author Nikky
 * @version 1.0
 */

object CurseClient : KoinComponent {
    private val json by inject<Json>()
    private const val ADDON_API = "https://addons-ecs.forgesvc.net/api/v2"

    private fun Request.pretendToBeTwitchapp() = header("User-Agent" to "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) twitch-desktop-electron-platform/1.0.0 Chrome/73.0.3683.121 Electron/5.0.12 Safari/537.36 desklight/8.51.0")


    suspend fun getAddon(projectId: Int, ignoreError: Boolean = false): Addon? {
        val url = "$ADDON_API/addon/$projectId"
        val (request, response, result) = url
            .httpGet()
            .pretendToBeTwitchapp()
            .awaitObjectResponseResult(kotlinxDeserializerOf(json = json, loader = Addon.serializer()))
        return when (result) {
            is Result.Success -> {
                result.value
            }
            is Result.Failure -> {
                if (!ignoreError) {
                    LOG.error("failed $request $response ${result.error}")
                }
                null
            }
        }
    }

    suspend fun getAddons(projectIds: List<Int>, ignoreErrors: Boolean = false, fail: Boolean = true): List<Addon>? {
        val url = "$ADDON_API/addon"
        val (request, response, result) = url
            .httpPost()
            .pretendToBeTwitchapp()
            .jsonBody(json.stringify(Int.serializer().list, projectIds))
            .awaitObjectResponseResult(kotlinxDeserializerOf(json = json, loader = Addon.serializer().list))
        return when (result) {
            is Result.Success -> {
                result.value
            }
            is Result.Failure -> {
                if (!ignoreErrors) {
                    LOG.error("failed $request $response ${result.error}")
                }
                if (fail) {
                    throw result.error
                }
                null
            }
        }
    }

    suspend fun getAddonDescription(projectId: Int): String? {
        val url = "$ADDON_API/addon/$projectId/description"
        val (request, response, result) = url
            .httpGet()
            .pretendToBeTwitchapp()
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
            .pretendToBeTwitchapp()
            .awaitObjectResponseResult(kotlinxDeserializerOf(json = json, loader = AddonFile.serializer()))
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

    suspend fun getAddonFiles(projectId: Int): List<AddonFile>? {
        val url = "$ADDON_API/addon/$projectId/files"
        val (request, response, result) = url
            .httpGet()
            .pretendToBeTwitchapp()
            .awaitObjectResponseResult(kotlinxDeserializerOf(json = json, loader = AddonFile.serializer().list))
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

    @Serializable
    data class AddonFileKey(
        @SerialName("AddonId") val addonId: Int,
        @SerialName("FileId") val fileId: Int
    )

    suspend fun getAddonFiles(keys: List<AddonFileKey>): Map<Int, List<AddonFile>>? {
        val url = "$ADDON_API/addon/files"
        val (request, response, result) = url
            .httpPost()
            .pretendToBeTwitchapp()
            .body(json.stringify(AddonFileKey.serializer().list, keys))
            .awaitObjectResponseResult(
                kotlinxDeserializerOf(
                    json = json, loader = MapSerializer(Int.serializer(), AddonFile.serializer().list)
                )
            )

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

    suspend fun getAddonChangelog(projectId: Int, fileId: Int): String? {
        val url = "$ADDON_API/addon/$projectId/file/$fileId/changelog"
        val (request, response, result) = url
            .httpGet()
            .pretendToBeTwitchapp()
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
        pageSize: Int = 50,
        searchFilter: String? = null
    ): List<Addon>? {
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
            .pretendToBeTwitchapp()
            .awaitStringResponseResult()
//            .awaitObjectResponseResult(kotlinxDeserializerOf(json = json, loader = Addon.serializer().list))

        LOG.debug("curl: ${request.cUrlString()}")

        return when (result) {
            is Result.Success -> {
                json.parse(Addon.serializer().list, result.value)
            }
            is Result.Failure -> {
                LOG.error("failed $request $response ${result.error}")
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
        pageSize: Int = 50,
        searchFilter: String? = null
    ): List<Addon> {
        require(pageSize <= 50) { "curse api limits pagesize to 50" }

        val n = 4
        var index = 0
        val results: MutableList<Addon> = mutableListOf()
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