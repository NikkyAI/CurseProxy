package moe.nikky.curseproxy.curse

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import moe.nikky.curseproxy.koinCtx
import moe.nikky.curseproxy.model.Addon
import moe.nikky.curseproxy.model.AddonFile
import mu.KotlinLogging
import mu.withLoggingContext
import org.koin.core.context.GlobalContext

/**
 * Created by nikky on 25/05/18.
 * @author Nikky
 * @version 1.0
 */

//TODO: replace fuel with ktor-client-okhhtp

object CurseClient {
    private val json by koinCtx.inject<Json>()
    private val client by koinCtx.inject<HttpClient>()
    private const val ADDON_API = "https://addons-ecs.forgesvc.net/api/v2"

    private val logger = KotlinLogging.logger { }
    private const val DEFAULT_RETRIES = 3

    suspend fun getAddon(
        projectId: Int,
        retry_count: Int = DEFAULT_RETRIES,
    ): Addon? = withLoggingContext(
        "request" to "addon",
        "projectId" to projectId.toString()
    ) {
        val url = "$ADDON_API/addon/$projectId"

        try {
            client.get<Addon>(url)
        } catch (e: Exception) {
            logger.error(e) { "GET addon $projectId failed with exception" }
            if (retry_count > 0) {
                delay(100)
                getAddon(projectId, retry_count - 1)
            } else {
                null
            }
        }
    }

    suspend fun getAddons(
        projectIds: List<Int>,
    ): List<Addon>? = withLoggingContext(
        "request" to "addons",
        "ids" to projectIds.toString()
    ) {
        val url = "$ADDON_API/addon"
        try {
            client.post<List<Addon>>(url) {
                contentType(ContentType.Application.Json)
                body = projectIds
            }
        } catch (e: IOException) {
            logger.error(e) { "GET addons projectIds: ${projectIds.size} failed with exception" }
            null
        }
    }

    suspend fun getAddonDescription(
        projectId: Int,
        retry_count: Int = DEFAULT_RETRIES,
    ): String? = withLoggingContext(
        "request" to "description",
        "projectId" to projectId.toString()
    ) {
        val url = "$ADDON_API/addon/$projectId/description"

        try {
            client.get<String>(url)
        } catch (e: Exception) {
            logger.error(e) { "GET description $projectId failed with exception" }
            if (retry_count > 0) {
                delay(100)
                getAddonDescription(projectId, retry_count - 1)
            } else {
                null
            }
        }
    }

    suspend fun getAddonFile(
        projectId: Int,
        fileId: Int,
        retry_count: Int = DEFAULT_RETRIES,
    ): AddonFile? = withLoggingContext(
        "request" to "file",
        "projectId" to projectId.toString(),
        "fileId" to fileId.toString()
    ) {
        val url = "$ADDON_API/addon/$projectId/file/$fileId"

        try {
            client.get<AddonFile>(url)
        } catch (e: Exception) {
            logger.error(e) { "GET file $projectId $fileId failed with exception" }
            if (retry_count > 0) {
                delay(100)
                getAddonFile(projectId, fileId, retry_count - 1)
            } else {
                null
            }
        }
    }

    suspend fun getAddonFiles(
        projectId: Int,
        retry_count: Int = DEFAULT_RETRIES,
    ): List<AddonFile>? = withLoggingContext(
        "request" to "files",
        "projectId" to projectId.toString()
    ) {
        val url = "$ADDON_API/addon/$projectId/files"
        logger.info { "GET $url" }

        try {
            client.get<List<AddonFile>>(url)
        } catch (e: IOException) {
            if (retry_count > 0) {
                logger.warn { "GET files $projectId failed with exception: ${e.message}" }
                delay(100)
                getAddonFiles(projectId, retry_count - 1)
            } else {
                logger.error(e) { "GET files $projectId failed with exception: $url" }
                null
            }
        }
    }

    @Serializable
    data class AddonFileKey(
        @SerialName("AddonId") val addonId: Int,
        @SerialName("FileId") val fileId: Int,
    ) {
        val short: String get() = "$addonId/$fileId"
    }

    suspend fun getAddonFiles(
        keys: List<AddonFileKey>,
        retry_count: Int = DEFAULT_RETRIES,
    ): Map<Int, List<AddonFile>>? = withLoggingContext(
        "request" to "addonFiles",
        "keys" to keys.joinToString(",", "[", "]") { it.short }
    ) {
        val url = "$ADDON_API/addon/files"

        try {
            client.post<Map<Int, List<AddonFile>>>(url) {
                contentType(ContentType.Application.Json)
                body = keys
            }
        } catch (e: Exception) {
            logger.error(e) { "GET addonFiles keys: ${keys.size} failed with exception" }
            if (retry_count > 0) {
                delay(100)
                getAddonFiles(keys)
            } else {
                null
            }
        }
//        val (request, response, result) = url
//            .httpPost()
//            .pretendToBeTwitchapp()
//            .body(json.stringify(AddonFileKey.serializer().list, keys))
//            .awaitObjectResponseResult(
//                kotlinxDeserializerOf(
//                    json = json, loader = MapSerializer(Int.serializer(), AddonFile.serializer().list)
//                )
//            )
//
//        return when (result) {
//            is Result.Success -> {
//                result.value
//            }
//            is Result.Failure -> {
//                LOG.error("failed $request $response ${result.error}")
//                null
//            }
//        }
    }

    suspend fun getAddonFileChangelog(
        projectId: Int,
        fileId: Int,
        retry_count: Int = DEFAULT_RETRIES,
    ): String? = withLoggingContext(
        "request" to "changelog",
        "projectId" to projectId.toString(),
        "fileId" to fileId.toString()
    ) {
        val url = "$ADDON_API/addon/$projectId/file/$fileId/changelog"

        client.get<String>(url)
        try {
            client.get<String>(url)
        } catch (e: Exception) {
            logger.error(e) { "GET changelog $projectId $fileId failed with exception" }
            if (retry_count > 0) {
                delay(100)
                getAddonFileChangelog(projectId, fileId, retry_count - 1)
            } else {
                null
            }
        }
//        val (request, response, result) = url
//            .httpGet()
//            .pretendToBeTwitchapp()
//            .awaitStringResponseResult()
//        return when (result) {
//            is Result.Success -> {
//                result.value
//            }
//            is Result.Failure -> {
//                LOG.error("failed $request $response ${result.error}")
//                null
//            }
//        }
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

    suspend fun search(
        gameId: Int,
        sectionId: Int? = null,
        categoryIds: List<Int>? = null,
        sort: AddonSortMethod = AddonSortMethod.Featured,
        isSortDescending: Boolean = true,
        gameVersions: List<String>? = null,
        index: Int = 0,
        pageSize: Int = 1000,
        searchFilter: String? = null,
        retry_count: Int = DEFAULT_RETRIES,
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

        return try {
            client.get<List<Addon>>(url) {
                parameters.forEach { (k, v) ->
                    parameter(k, v)
                }
            }
        } catch (e: Exception) {
            if (retry_count > 0) {
                logger.warn(e) { "exception during search" }
                delay(100)
                search(
                    gameId = gameId,
                    sectionId = sectionId,
                    categoryIds = categoryIds,
                    sort = sort,
                    isSortDescending = isSortDescending,
                    gameVersions = gameVersions,
                    index = index,
                    pageSize = pageSize,
                    searchFilter = searchFilter,
                    retry_count = retry_count - 1
                )
            } else {
                logger.error(e) { "exception during search" }
                null
            }
        }
//        val (request, response, result) = url
//            .httpGet(parameters = parameters.filter { (_, value) ->
//                value != null
//            }
////                .also { LOG.debug("parameters: $it") }
//            )
//            .pretendToBeTwitchapp()
//            .awaitStringResponseResult()
////            .awaitObjectResponseResult(kotlinxDeserializerOf(json = json, loader = Addon.serializer().list))
//
//        LOG.debug("curl: ${request.cUrlString()}")
//
//        return when (result) {
//            is Result.Success -> {
//                json.parse(Addon.serializer().list, result.value)
//            }
//            is Result.Failure -> {
//                LOG.error("failed $request $response ${result.error}")
//                null
//            }
//        }
    }

    suspend fun searchAll(
        gameId: Int,
        sectionId: Int? = null,
        categoryIds: List<Int>? = null,
        sort: AddonSortMethod = AddonSortMethod.Featured,
        isSortDescending: Boolean = true,
        gameVersions: List<String>? = null,
        pageSize: Int = 1000,
        searchFilter: String? = null,
    ): List<Addon> {
        require(pageSize <= 1000) { "curse api limits pagesize to 1000" }

        val n = 4
        var index = 0
        val results: MutableList<Addon> = mutableListOf()
        var done = false
        while (!done) {

//                (0 until n).map {
            val page = search(
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

            if(page == null) {
                logger.error { "error on index: $index" }
                break
            }

            logger.debug { "index: $index size: ${page.size}" }

            results += page
            if (page.size < pageSize) {
                done = true
                break
            }
            index += pageSize / 2
//                }
        }
        logger.info { "search results: ${results.size}" }
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