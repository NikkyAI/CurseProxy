package moe.nikky.curseproxy.dao

import moe.nikky.curseproxy.model.Addon
import moe.nikky.curseproxy.model.SparseAddon
import java.io.Closeable

interface AddonStorage : Closeable {

    fun createSparseAddon(sighting: SparseAddon): Int

    fun getSparseAddon(id: Int): SparseAddon?

    fun getAll(size: Long?, name: String?, author: String?, category: String?, section: String?): List<SparseAddon>

    //TODO: get addon by name ? only if not handled by graphql already
}