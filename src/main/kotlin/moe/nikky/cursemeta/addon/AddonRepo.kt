package moe.nikky.cursemeta.addon

import addons.curse.AddOn
import com.thiakil.curseapi.json.ProjectFeedDownloader
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import moe.nikky.cursemeta.addon.AddOnServiceClient.Companion.client
import moe.nikky.cursemeta.LOG
import java.io.File

/**
 * Created by nikky on 27/02/18.
 * @author Nikky
 * @version 1.0
 */
object AddonRepo {
    private val db = ProjectFeedDownloader()
    private val dbStorage = File(ProjectFeedDownloader.DEFAULT_CACHE_DIR, "db.json")

    private var projectIds = listOf<Int>()

    fun sync() {
        db.load(dbStorage)
        db.sync()
        db.save(dbStorage)
        projectIds = db.addOns.keys.toList()
        LOG.debug("loaded ${projectIds.count()} addons")
        LOG.debug("loaded ${db.addOns.count()} addons")
        IDCache.set(db.addOns)
        IDCache.save()
    }

    fun get(): List<addons.curse.AddOn> {
        try {
//            val ids = mutableListOf<Int>()//IDCache.getAddOnIDs().toMutableSet()
//            projectIds.forEach { ids += it }
//            val addons = client.v2GetAddOns(*ids.toIntArray())
//            addons.forEach {
//                if (it.attachments == null) {
//                    LOG.info("attachment of ${it.name} was null")
//                    it.attachments = listOf()
//                }
//            }
            sync()
            val addons = db.addOns.values

            return addons.toList()

        } catch (e: Exception) {
            LOG.error("Error retrieving AddOns", e)
            return listOf()
        }
    }

    fun get(id: Int): AddOn? {
        return client.getAddOn(id)
    }

    fun getDescription(id: Int): String? {
        return client.v2GetAddOnDescription(id)
    }

}