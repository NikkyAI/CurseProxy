package moe.nikky.curseproxy.graphql

import com.apurebase.kgraphql.KGraphQL
import kotlinx.coroutines.runBlocking
import moe.nikky.curseproxy.LOG
import moe.nikky.curseproxy.curse.CurseClient
import moe.nikky.curseproxy.data.CurseDatabase
import moe.nikky.curseproxy.data.addons
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

    val schema = KGraphQL.schema {

        configure {
            useDefaultPrettyPrinter = true
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

        query("addons") {
            resolver { gameID: Int?, name: String?, slug: String?, category: String?, section: String?, gameVersions: List<String>? ->
                measureMillisAndReport(LOG, "call db") {
                    runBlocking {
                        database.addons(gameID, name, slug, category, section, gameVersions)
                    }
                }
            }.withArgs {
                arg<Int> { name = "gameID"; defaultValue = null; description = "The game id to filter for" }
                arg<String> { name = "name"; defaultValue = null; description = "The name of the addon to return" }
                arg<String> { name = "slug"; defaultValue = null; description = "The slug of the addon to return" }
                arg<String> { name = "category"; defaultValue = null; description = "category string" }
                arg<String> { name = "section"; defaultValue = null; description = "section name" }
                arg<List<String>> { name = "gameVersions"; defaultValue = null; description = "game versions" }
            }
        }

        query("addonSearch") {
            description = "search for addons, passes the request through to the curse api"
            suspendResolver { searchFilter: String?, gameID: Int?, gameVersions: List<String>?, categoryIds: List<Int>?, section: Int? ->
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

//        type<Dummy> {
//            description = "A Dummy Type"
//
//            property(Dummy::placeholder) {
//                description = "placeholder field"
//            }
//        }
//        type<SimpleAddon> {
//            description = "A Sparse CurseAddon"
//            property(SimpleAddon::gameID) {
//                description = "id of the game this addon is for"
//            }
//            property(SimpleAddon::name) {
//                description = "addon name"
//            }
//            property(SimpleAddon::slug) {
//                description = "addon url slug"
//            }
//            property(SimpleAddon::categoryList) {
//                description = "list of project categories"
//            }
//        }

        type<Addon> {
            description = "A curse Addon"
//            property(Addon::id) {
//                ignore = true
//            }
//            this.unionProperty("id") {
//                resolver {
//                    it.id.value
//                }
//            }
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

        type<AddonFile> {
            description = "Curse File"
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
    }
}