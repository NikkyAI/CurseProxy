package moe.nikky.curseproxy.dao

import moe.nikky.curseproxy.model.Section
import moe.nikky.curseproxy.model.graphql.Addon
import java.io.Closeable

interface AddonStorage : Closeable {

    fun replaceORCreate(addon: Addon)

    fun getAddon(id: Int): Addon?

    fun getAll(
        gameId: Int?,
        name: String?,
        slug: String?,
        category: String?,
        section: Section?,
        gameVersions: List<String>?
    ): List<Addon>
}