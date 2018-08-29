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
            gameID = this.gameId,
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
            addons = CurseClient.getAddonsByCriteria(432, sort = CurseClient.AddonSortMethod.LastUpdated)
        }
        LOG.info("loaded ${addons?.size ?: 0} addons in $duration ms")
        addons?.forEach { addon ->
            LOG.info("${addon.name}: ${addon.dateModified}")
            processedIDs += addon.id

            val dependencies = addon
                    .latestFiles
                    .flatMap { it.dependencies ?: emptyList() }
                    .distinctBy { it.addOnId }
            processableIDs.addAll(dependencies.map { it.addOnId })
        }

        val idRange = (0..processedIDs.max()!!+10000)
        val chunkedRange = idRange.chunked(1000).shuffled()
        LOG.info("scanning ids ${idRange.start}..${idRange.endInclusive}")
        val startTime = System.currentTimeMillis()

        chunkedRange.forEachIndexed { i, it ->
            val timeElapsed = measureTimeMillis {
                LOG.info("processing ${it.first()} .. ${it.last()}")
                val result = CurseClient.getAddons(it.toTypedArray(), ignoreErrors = true)
                result?.forEach { addon ->
                    addonDatabase.createAddon(addon.toSparse())
                }
                LOG.info("added ${result?.count()} addons")
            }
            val step = i+1.toDouble()
            val timeSinceStart = System.currentTimeMillis() - startTime
            val averageTimeElapsed = timeSinceStart / step
            LOG.info("current:    ${timeElapsed / 1000.0}s")
            LOG.info("sinceStart: ${timeSinceStart / 1000.0}s")
            LOG.info("average:    ${averageTimeElapsed / 1000.0}s")
            LOG.info("prediction-current: ${timeElapsed * (chunkedRange.count() - step) / 1000.0}s")
            LOG.info("prediction-average: ${averageTimeElapsed * (chunkedRange.count() - step) / 1000.0}s")
        }
//        processableIDs.forEach {
//            if(!processedIDs.contains(it)) {
//                CurseClient.getAddons()
//                val addon = CurseClient.getAddon(it, ignoreError = true)
//                if(addon != null)
//                    addonDatabase.createAddon(addon.toSparse())
//            }
//        }


        log.info("import complete")
    }
}
