package moe.nikky.curseproxy.graphql.schema

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.slf4j.MDCContext
import moe.nikky.curseproxy.curse.CurseClient
import moe.nikky.curseproxy.data.CurseDAO
import moe.nikky.curseproxy.graphql.AuthorizedContext
import moe.nikky.curseproxy.model.*
import moe.nikky.curseproxy.util.measureMillisAndReport
import mu.KotlinLogging
import kotlin.time.measureTimedValue

class CurseQueryService(
    private val curseDAO: CurseDAO,
) : Query {
    private val logger = KotlinLogging.logger {}

    private inline fun <T : Any, R : Any> List<R>.filter(
        value: T?,
        valueList: List<T>?,
        function: (R, List<T>) -> Boolean,
    ): List<R> {
        val list = (valueList.orEmpty() + listOfNotNull(value))
        if (list.isEmpty()) return this
        return this.filter { addon ->
            function(addon, list)
        }
    }

    suspend fun test(context: AuthorizedContext): String {
        return "context: $context"

    }

    @GraphQLDescription("look up curseforge addon data")
    @Suppress("unused")
    suspend fun addons(
        gameId: Int? = null, gameIdList: List<Int>? = null,
        category: String? = null, categoryList: List<String>? = null,
        gameVersion: String? = null, gameVersionList: List<String>? = null,
        id: Int? = null, idList: List<Int>? = null,
        name: String? = null, nameList: List<String>? = null,
        slug: String? = null, slugList: List<String>? = null,
        section: String? = null, sectionList: List<String>? = null,
        status: ProjectStatus? = null, statusList: List<ProjectStatus>? = null,
    ): List<Addon> = measureMillisAndReport("addons", logger::info) {
        var (addons, duration) = measureTimedValue { curseDAO.getAllAddons() }
        logger.info { "retried ${addons.size} addons in $duration" }
        addons = addons.filter(gameId, gameIdList) { addon, list ->
            addon.gameId in list
        }
        addons = addons.filter(id, idList) { addon, list ->
            addon.id in list
        }
        addons = addons.filter(category, categoryList) { addon, list ->
            addon.categories.any { it.name in list } || addon.categorySection.name in list
        }
        addons = addons.filter(gameVersion, gameVersionList) { addon, list ->
            addon.latestFiles.any { it.gameVersion.all { it in list } }
                    || addon.gameVersionLatestFiles.any { it.gameVersion in list }
        }
        addons = addons.filter(name, nameList) { addon, list ->
            addon.name in list
        }
        addons = addons.filter(slug, slugList) { addon, list ->
            addon.slug in list
        }
        addons = addons.filter(section, sectionList) { addon, list ->
            addon.categorySection.name in list
        }
        addons = addons.filter(status, statusList) { addon, list ->
            addon.status in list
        }
        addons
    }

    @GraphQLDescription("look up files")
    @Suppress("unused")
    suspend fun files(
        id: Int,
        fileStatus: FileStatus? = null, fileStatusList: List<FileStatus>? = null,
        gameVersion: String? = null, gameVersionList: List<String>? = null,
        releaseType: FileType? = null, releaseTypeList: List<FileType>? = null,
    ): List<AddonFile> = measureMillisAndReport("files", logger::info) {
        var (files, duration) = measureTimedValue { CurseClient.getAddonFiles(id)!! }
        logger.info { "retried ${files.size} files in $duration" }
        files = files.filter(fileStatus, fileStatusList) { file, list ->
            file.fileStatus in list
        }
        files = files.filter(gameVersion, gameVersionList) { file, list ->
            file.gameVersion.any { it in list }
        }
        files = files.filter(releaseType, releaseTypeList) { file, list ->
            file.releaseType in list
        }
        files
    }

    @GraphQLDescription("sends a search query to curseforge")
    @Suppress("unused")
    suspend fun addonSearch(
        @GraphQLDescription("search filter")
        searchFilter: String? = null,
        @GraphQLDescription("game id")
        gameID: Int? = null,
        @GraphQLDescription("Game versions (and)")
        gameVersions: List<String>? = null,
        @GraphQLDescription("Category ids (and)")
        categoryIds: List<Int>? = null,
        @GraphQLDescription("section id")
        section: Int? = null,
    ): List<Addon> = CurseClient.searchAll(
        gameId = gameID ?: 432,
        sectionId = section,
        gameVersions = gameVersions,
        searchFilter = searchFilter,
        categoryIds = categoryIds,
        sort = CurseClient.AddonSortMethod.LastUpdated
    ).also {
        logger.info { it.first() }
    }

    @GraphQLDescription("sends a search query to curseforge")
    @Suppress("unused")
    suspend fun addonSearchFiles(
        @GraphQLDescription("search filter")
        searchFilter: String? = null,
        @GraphQLDescription("game id")
        gameID: Int? = null,
        @GraphQLDescription("Game versions (and)")
        gameVersions: List<String>? = null,
        @GraphQLDescription("Category ids (and)")
        categoryIds: List<Int>? = null,
        @GraphQLDescription("section id")
        section: Int? = null,
    ): List<AddonWithFiles> = coroutineScope {
        val addons = CurseClient.searchAll(
            gameId = gameID ?: 432,
            sectionId = section,
            gameVersions = gameVersions,
            searchFilter = searchFilter,
            categoryIds = categoryIds,
            sort = CurseClient.AddonSortMethod.LastUpdated
        )
        logger.info { "addons: ${addons.size}" }
        val addonsWithFiles = addons.map { addon ->
            async(Dispatchers.IO + MDCContext()) {
                val files = CurseClient.getAddonFiles(addon.id) ?: emptyList()
                AddonWithFiles(
                    id = addon.id,
                    name = addon.name,
                    authors = addon.authors,
                    attachments = addon.attachments,
                    websiteUrl = addon.websiteUrl,
                    gameId = addon.gameId,
                    summary = addon.summary,
                    defaultFileId = addon.defaultFileId,
                    downloadCount = addon.downloadCount,
                    latestFiles = addon.latestFiles,
                    categories = addon.categories,
                    status = addon.status,
                    categorySection = addon.categorySection,
                    slug = addon.slug,
                    gameVersionLatestFiles = addon.gameVersionLatestFiles,
                    popularityScore = addon.popularityScore,
                    gamePopularityRank = addon.gamePopularityRank,
                    gameName = addon.gameName,
                    portalName = addon.portalName,
                    dateModified = addon.dateModified,
                    dateCreated = addon.dateCreated,
                    dateReleased = addon.dateReleased,
                    isAvailable = addon.isAvailable,
                    primaryLanguage = addon.primaryLanguage,
                    isFeatured = addon.isFeatured,
                    files = files
                )
            }
        }.awaitAll()

//        val addonsWithFiles = addons.map {
//            async(Dispatchers.IO) {
//                AddonWithFiles(
//                    id = it.id,
//                    name = it.name,
//                    authors = it.authors,
//                    attachments = it.attachments,
//                    websiteUrl = it.websiteUrl,
//                    gameId = it.gameId,
//                    summary = it.summary,
//                    defaultFileId = it.defaultFileId,
//                    downloadCount = it.downloadCount,
//                    latestFiles = it.latestFiles,
//                    categories = it.categories,
//                    status = it.status,
//                    categorySection = it.categorySection,
//                    slug = it.slug,
//                    gameVersionLatestFiles = it.gameVersionLatestFiles,
//                    popularityScore = it.popularityScore,
//                    gamePopularityRank = it.gamePopularityRank,
//                    gameName = it.gameName,
//                    portalName = it.portalName,
//                    dateModified = it.dateModified,
//                    dateCreated = it.dateCreated,
//                    dateReleased = it.dateReleased,
//                    isAvailable = it.isAvailable,
//                    primaryLanguage = it.primaryLanguage,
//                    isFeatured = it.isFeatured,
//                    files = CurseClient.getAddonFiles(it.id) ?: emptyList()
//                )
//            }
//        }.awaitAll()
        addonsWithFiles
    }
}
