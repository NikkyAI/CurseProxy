package moe.nikky.curseproxy.dao

import moe.nikky.curseproxy.model.Addon
import java.io.Closeable

interface AddonStorage : Closeable {

    fun createAddon(sighting: Addon): Int

    fun getAddon(id: Int): Addon?

    fun getAll(size: Long): List<Addon>

    //TODO: get addon by name ? only if not handled by graphql already
}