package moe.nikky.curseproxy.dao

import kotlinx.coroutines.CoroutineScope
import moe.nikky.curseproxy.LOG
import moe.nikky.curseproxy.curse.CurseClient
import moe.nikky.curseproxy.model.CurseAddon
import moe.nikky.curseproxy.model.graphql.Addon
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import org.slf4j.Logger
import kotlin.system.measureTimeMillis

/**
 * Created by nikky on 25/05/18.
 * @author Nikky
 * @version 1.0
 */
open class AddonsImporter : KoinComponent {
    private val addonDatabase by inject<AddonStorage>()

    val processedIDs = mutableSetOf<Int>()
    val processableIDs = mutableSetOf<Int>()

    suspend fun CoroutineScope.import(log: Logger) {
        processedIDs.clear()
        processableIDs.clear()
        var addons: List<CurseAddon>? = null
        LOG.info("get addons fromCurseAddon search")
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

        val sections: MutableSet<Triple<String, Int, Int>> = mutableSetOf()

        val idRange = (0..processedIDs.max()!!+10000)
//        val idRange = (0..305914+10000)
        val chunkedRange = idRange.chunked(1000).shuffled()
        LOG.info("scanning ids ${idRange.start}..${idRange.endInclusive}")
        val startTime = System.currentTimeMillis()

        chunkedRange.forEachIndexed { i, it ->
            val timeElapsed = measureTimeMillis {
                LOG.info("processing ${it.first()} .. ${it.last()}")
                val result = with(CurseClient) { getAddons(it.toTypedArray(), ignoreErrors = true) }
                result?.forEach { addon ->
                    addonDatabase.replaceORCreate(Addon.fromCurseAddon(addon))
                    sections += Triple(addon.categorySection.name, addon.categorySection.id, addon.categorySection.gameID)
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
//                    addonDatabase.replaceORCreate(addon.toSparse())
//            }
//        }


        log.info("import complete")

        log.info("sections:")
        sections.forEach { (name, id, gameID) ->
            log.info("name: $name, id: $id, gameID: $gameID)")
        }
    }
}
