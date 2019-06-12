package moe.nikky.curseproxy.dao

import moe.nikky.curseproxy.model.graphql.SimpleAddon
import java.io.Closeable

interface AddonStorage : Closeable {

    fun replaceORCreate(addon: SimpleAddon)

    fun getAddon(id: Int): SimpleAddon?

    fun getAll(
        gameId: Int?,
        name: String?,
        slug: String?,
        category: String?,
        section: String?,
        gameVersions: List<String>?
    ): List<SimpleAddon>
}