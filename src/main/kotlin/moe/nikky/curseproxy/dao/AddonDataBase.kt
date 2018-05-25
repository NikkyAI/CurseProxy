package moe.nikky.curseproxy.dao

import moe.nikky.curseproxy.dao.SparseAddons.addonId
import moe.nikky.curseproxy.dao.SparseAddons.name
import moe.nikky.curseproxy.model.Addon
import moe.nikky.curseproxy.model.SparseAddon
import org.jetbrains.squash.connection.DatabaseConnection
import org.jetbrains.squash.connection.transaction
import org.jetbrains.squash.dialects.h2.H2Connection
import org.jetbrains.squash.expressions.count
import org.jetbrains.squash.expressions.eq
import org.jetbrains.squash.query.*
import org.jetbrains.squash.results.ResultRow
import org.jetbrains.squash.results.get
import org.jetbrains.squash.schema.create
import org.jetbrains.squash.statements.fetch
import org.jetbrains.squash.statements.insertInto
import org.jetbrains.squash.statements.values

fun ResultRow.toSparseAddon() = SparseAddon(
        id = this[SparseAddons.id],
        addonId = this[SparseAddons.addonId],
        name = this[SparseAddons.name]
)

//TODO: add more database row conversion


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

    override fun getAll(size: Long) = db.transaction {
        from(SparseAddons)
                .select()
//                .orderBy(Addons.date, ascending = false)
                .limit(size)
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
        }.fetch(SparseAddons.id).execute()
    }


    override fun close() { }

}