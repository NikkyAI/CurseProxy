package moe.nikky.curseproxy

import kotlinx.coroutines.runBlocking
import moe.nikky.curseproxy.curse.CurseClient
import moe.nikky.curseproxy.curse.auth.AuthToken
import moe.nikky.curseproxy.dao.AddonsImporter
import moe.nikky.curseproxy.di.mainModule
import org.koin.core.Koin
import org.koin.log.PrintLogger
import org.koin.standalone.StandAloneContext

fun main(args: Array<String>) {

    Koin.logger = PrintLogger()
    StandAloneContext.startKoin(listOf(mainModule))

    AuthToken.test()

//    runBlocking {
//        val finds =  CurseClient.getAddonsByCriteria(432, searchFilter = "Entity")
//        LOG.info("found " + finds?.map { it.name })
//
//        val importer = AddonsImporter()
//        importer.import(LOG)
//    }
//    LOG.info("Addons imported")
}
