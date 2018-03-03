package moe.nikky.curseproxy.addon

import addons.curse.AddOn
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import kotlinx.coroutines.experimental.async
import moe.nikky.curseproxy.LOG
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

        /**
         * Created by nikky on 27/02/18.
         * @author Nikky
         * @version 1.0
         */
typealias IDMap = ConcurrentMap<Int, MutableSet<Int>>

object IDCache {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val idFile = File("ids.json")
    private val idMap: IDMap

    init {
        LOG.info("initializing ID map")
        idMap = if (idFile.isFile)
            gson.fromJson(idFile.readText(), object : TypeToken<IDMap>() {}.type)
        else {
            ConcurrentHashMap()
        }
    }

    //TODO: make get and save threadsafe / synchronized and run on the background
    //TODO: must not block requests

    fun save() {
        async {
            idFile.writeText(gson.toJson(idMap))
        }
    }

    fun set(addonID: Int) {
        async {
            val mutableSet = idMap[addonID]
            if (mutableSet == null) {
                idMap[addonID] = mutableSetOf<Int>().apply { idMap[addonID] = this }
            }
        }
    }

    fun set(addonIDs: Iterable<Int>) {
        async {
            addonIDs.forEach {
                set(it)
            }
        }
    }

    fun set(addons: Int2ObjectMap<AddOn>) {
        async {
            addons.forEach { (id, addon) ->
                set(id, addon.latestFiles.map { it.id })
            }
        }
    }

    fun set(addonID: Int, fileIDs: List<Int>) {
        async {
            //LOG.debug("adding $addonID $fileIDs to cache")
            val ids = idMap[addonID] ?: mutableSetOf<Int>().apply { idMap[addonID] = this }
            ids.addAll(fileIDs)
        }
    }
    fun set(addonID: Int, fileID: Int) {
        async {
            //LOG.debug("adding $addonID $fileIDs to cache")
            val ids = idMap[addonID] ?: mutableSetOf<Int>().apply { idMap[addonID] = this }
            ids.add(fileID)
        }
    }

    fun getFileIDs(addonID: Int): Set<Int>? {
        return idMap[addonID]
    }

    fun getAddOnIDs(): Set<Int> {
        return idMap.keys.toSet()
    }

    fun getIDMap(): Map<Int, Set<Int>> {
        return idMap.toMap()
    }
}