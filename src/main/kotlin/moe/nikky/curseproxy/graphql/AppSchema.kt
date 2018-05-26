package moe.nikky.curseproxy.graphql

import com.github.pgutkowski.kgraphql.KGraphQL
import moe.nikky.curseproxy.LOG
import moe.nikky.curseproxy.dao.AddonStorage
import moe.nikky.curseproxy.model.Section
import moe.nikky.curseproxy.model.SparseAddon
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
            resolver { size: Long?, name: String?, author: String?, category: String?, section: Section? ->
                storage.getAll(size, name, author, category, section)
            }.withArgs {
                arg<Long> { name = "size"; defaultValue = null; description = "The number of records to return" }
                arg<String> { name = "name"; defaultValue = null; description = "The name of the addon to return" }
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
        type<SparseAddon> {
            description = "A Addon"

            property(SparseAddon::addonId) {
                description = "project id"
            }
            property(SparseAddon::name) {
                description = "addon name"
            }
            property(SparseAddon::primaryAuthorName) {
                description = "primary project author"
            }
            property(SparseAddon::primaryCategoryName) {
                description = "primary project category"
            }
        }

        enum<Section>()

    }

}