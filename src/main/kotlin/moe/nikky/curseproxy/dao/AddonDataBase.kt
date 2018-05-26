package moe.nikky.curseproxy.dao

import moe.nikky.curseproxy.LOG
import moe.nikky.curseproxy.model.Section
import moe.nikky.curseproxy.model.SparseAddon
import org.jetbrains.squash.connection.DatabaseConnection
import org.jetbrains.squash.connection.transaction
import org.jetbrains.squash.dialects.h2.H2Connection
import org.jetbrains.squash.expressions.eq
import org.jetbrains.squash.expressions.like
import org.jetbrains.squash.query.*
import org.jetbrains.squash.results.ResultRow
import org.jetbrains.squash.results.get
import org.jetbrains.squash.schema.create
import org.jetbrains.squash.statements.fetch
import org.jetbrains.squash.statements.insertInto
import org.jetbrains.squash.statements.values
import sun.misc.MessageUtils.where

fun ResultRow.toSparseAddon() = SparseAddon(
        id = this[SparseAddons.id],
        addonId = this[SparseAddons.addonId],
        name = this[SparseAddons.name],
        primaryAuthorName = this[SparseAddons.primaryAuthorName],
        primaryCategoryName = this[SparseAddons.primaryCategoryName],
        sectionName = Section.valueOf(this[SparseAddons.sectionName]),
        dateModified = this[SparseAddons.dateModified],
        dateCreated = this[SparseAddons.dateCreated],
        dateReleased = this[SparseAddons.dateReleased]
)

//TODO: add more database row conversions


class AddonDatabase(val db: DatabaseConnection = H2Connection.createMemoryConnection()) : AddonStorage {
    init {
        db.transaction {
            databaseSchema().create(SparseAddons)
        }
    }

    override fun getSparseAddon(id: Int) = db.transaction {
        val row = from(SparseAddons).where { SparseAddons.id eq id }.execute().singleOrNull()
        row?.toSparseAddon()
    }

    override fun getAll(size: Long?, name: String?, author: String?, category: String?, section: Section?) = db.transaction {
        from(SparseAddons)
                .select()
                .apply {
                    name?.let {
                        LOG.debug("added name filter '$it'")
                        where { SparseAddons.name like it}
                    }
                    author?.let {
                        LOG.debug("added author filter '$it'")
                        where { SparseAddons.primaryAuthorName like it}
                    }
                    category?.let {
                        LOG.debug("added category filter '$it'")
                        where { SparseAddons.primaryCategoryName like it}
                    }
                    section?.let {
                        LOG.debug("added section filter '$it'")
                        where { SparseAddons.sectionName eq it.toString()}
                    }
                    size?.let {
                        LOG.debug("added size limit '$it'")
                        limit(size)
                    }
                }
//                .orderBy(Addons.date, ascending = false)
                .execute()
                .map { it.toSparseAddon() }
                .toList()
    }

    override fun createSparseAddon(addon: SparseAddon) = db.transaction {
        insertInto(SparseAddons).values {
            if(addon.id != null)
                it[id] = addon.id
            it[addonId] = addon.addonId
            it[name] = addon.name
            it[primaryAuthorName] = addon.primaryAuthorName
            it[primaryCategoryName] = addon.primaryCategoryName
            it[sectionName] = addon.sectionName.toString()
            it[dateModified] = addon.dateModified
            it[dateCreated] = addon.dateCreated
            it[dateReleased] = addon.dateReleased
        }.fetch(SparseAddons.id).execute()
    }

    override fun close() { }

}