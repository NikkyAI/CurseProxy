package moe.nikky.curseproxy.data

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import moe.nikky.curseproxy.LOG
import moe.nikky.curseproxy.model.Addon
import moe.nikky.curseproxy.model.Attachment
import moe.nikky.curseproxy.model.Author
import moe.nikky.curseproxy.model.CategorySection
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

    val localDateTimeAdapter = object : ColumnAdapter<LocalDateTime, String> {
        val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss")
        override fun decode(databaseValue: String): LocalDateTime = LocalDateTime.from(formatter.parse(databaseValue))
        override fun encode(value: LocalDateTime): String = formatter.format(value)
    }
    lateinit var database: CurseDatabase
    database = CurseDatabase(
        driver = driver,
        addonsAdapter = Addons.Adapter(
            statusAdapter = EnumColumnAdapter(),
            dateCreatedAdapter = localDateTimeAdapter,
            dateModifiedAdapter = localDateTimeAdapter,
            dateReleasedAdapter = localDateTimeAdapter
        ),
        categorySectionsAdapter = CategorySections.Adapter(
            packageTypeAdapter = EnumColumnAdapter()
        ),
        addonFilesAdapter = AddonFiles.Adapter(
            fileDateAdapter = localDateTimeAdapter,
            releaseTypeAdapter = EnumColumnAdapter(),
            fileStatusAdapter = EnumColumnAdapter()
        )
    )
    return database
}

fun CurseDatabase.store(addon: Addon) {
    val projectId = addon.id.value
    addon.authors.forEach { author ->
        store(author)
    }
    addon.attachments?.forEach { attachment ->
        store(attachment)
    }
    val categorySectionId = store(addon.categorySection)
    LOG.info("store ${addon.id}")
    addonQueries.replace(
        Addons.Impl(
            id = addon.id.value,
            name = addon.name,
//            authors = addon.authors, // INSERT DONE
//            attachments = addon.attachments // INSERT DONE
            websiteUrl = addon.websiteUrl,
            gameId = addon.gameId,
            summary = addon.summary,
            defaultFileId = addon.defaultFileId,
            downloadCount = addon.downloadCount,
//            latestFiles = addons.latestFiles
//            categories = addons.categories
            status = addon.status,
            categorySection = categorySectionId,
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

fun CurseDatabase.store(author: Author) {
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
}

fun CurseDatabase.store(attachment: Attachment) {
//    LOG.info("store $attachment")
    attachmentQueries.replace(
        Attachments.Impl(
            id = attachment.id,
            projectId = attachment.projectId,
            description = attachment.description,
            thumbnailUrl = attachment.thumbnailUrl,
            title = attachment.title,
            url = attachment.url,
            status = attachment.status
        )
    )
}

fun CurseDatabase.store(categorySection: CategorySection): Int {
//    LOG.info("store $categorySection")
    categorySectionQueries.replace(
        CategorySections.Impl(
            id = categorySection.id,
            gameId = categorySection.gameId,
            name = categorySection.name,
            packageType = categorySection.packageType,
            path = categorySection.path,
            initialInclusionPattern = categorySection.initialInclusionPattern,
            extraIncludePattern = categorySection.extraIncludePattern
        )
    )
    return categorySection.id
}
