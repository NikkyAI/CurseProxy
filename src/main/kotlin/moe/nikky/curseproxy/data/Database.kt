package moe.nikky.curseproxy.data

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import moe.nikky.curseproxy.LOG
import moe.nikky.curseproxy.model.AddOnFileDependency
import moe.nikky.curseproxy.model.AddOnModule
import moe.nikky.curseproxy.model.Addon
import moe.nikky.curseproxy.model.AddonFile
import moe.nikky.curseproxy.model.Attachment
import moe.nikky.curseproxy.model.Author
import moe.nikky.curseproxy.model.Category
import moe.nikky.curseproxy.model.CategorySection
import moe.nikky.curseproxy.model.GameVersionLatestFile
import moe.nikky.curseproxy.util.measureMillisAndReport
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun setupCurseDatabase(dbPath: String = "curse.db"): CurseDatabase {
    val dbFile = File(dbPath)
    val dbFileExists = dbFile.exists()
    val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:$dbPath")
    if (!dbFileExists) {
        CurseDatabase.Schema.create(driver)
    }

    val intListAdapter = object : ColumnAdapter<List<Int>, String> {
        override fun decode(databaseValue: String): List<Int> =
            databaseValue.split(",").mapNotNull { it.toIntOrNull() }

        override fun encode(value: List<Int>): String =
            value.joinToString(",")
    }
    val stringListAdapter = object : ColumnAdapter<List<String>, String> {
        override fun decode(databaseValue: String): List<String> =
            databaseValue.split(",").filter { it.isNotEmpty() }

        override fun encode(value: List<String>): String = value.joinToString(",")
    }
//    val fileIdListAdapter = object : ColumnAdapter<List<FileID>, String> {
//        override fun decode(databaseValue: String): List<FileID> =
//            databaseValue.split(",")
//                .mapNotNull { it.toIntOrNull() }
//                .map { FileID(it) }
//
//        override fun encode(value: List<FileID>): String = value.joinToString(",")
//    }
    val localDateTimeAdapter = object : ColumnAdapter<LocalDateTime, String> {
        val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss")
        override fun decode(databaseValue: String): LocalDateTime = LocalDateTime.from(formatter.parse(databaseValue))
        override fun encode(value: LocalDateTime): String = formatter.format(value)
    }
//    val projectIdAdapter = object: ColumnAdapter<ProjectID, Long> {
//        override fun decode(databaseValue: Long): ProjectID = ProjectID(databaseValue.toInt())
//        override fun encode(value: ProjectID): Long = value.value.toLong()
//    }
//    val fileIdAdapter = object: ColumnAdapter<FileID, Long> {
//        override fun decode(databaseValue: Long): FileID = FileID(databaseValue.toInt())
//        override fun encode(value: FileID): Long = value.value.toLong()
//    }
    lateinit var database: CurseDatabase
    database = CurseDatabase(
        driver = driver,
        addonsAdapter = Addons.Adapter(
//            idAdapter = projectIdAdapter,
            authorIdsAdapter = intListAdapter,
            attachmentIdsAdapter = intListAdapter,
            latestFileIdsAdapter = intListAdapter,
            categoryIdsAdapter = intListAdapter,
            gameVersionLatestFileIdsAdapter = intListAdapter,
            statusAdapter = EnumColumnAdapter(),
            dateCreatedAdapter = localDateTimeAdapter,
            dateModifiedAdapter = localDateTimeAdapter,
            dateReleasedAdapter = localDateTimeAdapter
        ),
        addonFilesAdapter = AddonFiles.Adapter(
//            idAdapter = fileIdAdapter,
            fileDateAdapter = localDateTimeAdapter,
            releaseTypeAdapter = EnumColumnAdapter(),
            fileStatusAdapter = EnumColumnAdapter(),
            gameVersionAdapter = stringListAdapter
        ),
        addonFileDependenciesAdapter = AddonFileDependencies.Adapter(
//            _parentIdAdapter = fileIdAdapter,
//            addonIdAdapter = projectIdAdapter,
            typeAdapter = EnumColumnAdapter()
        ),
//        addonModulesAdapter = AddonModules.Adapter(
//            fileIdAdapter = fileIdAdapter
//        ),
//        attachmentsAdapter = Attachments.Adapter(
//            projectIdAdapter = projectIdAdapter
//        ),
//        authorsAdapter = Authors.Adapter(
//            projectIdAdapter = projectIdAdapter
//        ),
        categorySectionsAdapter = CategorySections.Adapter(
            packageTypeAdapter = EnumColumnAdapter()
        ),
        gameVersionLatestFilesAdapter = GameVersionLatestFiles.Adapter(
//            _parentIdAdapter = projectIdAdapter,
//            projectFileIdAdapter = fileIdAdapter,
            fileTypeAdapter = EnumColumnAdapter()
        )
    )
    return database
}

object Database {
    val addons = mutableMapOf<Int, Addon>()
    val authors = mutableMapOf<Int, Author>()
    val categories = mutableMapOf<Int, Category>()

}

fun CurseDatabase.store(addon: Addon) {
//    val authorIds = addon.authors.map { author ->
//        store(author)
//    }
//    val attachmentIds = addon.attachments.map { attachment ->
//        store(attachment)
//    }
//    val latestFileIds = addon.latestFiles.map { file ->
//        store(file)
//    }
//    val categoryIds = addon.categories.map { category ->
//        store(category)
//    }
//    // TODO: latestFIles
//    // TODO: categories
//    val categorySectionId = store(addon.categorySection)
//    val gameVersionLatestFileIds = addon.gameVersionLatestFiles.map { gameVersionFile ->
//        store(gameVersionFile, addon.id)
//    }

//    LOG.info("store ${addon.id}")

    val authors = addon.authors.map {
        Database.authors.getOrPut(it.id) { it }
    }
    val categories = addon.categories.map {
        Database.categories.getOrPut(it.categoryId) { it }
    }
    val addon = addon.copy(
//        authors = authors
        categories = categories
    )

//    addonQueries.replace(
//        Addons.Impl(
//            id = addon.id,
//            name = addon.name,
//            authorIds = authorIds, // INSERT DONE
//            attachmentIds = attachmentIds, // INSERT DONE
//            websiteUrl = addon.websiteUrl,
//            gameId = addon.gameId,
//            summary = addon.summary,
//            defaultFileId = addon.defaultFileId,
//            downloadCount = addon.downloadCount,
//            latestFileIds = latestFileIds,
//            categoryIds = categoryIds,
//            status = addon.status,
//            categorySectionId = categorySectionId,
//            slug = addon.slug,
//            gameVersionLatestFileIds = gameVersionLatestFileIds,
//            popularityScore = addon.popularityScore,
//            gamePopularityRank = addon.gamePopularityRank,
//            gameName = addon.gameName,
//            portalName = addon.portalName,
//            dateModified = addon.dateModified,
//            dateCreated = addon.dateCreated,
//            dateReleased = addon.dateReleased,
//            isAvailable = addon.isAvailable,
//            primaryLanguage = addon.primaryLanguage,
//            isFeatured = addon.isFeatured
//        )
//    )
    Database.addons[addon.id] = addon
}

fun CurseDatabase.store(author: Author): Int {
//    LOG.info("store $author")
    authorQueries.replace(
        Authors.Impl(
            id = author.id,
            name = author.name,
            url = author.url,
            projectId = author.projectId,
            projectTitleId = author.projectTitleId,
            projectTitleTitle = author.projectTitleTitle,
            userId = author.userId,
            twitchId = author.twitchId
        )
    )
    return author.id
}

fun CurseDatabase.store(attachment: Attachment): Int {
//    LOG.info("store $attachment")
    attachmentQueries.replace(
        Attachments.Impl(
            id = attachment.id,
            projectId = attachment.projectId,
            description = attachment.description,
            isDefault = attachment.isDefault,
            thumbnailUrl = attachment.thumbnailUrl,
            title = attachment.title,
            url = attachment.url,
            status = attachment.status
        )
    )
    return attachment.id
}

fun CurseDatabase.store(addonFile: AddonFile): Int {
//    LOG.info("store $addonFile")
    addonFile.dependencies.forEach { dependency ->
        store(dependency, addonFile.id)
    }
    addonFile.modules.forEach { module ->
        store(module, addonFile.id)
    }
    addonFileQueries.replace(
        AddonFiles.Impl(
            id = addonFile.id,
            fileName = addonFile.fileName,
            fileDate = addonFile.fileDate,
            releaseType = addonFile.releaseType,
            fileStatus = addonFile.fileStatus,
            downloadUrl = addonFile.downloadUrl,
            isAlternate = addonFile.isAlternate,
            alternateFileId = addonFile.alternateFileId,
            isAvailable = addonFile.isAvailable,
            packageFingerprint = addonFile.packageFingerprint,
            gameVersion = addonFile.gameVersion,
            installMetadata = addonFile.installMetadata,
            fileLength = addonFile.fileLength
        )
    )
    return addonFile.id
}

fun CurseDatabase.store(dependency: AddOnFileDependency, fileId: Int) {
//    LOG.info("store $dependency fileId = $fileId")
    addonFileDependencyQueries.replace(
        AddonFileDependencies.Impl(
            _parentId = fileId,
            addonId = dependency.addonId,
            type = dependency.type
        )
    )
}

fun CurseDatabase.store(module: AddOnModule, fileId: Int) {
//    LOG.info("store $module fileId = $fileId")
    addonModuleQueries.replace(
        AddonModules.Impl(
            fileId = fileId,
            fingerprint = module.fingerprint,
            foldername = module.foldername
        )
    )
}

fun CurseDatabase.store(category: Category): Int {
//    LOG.info("store $category")
    categoryQueries.replace(
        Categories.Impl(
            categoryId = category.categoryId,
            categoryName = category.name,
            url = category.url,
            avatarUrl = category.avatarUrl
        )
    )
    return category.categoryId
}

fun CurseDatabase.store(categorySection: CategorySection): Int {
//    LOG.info("store $categorySection")
    categorySectionQueries.replace(
        CategorySections.Impl(
            categorySectionId = categorySection.id,
            categorySectionGameId = categorySection.gameId,
            categorySectionName = categorySection.name,
            packageType = categorySection.packageType,
            path = categorySection.path,
            initialInclusionPattern = categorySection.initialInclusionPattern,
            extraIncludePattern = categorySection.extraIncludePattern
        )
    )
    return categorySection.id
}

fun CurseDatabase.store(gameVersionLatestFile: GameVersionLatestFile, parentId: Int): Int {
//    LOG.info("store $categorySection")
    gameVersionLatestFileQueries.replace(
        GameVersionLatestFiles.Impl(
            _parentId = parentId,
            gameVersion = gameVersionLatestFile.gameVersion,
            projectFileId = gameVersionLatestFile.projectFileId,
            projectFileName = gameVersionLatestFile.projectFileName,
            fileType = gameVersionLatestFile.fileType
        )
    )
    return gameVersionLatestFile.projectFileId
}

suspend fun CurseDatabase.addons(
    gameId: Int? = null,
    name: String? = null,
    slug: String? = null,
    category: String? = null,
    section: String? = null,
    gameVersions: List<String>? = null
): List<Addon> {
//    GlobalScope.launch {
//        val runtime = Runtime.getRuntime()
//        LOG.info(String.format("max memory: %.3f Mb", runtime.maxMemory() / 1024.0 / 1024.0))
//        LOG.info(String.format("total memory: %.3f Mb", runtime.totalMemory() / 1024.0 / 1024.0))
//        LOG.info(String.format("free memory: %.3f Mb", runtime.freeMemory() / 1024.0 / 1024.0))
//    }

    val results = measureMillisAndReport(LOG, "query addons") {
        Database.addons.filter { (id, addon) ->
            (gameId == null || addon.gameId == gameId) &&
                    (name == null || addon.name == name) &&
                    (slug == null || addon.slug == slug) &&
                    (category == null || addon.categories.any { it.name == category }) &&
                    (section == null || addon.categorySection.name == section ) &&
                    (gameVersions == null || addon.gameVersionLatestFiles.any { it.gameVersion in gameVersions })
        }
//        addonQueries.select(
//            gameId == null, gameId ?: 0,
//            category == null, category ?: "",
//            name == null, name ?: ""
//        ).executeAsList()
    }
    return results.values.toList()

//    val remappedResults = measureMillisAndReport(LOG, "remap addons") {
//        results
//            .map {
//                SelectAll.Impl(
//                    id = it.id,
//                    name = it.name,
//                    authorIds = it.authorIds,
//                    attachmentIds = it.attachmentIds,
//                    websiteUrl = it.websiteUrl,
//                    gameId = it.gameId,
//                    summary = it.summary,
//                    defaultFileId = it.defaultFileId,
//                    downloadCount = it.downloadCount,
//                    latestFileIds = it.latestFileIds,
//                    categoryIds = it.categoryIds,
//                    status = it.status,
//                    categorySectionId = it.categorySectionId,
//                    slug = it.slug,
//                    gameVersionLatestFileIds = it.gameVersionLatestFileIds,
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
//                    categorySectionId_ = it.categorySectionId_,
//                    categorySectionGameId = it.categorySectionGameId,
//                    categorySectionName = it.categorySectionName,
//                    packageType = it.packageType,
//                    path = it.path,
//                    initialInclusionPattern = it.initialInclusionPattern,
//                    extraIncludePattern = it.extraIncludePattern
//                )
//            }
//    }


//    return measureMillisAndReport(LOG, "convert to addons") {
//        toAddons(remappedResults)
//    }
}

suspend fun CurseDatabase.allAddons(logging: Boolean = false): List<Addon> {
    val results = addonQueries.selectAll().executeAsList()
    return toAddons(results, logging)
}

suspend fun CurseDatabase.testAddons(limit: Int = 50, logging: Boolean = false): List<Addon> {
    val results = addonQueries.selectAll().executeAsList().take(limit)
    return toAddons(results, logging)
}

suspend fun CurseDatabase.toAddons(results: List<SelectAll>, logging: Boolean = false): List<Addon> = coroutineScope {
    val projectIds = results.map { it.id }
    val latestFileIds = results.flatMap { it.latestFileIds }
    val categoryIds = results.flatMap { it.categoryIds }

    val categorySectionsDeferred = async(Dispatchers.IO) {
        measureMillisAndReport(LOG, "map category sections") {
            results.associate {
                it.categorySectionId to CategorySection(
                    id = it.categorySectionId,
                    gameId = it.categorySectionGameId,
                    name = it.categorySectionName,
                    packageType = it.packageType,
                    path = it.path,
                    initialInclusionPattern = it.initialInclusionPattern,
                    extraIncludePattern = it.extraIncludePattern
                )
            }
        }
    }

    val authorIds = results.flatMap { it.authorIds }
    val groupedAuthorsDeferred = async(Dispatchers.IO) {
        measureMillisAndReport(LOG, "query authors") {
            authorQueries.selectByIds(authorIds).executeAsList()
                .groupBy({ it.projectId }) {
                    Author(
                        id = it.id,
                        name = it.name,
                        url = it.url,
                        projectId = it.projectId,
                        projectTitleId = it.projectTitleId,
                        projectTitleTitle = it.projectTitleTitle,
                        userId = it.userId,
                        twitchId = it.twitchId
                    )
                }
        }
    }


//    if(logging) {
//        LOG.info("groupedAuthors: $groupedAuthors")
//    }

    val attachmentsDeferred = async(Dispatchers.IO) {
        val attachments = measureMillisAndReport(LOG, "query attachments") {
            attachmentQueries.selectByProjectIds(projectIds)
                .executeAsList().groupBy({ it.projectId }) {
                    Attachment(
                        id = it.id,
                        projectId = it.projectId,
                        description = it.description,
                        isDefault = it.isDefault,
                        thumbnailUrl = it.thumbnailUrl,
                        title = it.title,
                        url = it.url,
                        status = it.status
                    )
                }
        }
        if (logging) {
            LOG.info("attachments: ")
            for (attachment in attachments) {
                LOG.info("$attachment")
            }
        }
        attachments
    }

    val dependenciesDeferred = async(Dispatchers.IO) {
        measureMillisAndReport(LOG, "query dependencies") {
            addonFileDependencyQueries.selectByParentFileIds(latestFileIds)
                .executeAsList().groupBy({ it._parentId }) {
                    AddOnFileDependency(
                        addonId = it.addonId,
                        type = it.type
                    )
                }
        }
    }

    val fileResultsDeferred = async {
        measureMillisAndReport(LOG, "query addonFiles") {
            addonFileQueries.selectByIds(latestFileIds).executeAsList()
        }
    }

    val modules = measureMillisAndReport(LOG, "query modules") {
        addonModuleQueries.selectByFileIds(latestFileIds)
            .executeAsList().groupBy({ it.fileId }) {
                AddOnModule(
                    fingerprint = it.fingerprint,
                    foldername = it.foldername
                )
            }
    }

    val fileResults = fileResultsDeferred.await()
    val dependencies = dependenciesDeferred.await()
    val files = fileResults.associate {
        it.id to AddonFile(
            id = it.id,
            fileName = it.fileName,
            fileDate = it.fileDate,
            releaseType = it.releaseType,
            fileStatus = it.fileStatus,
            downloadUrl = it.downloadUrl,
            isAlternate = it.isAlternate,
            alternateFileId = it.alternateFileId,
            dependencies = dependencies[it.id] ?: listOf(),
            isAvailable = it.isAvailable,
            modules = modules[it.id] ?: listOf(),
            packageFingerprint = it.packageFingerprint,
            gameVersion = it.gameVersion,
            installMetadata = it.installMetadata,
            fileLength = it.fileLength
        )
    }

    val allCategoriesDeferred = async(Dispatchers.IO) {
        measureMillisAndReport(LOG, "query categories") {
            categoryQueries.selectByIds(categoryIds)
                .executeAsList().associate {
                    it.categoryId to Category(
                        categoryId = it.categoryId,
                        name = it.categoryName,
                        avatarUrl = it.avatarUrl,
                        url = it.url
                    )
                }
        }
    }

    val gameVersionFileIds = results.flatMap { it.gameVersionLatestFileIds }

    val gameVersionLatestFilesDeferred = async(Dispatchers.IO) {
        measureMillisAndReport(LOG, "query gameVersionLatest") {
            gameVersionLatestFileQueries.selectByProjectFileIds(gameVersionFileIds)
                .executeAsList().groupBy({ it._parentId }) {
                    GameVersionLatestFile(
                        gameVersion = it.gameVersion,
                        projectFileId = it.projectFileId,
                        projectFileName = it.projectFileName,
                        fileType = it.fileType
                    )
                }
        }
    }

    val categorySections = categorySectionsDeferred.await()
    val groupedAuthors = groupedAuthorsDeferred.await()
    val attachments = attachmentsDeferred.await()
    val allCategories = allCategoriesDeferred.await()
    val gameVersionLatestFiles = gameVersionLatestFilesDeferred.await()
    return@coroutineScope measureMillisAndReport(LOG, "map results into addons") {
        results.map {
            //        val authors = it.authorIds.map { authorId -> allAuthors.getValue(authorId) }
            val latestFiles = it.latestFileIds.map { fileId -> files.getValue(fileId) }
            val categories = it.categoryIds.map { categoryId -> allCategories.getValue(categoryId) }
            Addon(
                id = it.id,
                name = it.name,
                authors = groupedAuthors[it.id] ?: listOf(),
                attachments = attachments[it.id] ?: listOf(),
                websiteUrl = it.websiteUrl,
                gameId = it.gameId,
                summary = it.summary,
                defaultFileId = it.defaultFileId,
                downloadCount = it.downloadCount,
                latestFiles = latestFiles,
                categories = categories,
                status = it.status,
                categorySection = categorySections[it.categorySectionId]
                    ?: error("no categorySection with id '${it.categorySectionId}' for project ${it.id}"),
                slug = it.slug,
                gameVersionLatestFiles = gameVersionLatestFiles[it.id] ?: listOf(), // TODO
                popularityScore = it.popularityScore,
                gamePopularityRank = it.gamePopularityRank,
                gameName = it.gameName,
                portalName = it.portalName,
                dateModified = it.dateModified,
                dateCreated = it.dateCreated,
                dateReleased = it.dateReleased,
                isAvailable = it.isAvailable,
                primaryLanguage = it.primaryLanguage,
                isFeatured = it.isFeatured
            )
        }
    }
}

