package moe.nikky.curseproxy.graphql

import com.github.pgutkowski.kgraphql.KGraphQL
import moe.nikky.curseproxy.dao.AddonStorage
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
            resolver { size: Long, name: String?, author: String?, category: String?, section: String? ->
                storage.getAll(size, name, author, category, section) }.withArgs {
                arg<Long> { name = "size"; defaultValue = 1000; description = "The number of records to return" }
                arg<String> { name = "name"; defaultValue = null; description = "The name of the addon to return" }
                arg<String> { name = "author"; defaultValue = null; description = "author name" }
                arg<String> { name = "category"; defaultValue = null; description = "category string" }
                arg<String> { name = "section"; defaultValue = null; description = "section string" }
            }
        }

//        mutation("addNothing") {
//            description = "does nothing"
//
//            resolver { input: String -> { println(input)}/**/ }
//        }

        type<SparseAddon> {
            description = "A Addon"

            property(SparseAddon::addonId) {
                description = "addon name"
            }
        }
    }

}