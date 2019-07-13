package moe.nikky.curseproxy.graphql

import com.apurebase.kgraphql.KGraphQL
import moe.nikky.curseproxy.LOG
import moe.nikky.curseproxy.curse.CurseClient
import moe.nikky.curseproxy.data.CurseDatabase
import moe.nikky.curseproxy.data.filterAddons
import moe.nikky.curseproxy.model.AddOnModule
import moe.nikky.curseproxy.model.Addon
import moe.nikky.curseproxy.model.AddonFile
import moe.nikky.curseproxy.model.Attachment
import moe.nikky.curseproxy.model.Author
import moe.nikky.curseproxy.model.Category
import moe.nikky.curseproxy.model.CategorySection
import moe.nikky.curseproxy.model.DependencyType
import moe.nikky.curseproxy.model.FileStatus
import moe.nikky.curseproxy.model.FileType
import moe.nikky.curseproxy.model.GameVersionLatestFile
import moe.nikky.curseproxy.model.PackageType
import moe.nikky.curseproxy.model.ProjectStatus
import moe.nikky.curseproxy.util.measureMillisAndReport
import voodoo.data.curse.ProjectID
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AppSchema(private val database: CurseDatabase) {

    inline fun <T: Any, R: Any> List<R>.filter(
        value: T?,
        valueList: List<T>?,
        function: (R, List<T>) -> Boolean
    ): List<R> {
        val list = (valueList.orEmpty() + listOfNotNull(value))
        if(list.isEmpty()) return this
        return this.filter { addon ->
            function(addon, list)
        }
    }

    suspend fun addonsResolver(
        gameId: Int?, gameIdList: List<Int>?,
        category: String?, categoryList: List<String>? =null,
        gameVersion: String?, gameVersionList: List<String>?,
        id: Int?, idList: List<Int>?,
        name: String?, nameList: List<String>?,
        slug: String?, slugList: List<String>?,
        section: String?, sectionList: List<String>?,
        status: ProjectStatus?, statusList: List<ProjectStatus>?
    ) = measureMillisAndReport(LOG, "call db") {
        var addons = database.filterAddons()
        addons = addons.filter(gameId, gameIdList) { addon, list ->
            addon.gameId in list
        }
        addons = addons.filter(id, idList) { addon, list ->
            addon.id in list
        }
        addons = addons.filter(category, categoryList) { addon, list ->
            addon.categories.any { it.name in list }
        }
        addons = addons.filter(gameVersion, gameVersionList) { addon, list ->
            addon.gameVersionLatestFiles.any { it.gameVersion in list }
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

    suspend fun filesResolve(
        id: Int,
        fileStatus: FileStatus?, fileStatusList: List<FileStatus>?,
        gameVersion: String?, gameVersionList: List<String>?,
        releaseType: FileType?, releaseTypeList: List<FileType>?
    ): List<AddonFile> {
        var files = CurseClient.getAddonFiles(id)!!
        files = files.filter(fileStatus, fileStatusList) { file, list ->
            file.fileStatus in list
        }
        files = files.filter(gameVersion, gameVersionList) { file, list ->
            file.gameVersion.any { it in list }
        }
        files = files.filter(releaseType, releaseTypeList) { file, list ->
            file.releaseType in list
        }
        return files
    }

    val schema = KGraphQL.schema {

        configure {
            useDefaultPrettyPrinter = true
            acceptSingleValueAsArray = true
        }

        query("addons") {
            ::addonsResolver.toResolver()
        }

        query("files") {
            ::filesResolve.toResolver()
        }

        query("addonSearch") {
            description = "search for addons, passes the request through to the curse api"
            resolver { searchFilter: String?, gameID: Int?, gameVersions: List<String>?, categoryIds: List<Int>?, section: Int? ->
                CurseClient.getAllAddonsByCriteria(
                    gameId = gameID ?: 432,
                    sectionId = section,
                    gameVersions = gameVersions,
                    searchFilter = searchFilter,
                    categoryIds = categoryIds,
                    sort = CurseClient.AddonSortMethod.LastUpdated
                )
            }.withArgs {
                arg<String> { name = "searchFilter"; defaultValue = null; description = "search filter" }
                arg<Int> { name = "gameID"; defaultValue = null; description = "Game id" }
                arg<List<String>> { name = "gameVersions"; defaultValue = null; description = "Game Versions" }
                arg<List<Int>> { name = "categoryIds"; defaultValue = null; description = "category ids" }
                arg<Int> { name = "section"; defaultValue = null; description = "section" }
            }
        }

        mutation("doNothing") {
            description = "Does nothing"
            resolver { a: String ->
                LOG.info("called mutation with $a")
                Dummy()
            }
        }

        type<Addon> {
            description = "A curse Addon"

            property<List<Attachment>>("attachments") {
                resolver { addon: Addon, isDefault: Boolean? ->
                    addon.attachments.filter { isDefault == null || isDefault == it.isDefault }
                }.withArgs {
                    arg<Boolean> {
                        name = "isDefault"; defaultValue = null; description =
                        "only list the default attachment or exclude the default attachment"
                    }
                }
            }

            property<List<AddonFile>>("files") {
                resolver { addon ->
                    CurseClient.getAddonFiles(addon.id)!!
                }
            }
        }

        type<AddonFile> {
            description = "Curse File"
        }

        type<ProjectID> {
            description = "a Project ID"
        }

        type<GameVersionLatestFile> {
            description = "Curse Latest File"
        }

        type<Author> {
            description = "Curse Mod Author"
        }

        type<Attachment> {
            description = "Curse Project/File attachment"
        }

        type<Category> {
            description = "Curse Project category"
        }

        type<CategorySection> {
            description = "Curse Project category section"
        }

        enum<ProjectStatus> {
            description = "Curse Project status"
        }

        enum<PackageType> {
            description = "Curse package Type"
        }

        enum<DependencyType> {
            description = "Curse dependency type"
        }

        enum<FileType> {
            description = "Curse File type"
        }

        enum<FileStatus> {
            description = "Curse File status"
        }

        type<AddOnModule> {
            description = "Curse addon module"
        }

        stringScalar<LocalDate> {
            serialize = { date -> date.toString() }
            deserialize = { dateString -> LocalDate.parse(dateString) }
        }
        stringScalar<LocalDateTime> {
            val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss")
            serialize = { date -> formatter.format(date) }
            deserialize = { dateString -> LocalDateTime.from(formatter.parse(dateString)) }
        }
    }
}