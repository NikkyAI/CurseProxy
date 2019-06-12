package moe.nikky.curseproxy.data

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import moe.nikky.curseproxy.model.Author
import moe.nikky.curseproxy.model.CategorySection
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun setupCurseDatabase(dbPath: String = "curse.db") : CurseDatabase {
    val dbFile = File(dbPath)
    val dbFileExists = dbFile.exists()
    val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:$dbPath")
    if(!dbFileExists) {
        CurseDatabase.Schema.create(driver)
    }

    val localDateTimeAdapter = object: ColumnAdapter<LocalDateTime, String> {
        val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss")
        override fun decode(databaseValue: String): LocalDateTime = LocalDateTime.from(formatter.parse(databaseValue))
        override fun encode(value: LocalDateTime): String = formatter.format(value)
    }
    lateinit var database: CurseDatabase
    database = CurseDatabase(
        driver = driver,
        categorySectionEntryAdapter = CategorySectionEntry.Adapter (
            packageTypeAdapter = EnumColumnAdapter()
        ),
        addonEntryAdapter = AddonEntry.Adapter (
            authorsAdapter = object: ColumnAdapter<List<Author>, String> {
                override fun decode(databaseValue: String): List<Author> {
                    val authors = databaseValue.split(",").map {
                        val id = it.toInt()
                        val result = database.authorQueries.selectById(id).executeAsOne()
                        Author (
                            name = result.name,
                            url = result.url,
                            projectId = result.projectId,
                            id = result.id,
                            projectTitleId = result.projectTitleId,
                            projectTitleTitle = result.projectTitleTitle,
                            userId = result.userId,
                            twitchId = result.twitchId
                        )
                    }
                    return authors
                }

                override fun encode(value: List<Author>): String = value.map {
                    database.authorQueries.replace(
                        name = it.name,
                        url = it.url,
                        projectId = it.projectId,
                        id = it.id,
                        projectTitleId = it.projectTitleId,
                        projectTitleTitle = it.projectTitleTitle,
                        userId = it.userId,
                        twitchId = it.twitchId
                    )
                    it.id
                }.joinToString(",")
            },
            statusAdapter = EnumColumnAdapter(),
            categorySectionAdapter = object: ColumnAdapter<CategorySection, Long> {
                override fun decode(databaseValue: Long): CategorySection {
                    val query = database.categorySectionQueries.selectById(databaseValue.toInt())

                    val result = query.executeAsOne()

                    return CategorySection(
                        id = result.id,
                        name = result.name,
                        gameId = result.gameId,
                        packageType = result.packageType,
                        path = result.path,
                        initialInclusionPattern = result.initialInclusionPattern,
                        extraIncludePattern = result.extraIncludePattern
                    )
                }

                override fun encode(value: CategorySection): Long {
                    database.categorySectionQueries.replace(
                        id = value.id,
                        name = value.name,
                        gameId = value.gameId,
                        packageType = value.packageType,
                        path = value.path,
                        initialInclusionPattern = value.initialInclusionPattern,
                        extraIncludePattern = value.extraIncludePattern
                    )
                    return value.id.toLong()
                }
            },
            dateCreatedAdapter = localDateTimeAdapter,
            dateModifiedAdapter = localDateTimeAdapter,
            dateReleasedAdapter = localDateTimeAdapter
        )
    )
    return database
}