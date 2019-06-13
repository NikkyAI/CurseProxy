package moe.nikky.curseproxy.data

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import moe.nikky.curseproxy.LOG
import moe.nikky.curseproxy.curse.CurseClient
import moe.nikky.curseproxy.model.Addon
import moe.nikky.curseproxy.util.measureTimeMillis1
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import org.slf4j.Logger
import kotlin.system.measureTimeMillis

/**
 * Created by nikky on 25/05/18.
 * @author Nikky
 * @version 1.0
 */
open class AddonsImporter() : KoinComponent {
    private val database by inject<CurseDatabase>()

    val processedIDs = mutableSetOf<Int>()
    val processableIDs = mutableSetOf<Int>()

    suspend fun import(log: Logger) = coroutineScope {
        processedIDs.clear()
        processableIDs.clear()
        LOG.info("get addons fromCurseAddon search")
        val (searchResult, duration) = measureTimeMillis1 {
            CurseClient.getAddonsByCriteria(432, sort = CurseClient.AddonSortMethod.LastUpdated)
        }
        LOG.info("loaded ${searchResult?.size ?: "null"} addons in $duration ms")
        searchResult?.forEach { addon ->
            //            LOG.info("${addon.name}: ${addon.dateModified}")
            processedIDs += addon.id

            val dependencies = addon
                .latestFiles
                .flatMap { it.dependencies }
                .distinctBy { it.addonId }
            processableIDs.addAll(dependencies.map { it.addonId })
            Unit
        }

        val idRange = (0..processedIDs.max()!! + 10000)
//        val idRange = (0..324142+10000)
        val chunkedRange = idRange.chunked(1000).shuffled()
        LOG.info("scanning ids ${idRange.start}..${idRange.endInclusive}")
        val startTime = System.currentTimeMillis()

//        val insertChannel = Channel<Addon>(-1)

        coroutineScope {
            //            launch(Dispatchers.IO) {
//                for (addon in insertChannel) {
//                    database.store(addon)
//                }
//            }

            chunkedRange.forEachIndexed { i, ids ->
                val timeElapsed = measureTimeMillis {
                    LOG.info("processing ${ids.first()} .. ${ids.last()}")
                    var success = false
                    while(!success) {
                        val result = CurseClient.getAddons(ids, ignoreErrors = false, fail = false)
                        if(result == null) {
                            delay(1000)
                            continue
                        }
                        success = true
//                        launch(Dispatchers.IO) {
                        database.transaction {
                            result.forEach { addon ->
                                database.store(addon)
                            }
                        }
//                        }
                        LOG.info("added ${result.count()} addons")
                    }
                }
                val step = i + 1.toDouble()
                val timeSinceStart = System.currentTimeMillis() - startTime
                val averageTimeElapsed = timeSinceStart / step
                LOG.info("current:    ${timeElapsed / 1000.0}s")
                LOG.info("sinceStart: ${timeSinceStart / 1000.0}s")
                LOG.info("average:    ${averageTimeElapsed / 1000.0}s")
                LOG.info("prediction-current: ${timeElapsed * (chunkedRange.count() - step) / 1000.0}s")
                LOG.info("prediction-average: ${averageTimeElapsed * (chunkedRange.count() - step) / 1000.0}s")
            }
        }

//        insertChannel.close()
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

    suspend fun test(log: Logger, json: Json) = coroutineScope {
        val addons = database.testAddons(5, true)
        log.info("loaded ${addons.size} addons from db")
        log.info("")
        addons.forEach { addon ->
            log.info(json.stringify(Addon.serializer(), addon))
        }
    }
}
