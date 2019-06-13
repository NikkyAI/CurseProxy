package moe.nikky.curseproxy

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import moe.nikky.curseproxy.data.AddonsImporter
import moe.nikky.curseproxy.di.mainModule
import org.koin.core.Koin
import org.koin.log.PrintLogger
import org.koin.standalone.StandAloneContext

fun main() {

    Koin.logger = PrintLogger()
    StandAloneContext.startKoin(listOf(mainModule))

    val json = Json(JsonConfiguration(prettyPrint = true, encodeDefaults = false))

    runBlocking {
//        val finds =  CurseClient.getAddonsByCriteria(432, searchFilter = "Entity")
//        LOG.info("found " + finds?.map { it.name })

        val importer = AddonsImporter()
        importer.test(LOG, json)
    }
//    LOG.info("Addons imported")

//    runBlocking(Dispatchers.IO) {
//
//    }
}
