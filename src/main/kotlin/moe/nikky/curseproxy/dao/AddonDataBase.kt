package moe.nikky.curseproxy.dao

import moe.nikky.curseproxy.model.Addon
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

fun ResultRow.toAddon() = Addon(
        id = this[Addons.id],
        addonId = this[Addons.addonId],
        name = this[Addons.name]
)

//TODO: add more database row conversion


class AddonDatabase(val db: DatabaseConnection = H2Connection.createMemoryConnection()) : AddonStorage {
    init {
        db.transaction {
            databaseSchema().create(Addons)
        }
    }
    override fun getAddon(id: Int) = db.transaction {
        val row = from(Addons).where { Addons.id eq id }.execute().singleOrNull()
        row?.toAddon()
    }

    override fun getAll(size: Long) = db.transaction {
        from(Addons)
                .select()
//                .orderBy(Addons.date, ascending = false)
                .limit(size)
                .execute()
                .map { it.toAddon() }
                .toList()
    }

    override fun createAddon(addon: Addon) = db.transaction {
        insertInto(Addons).values {
            it[addonId] = addon.addonId
            it[name] = addon.name
        }.fetch(Addons.id).execute()
    }


    override fun close() { }

}