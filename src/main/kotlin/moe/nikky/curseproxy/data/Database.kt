package moe.nikky.curseproxy.data

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import moe.nikky.curseproxy.LOG
import moe.nikky.curseproxy.model.AddOnFileDependency
import moe.nikky.curseproxy.model.AddOnModule
import moe.nikky.curseproxy.model.Addon
import moe.nikky.curseproxy.model.AddonFile
import moe.nikky.curseproxy.model.Attachment
import moe.nikky.curseproxy.model.Author
import moe.nikky.curseproxy.model.Category
import moe.nikky.curseproxy.model.CategorySection
import voodoo.data.curse.FileID
import voodoo.data.curse.ProjectID
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
    val fileIdListAdapter = object : ColumnAdapter<List<FileID>, String> {
        override fun decode(databaseValue: String): List<FileID> =
            databaseValue.split(",")
                .mapNotNull { it.toIntOrNull() }
                .map { FileID(it) }

        override fun encode(value: List<FileID>): String = value.joinToString(",")
    }
    val localDateTimeAdapter = object : ColumnAdapter<LocalDateTime, String> {
        val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss")
        override fun decode(databaseValue: String): LocalDateTime = LocalDateTime.from(formatter.parse(databaseValue))
        override fun encode(value: LocalDateTime): String = formatter.format(value)
    }
    val projectIdAdapter = object: ColumnAdapter<ProjectID, Long> {
        override fun decode(databaseValue: Long): ProjectID = ProjectID(databaseValue.toInt())
        override fun encode(value: ProjectID): Long = value.value.toLong()
    }
    val fileIdAdapter = object: ColumnAdapter<FileID, Long> {
        override fun decode(databaseValue: Long): FileID = FileID(databaseValue.toInt())
        override fun encode(value: FileID): Long = value.value.toLong()
    }
    lateinit var database: CurseDatabase
    database = CurseDatabase(
        driver = driver,
        addonsAdapter = Addons.Adapter(
            idAdapter = projectIdAdapter,
            authorIdsAdapter = intListAdapter,
            attachmentIdsAdapter = intListAdapter,
            latestFileIdsAdapter = fileIdListAdapter,
            categoryIdsAdapter = intListAdapter,
            statusAdapter = EnumColumnAdapter(),
            dateCreatedAdapter = localDateTimeAdapter,
            dateModifiedAdapter = localDateTimeAdapter,
            dateReleasedAdapter = localDateTimeAdapter
        ),
        addonFilesAdapter = AddonFiles.Adapter(
            idAdapter = fileIdAdapter,
            fileDateAdapter = localDateTimeAdapter,
            releaseTypeAdapter = EnumColumnAdapter(),
            fileStatusAdapter = EnumColumnAdapter(),
            gameVersionAdapter = stringListAdapter
        ),
        addonFileDependenciesAdapter = AddonFileDependencies.Adapter(
            _parentIdAdapter = fileIdAdapter,
            addonIdAdapter = projectIdAdapter,
            typeAdapter = EnumColumnAdapter()
        ),
        addonModulesAdapter = AddonModules.Adapter(
            fileIdAdapter = fileIdAdapter
        ),
        attachmentsAdapter = Attachments.Adapter(
            projectIdAdapter = projectIdAdapter
        ),
        authorsAdapter = Authors.Adapter(
            projectIdAdapter = projectIdAdapter
        ),
        categorySectionsAdapter = CategorySections.Adapter(
            packageTypeAdapter = EnumColumnAdapter()
        )

    )
    return database
}

fun CurseDatabase.store(addon: Addon) {
    val projectId = addon.id.value
    val authorIds = addon.authors.map { author ->
        store(author)
    }
    val attachmentIds = addon.attachments.map { attachment ->
        store(attachment)
    }
    val latestFileIds = addon.latestFiles.map { file ->
        store(file)
    }
    val categoryIds = addon.categories.map { category ->
        store(category)
    }
    // TODO: latestFIles
    // TODO: categories
    val categorySectionId = store(addon.categorySection)
    LOG.info("store ${addon.id}")
    addonQueries.replace(
        Addons.Impl(
            id = addon.id,
            name = addon.name,
            authorIds = authorIds, // INSERT DONE
            attachmentIds = attachmentIds, // INSERT DONE
            websiteUrl = addon.websiteUrl,
            gameId = addon.gameId,
            summary = addon.summary,
            defaultFileId = addon.defaultFileId,
            downloadCount = addon.downloadCount,
            latestFileIds = latestFileIds,
            categoryIds = categoryIds,
            status = addon.status,
            categorySectionId = categorySectionId,
            slug = addon.slug,
//            gameVersionLatestFiles = addon.gameVersionLatestFiles,
            popularityScore = addon.popularityScore,
            gamePopularityRank = addon.gamePopularityRank,
            gameName = addon.gameName,
            portalName = addon.portalName,
            dateModified = addon.dateModified,
            dateCreated = addon.dateCreated,
            dateReleased = addon.dateReleased,
            isAvailable = addon.isAvailable,
            primaryLanguage = addon.primaryLanguage,
            isFeatured = addon.isFeatured
        )
    )
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

fun CurseDatabase.store(addonFile: AddonFile): FileID {
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

fun CurseDatabase.store(dependency: AddOnFileDependency, fileId: FileID) {
//    LOG.info("store $dependency fileId = $fileId")
    addonFileDependencyQueries.replace(
        AddonFileDependencies.Impl(
            _parentId = fileId,
            addonId = dependency.addonId,
            type = dependency.type
        )
    )
}
fun CurseDatabase.store(module: AddOnModule, fileId: FileID) {
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
            categoryName = category.categoryName,
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
            categorySectionId = categorySection.categorySectionId,
            categorySectionGameId = categorySection.categorySectionGameId,
            categorySectionName = categorySection.categorySectionName,
            packageType = categorySection.packageType,
            path = categorySection.path,
            initialInclusionPattern = categorySection.initialInclusionPattern,
            extraIncludePattern = categorySection.extraIncludePattern
        )
    )
    return categorySection.categorySectionId
}

fun CurseDatabase.addons(mcVersions: List<String>, gameIds: List<Int>): List<Addon> {
    val results = addonQueries.selectAll().executeAsList()
        .filter {
            it.gameId in gameIds
        }
    return toAddons(results)
}

fun CurseDatabase.allAddons(logging: Boolean = false): List<Addon> {
    val results = addonQueries.selectAll().executeAsList()
    return toAddons(results, logging)
}

fun CurseDatabase.testAddons(limit: Int = 50, logging: Boolean = false): List<Addon> {
    val results = addonQueries.selectAll().executeAsList().take(limit)
    return toAddons(results, logging)
}

fun CurseDatabase.toAddons(results: List<SelectAll>, logging: Boolean = true): List<Addon> {
    val projectIds = results.map { it.id }
    val categorySections = results.associate {
        it.categorySectionId to CategorySection(
            categorySectionId = it.categorySectionId,
            categorySectionGameId = it.categorySectionGameId,
            categorySectionName = it.categorySectionName,
            packageType = it.packageType,
            path = it.path,
            initialInclusionPattern = it.initialInclusionPattern,
            extraIncludePattern = it.extraIncludePattern
        )
    }

    val authorIds = results.flatMap { it.authorIds }
    val groupedAuthors = authorQueries.selectByIds(authorIds)
        .executeAsList()
//        .also {
//            if(logging) {
//                LOG.info("authors: $it")
//            }
//        }
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

    if(logging) {
        LOG.info("groupedAuthors: $groupedAuthors")
    }

    val attachments = attachmentQueries.selectByProjectIds(projectIds)
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
    if(logging) {
        LOG.info("attachments: $attachments")
    }

    val fileIds = results.flatMap { it.latestFileIds }
    val fileResults = addonFileQueries.selectByIds(fileIds).executeAsList()
    val dependencies = addonFileDependencyQueries.selectByParentFileIds(fileIds)
        .executeAsList().groupBy({it._parentId}) {
            AddOnFileDependency(
                addonId = it.addonId,
                type = it.type
            )
        }

    val modules = addonModuleQueries.selectByFileIds(fileIds)
        .executeAsList().groupBy({ it.fileId }) {
            AddOnModule(
                fingerprint = it.fingerprint,
                foldername = it.foldername
            )
        }
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
            dependencies = dependencies[it.id] ?: listOf(), // TODO
            isAvailable = it.isAvailable,
            modules = modules[it.id] ?: listOf(),
            packageFingerprint = it.packageFingerprint,
            gameVersion = it.gameVersion,
            installMetadata = it.installMetadata,
            fileLength = it.fileLength
        )
    }

    val categoryIds = results.flatMap { it.categoryIds }
    val allCategories = categoryQueries.selectByIds(categoryIds)
        .executeAsList().associate {
            it.categoryId to Category(
                categoryId = it.categoryId,
                categoryName = it.categoryName,
                avatarUrl = it.avatarUrl,
                url = it.url
            )
        }

    return results.map {
//        val authors = it.authorIds.map { authorId -> allAuthors.getValue(authorId) }
        val latestFiles = it.latestFileIds.map { fileId -> files.getValue(fileId) }
        val categories = it.categoryIds.map { categoryId -> allCategories.getValue(categoryId) }
        Addon(
            id = it.id,
            name = it.name,
            authors = groupedAuthors.getValue(it.id),
            attachments = attachments.getValue(it.id),
            websiteUrl = it.websiteUrl,
            gameId = it.gameId,
            summary = it.summary,
            defaultFileId = it.defaultFileId,
            downloadCount = it.downloadCount,
            latestFiles = latestFiles,
            categories = categories,
            status = it.status,
            categorySection = categorySections.getValue(it.categorySectionId),
            slug = it.slug,
            gameVersionLatestFiles = listOf(), // TODO
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

