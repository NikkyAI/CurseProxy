package moe.nikky.curseproxy.dao

import moe.nikky.curseproxy.model.Section
import moe.nikky.curseproxy.model.graphql.Addon
import java.io.Closeable

interface AddonStorage : Closeable {

    fun createAddon(addon: Addon)

    fun getAddon(id: Int): Addon?

    fun getAll(size: Long?, name: String?, author: String?, category: String?, section: Section?): List<Addon>
}