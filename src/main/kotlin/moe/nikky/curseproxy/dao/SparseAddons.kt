package moe.nikky.curseproxy.dao

import org.jetbrains.squash.definition.*

object SparseAddons : TableDefinition() {
    val id = integer("id").autoIncrement().primaryKey()
    val addonId = integer("addonId")
    val name = varchar("name", 300)
    val primaryAuthorName = varchar("primaryAuthorName", 60)
    val primaryCategoryName = varchar("primaryCategoryName", 60)
    val sectionName = varchar("sectionName", 6100)
    val dateModified = date("dateModified")
    val dateCreated = date("dateCreated")
    val dateReleased = date("dateReleased")
}
