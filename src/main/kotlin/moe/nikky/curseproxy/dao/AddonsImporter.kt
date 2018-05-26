package moe.nikky.curseproxy.dao

import io.ktor.application.Application
import io.ktor.application.log
import moe.nikky.curseproxy.LOG
import moe.nikky.curseproxy.curse.CurseClient
import moe.nikky.curseproxy.model.Addon
import moe.nikky.curseproxy.model.SparseAddon
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import org.slf4j.Logger
import java.time.ZoneId
import kotlin.system.measureTimeMillis

/**
 * Created by nikky on 25/05/18.
 * @author Nikky
 * @version 1.0
 */
open class AddonsImporter : KoinComponent {
    private val addonDatabase by inject<AddonStorage>()

    fun import(log: Logger) {
        var addons: List<Addon>? = null
        val duration = measureTimeMillis {
            addons = CurseClient.getAllAddonsByCriteria(432)
        }
        LOG.info("loaded ${addons?.size ?: 0} addons in $duration ms")
//        val addons = CurseClient.getAllAddonsByCriteria(432)
        addons?.forEach { addon ->
            val sparse = SparseAddon(
                    addonId = addon.id,
                    name = addon.name,
                    primaryAuthorName = addon.primaryAuthorName,
                    primaryCategoryName = addon.primaryCategoryName,
                    sectionName = addon.sectionName,
                    dateModified = addon.dateModified.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                    dateCreated = addon.dateCreated.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                    dateReleased = addon.dateReleased.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            )
            addonDatabase.createSparseAddon(sparse)
        }
        log.info("import complete")
    }
}

fun Application.importData() {
    AddonsImporter().import(log)
}