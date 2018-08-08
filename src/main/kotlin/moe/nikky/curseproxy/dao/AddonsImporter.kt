package moe.nikky.curseproxy.dao

import moe.nikky.curseproxy.LOG
import moe.nikky.curseproxy.curse.CurseClient
import moe.nikky.curseproxy.model.CurseAddon
import moe.nikky.curseproxy.model.graphql.Addon
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

    private fun CurseAddon.toSparse() = Addon(
            id = this.id,
            name = this.name,
            slug = this.slug,
            primaryAuthorName = this.primaryAuthorName,
            primaryCategoryName = this.primaryCategoryName,
            sectionName = this.sectionName,
            dateModified = this.dateModified.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            dateCreated = this.dateCreated.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            dateReleased = this.dateReleased.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            categoryList = this.categoryList
    )

    val processedIDs = mutableSetOf<Int>()
    val processableIDs = mutableSetOf<Int>()

    suspend fun import(log: Logger) {
        processedIDs.clear()
        processableIDs.clear()
        var addons: List<CurseAddon>? = null
        val duration = measureTimeMillis {
            addons = CurseClient.getAllAddonsByCriteria(432)
        }
        LOG.info("loaded ${addons?.size ?: 0} addons in $duration ms")
        addons?.forEach { addon ->
            addonDatabase.createAddon(addon.toSparse())
            processedIDs += addon.id

            val dependencies = addon
                    .latestFiles
                    .flatMap { it.dependencies ?: emptyList() }
                    .distinctBy { it.addOnId }
            processableIDs.addAll(dependencies.map { it.addOnId })
        }

        processableIDs.forEach {
            if(!processedIDs.contains(it)) {
                val addon = CurseClient.getAddon(it)
                if(addon != null)
                    addonDatabase.createAddon(addon.toSparse())
            }
        }


        log.info("import complete")
    }
}
