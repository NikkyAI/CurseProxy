package moe.nikky.curseproxy.dao

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import moe.nikky.curseproxy.LOG
import moe.nikky.curseproxy.curse.CurseClient
import moe.nikky.curseproxy.curse.util.measureTimeMillis
import moe.nikky.curseproxy.data.CurseDatabase
import moe.nikky.curseproxy.model.Addon
import moe.nikky.curseproxy.model.graphql.SimpleAddon
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import org.slf4j.Logger

/**
 * Created by nikky on 25/05/18.
 * @author Nikky
 * @version 1.0
 */
open class AddonsImporter(val database: CurseDatabase) : KoinComponent {
    private val addonDatabase by inject<AddonStorage>()

    val processedIDs = mutableSetOf<Int>()
    val processableIDs = mutableSetOf<Int>()

    suspend fun import(log: Logger) = coroutineScope {
        processedIDs.clear()
        processableIDs.clear()
        var addons: List<moe.nikky.curseproxy.model.Addon>? = null
        LOG.info("get addons fromCurseAddon search")
        val duration = measureTimeMillis {
            addons = CurseClient.getAddonsByCriteria(432, sort = CurseClient.AddonSortMethod.LastUpdated)
        }
        LOG.info("loaded ${addons?.size ?: 0} addons in $duration ms")
        addons?.forEach { addon ->
            LOG.info("${addon.name}: ${addon.dateModified}")
            processedIDs += addon.id.value

            val dependencies = addon
                    .latestFiles
                    .flatMap { it.dependencies ?: emptyList() }
                    .distinctBy { it.addonId }
            processableIDs.addAll(dependencies.map { it.addonId.value })
            Unit
        }

        val idRange = (0..processedIDs.max()!!+10000)
//        val idRange = (0..305914+10000)
        val chunkedRange = idRange.chunked(1000).shuffled()
        LOG.info("scanning ids ${idRange.start}..${idRange.endInclusive}")
        val startTime = System.currentTimeMillis()

        val insertChannel = Channel<Addon>()

        launch(Dispatchers.IO) {
            for (addon in insertChannel) {
                database.addonQueries.replace(
                    id = addon.id.value,
                    name = addon.name,
//                        authors = addon.authors
//                        attachments = addon.attachments
                    websiteUrl = addon.websiteUrl,
                    gameId = addon.gameId,
                    summary = addon.summary,
                    defaultFileId = addon.defaultFileId,
                    downloadCount = addon.downloadCount,
//                        latestFIles = addons.latestFiles
//                        categories = addons.categories
                    status = addon.status,
                    categorySection = addon.categorySection,
                    slug = addon.slug,
//                        gameVersionLatestFiles = addon.gameVersionLatestFiles,
                    popularityScore = addon.popularityScore,
                    gamePopularityRank = addon.gamePopularityRank,
                    gameName = addon.gameName,
                    portalName = addon.portalName,
                    dateModified = addon.dateModified,
                    dateCreated = addon.dateCreated,
                    dateReleased = addon.dateReleased,
                    isAvailable = addon.isAvailable,
                    primaryLanguage = addon.primaryLanguage,
                    isFeatured = addon.isFeatured
                )
            }
        }

        chunkedRange.forEachIndexed { i, it ->
            val timeElapsed = measureTimeMillis {
                LOG.info("processing ${it.first()} .. ${it.last()}")
                val result = with(CurseClient) {
                    getAddons(it, ignoreErrors = true)
                }
                launch {
                    result?.forEach { addon ->
                        insertChannel.send(addon)
//                        addonDatabase.replaceORCreate(SimpleAddon.fromAddon(addon))
                    }
                    LOG.info("added ${result?.count()} addons")
                }
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
        insertChannel.close()
//        processableIDs.forEach {
//            if(!processedIDs.contains(it)) {
//                CurseClient.getAddons()
//                val addon = CurseClient.getAddon(it, ignoreError = true)
//                if(addon != null)
//                    addonDatabase.replaceORCreate(addon.toSparse())
//            }
//        }


        log.info("import complete")
    }
}
