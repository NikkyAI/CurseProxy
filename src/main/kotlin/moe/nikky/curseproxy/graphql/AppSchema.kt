package moe.nikky.curseproxy.graphql

import com.github.pgutkowski.kgraphql.KGraphQL
import moe.nikky.curseproxy.LOG
import moe.nikky.curseproxy.curse.CurseClient
import moe.nikky.curseproxy.dao.AddonStorage
import moe.nikky.curseproxy.model.AddOnModule
import moe.nikky.curseproxy.model.AddonFile
import moe.nikky.curseproxy.model.Attachment
import moe.nikky.curseproxy.model.Author
import moe.nikky.curseproxy.model.Category
import moe.nikky.curseproxy.model.CategorySection
import moe.nikky.curseproxy.model.CurseAddon
import moe.nikky.curseproxy.model.DependencyType
import moe.nikky.curseproxy.model.FileStatus
import moe.nikky.curseproxy.model.FileType
import moe.nikky.curseproxy.model.GameVersionLatestFile
import moe.nikky.curseproxy.model.PackageType
import moe.nikky.curseproxy.model.ProjectStatus
import moe.nikky.curseproxy.model.graphql.Addon
import java.time.LocalDate

class AppSchema(private val storage: AddonStorage) {

    val schema = KGraphQL.schema {

        configure {
            useDefaultPrettyPrinter = true
        }

        stringScalar<LocalDate> {
            serialize = { date -> date.toString() }
            deserialize = { dateString -> LocalDate.parse(dateString) }
        }

        query("addons") {
            resolver { gameID: Int?, name: String?, slug: String?, category: String?, section: String?, gameVersions: List<String>? ->
                storage.getAll(gameID, name, slug, category, section, gameVersions)
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

        type<Dummy> {
            description = "A Dummy Type"

            property(Dummy::placeholder) {
                description = "placeholder field"
            }
        }
        type<Addon> {
            description = "A Sparse CurseAddon"
            property(Addon::gameID) {
                description = "id of the game this addon is for"
            }
            property(Addon::name) {
                description = "addon name"
            }
            property(Addon::slug) {
                description = "addon url slug"
            }
            property(Addon::primaryAuthorName) {
                description = "primary project author"
            }
            property(Addon::primaryCategoryName) {
                description = "primary project category"
            }
            property(Addon::categoryList) {
                description = "list of project categories"
            }
        }

        type<CurseAddon> {
            description = "A CurseAddon"
//            property(CurseAddon::categories) {
//
//            }
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