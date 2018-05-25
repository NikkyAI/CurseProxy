package moe.nikky.curseproxy.dao

import org.jetbrains.squash.definition.*

object SparseAddons : TableDefinition() {
    val id = integer("id").autoIncrement().primaryKey()
    val addonId = integer("addonId")
    val name = varchar("name", 60)
}
