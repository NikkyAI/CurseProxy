package moe.nikky.curseproxy.dao

import org.jetbrains.squash.definition.*

object Addons : TableDefinition() {
    val id = integer("id").autoIncrement().primaryKey()
    val addonId = integer("addonId")
    val name = varchar("name", 60)
}
