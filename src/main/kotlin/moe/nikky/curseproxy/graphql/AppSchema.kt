package moe.nikky.curseproxy.graphql

import com.github.pgutkowski.kgraphql.KGraphQL
import moe.nikky.curseproxy.LOG
import moe.nikky.curseproxy.dao.AddonStorage
import moe.nikky.curseproxy.model.Section
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
            resolver { size: Long?, name: String?, slug: String?, author: String?, category: String?, section: Section? ->
                storage.getAll(size, name, slug, author, category, section)
            }.withArgs {
                arg<Long> { name = "size"; defaultValue = null; description = "The number of records to return" }
                arg<String> { name = "name"; defaultValue = null; description = "The name of the addon to return" }
                arg<String> { name = "slug"; defaultValue = null; description = "The slug of the addon toreturn" }
                arg<String> { name = "author"; defaultValue = null; description = "author name" }
                arg<String> { name = "category"; defaultValue = null; description = "category string" }
                arg<Section> { name = "section"; defaultValue = null; description = "section name" }
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
            description = "A CurseAddon"

            property(Addon::id) {
                description = "project id"
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

        enum<Section>()

    }

}