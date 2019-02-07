package moe.nikky.curseproxy

import kotlinx.coroutines.runBlocking
import moe.nikky.curseproxy.curse.CurseClient
import moe.nikky.curseproxy.curse.auth.AuthToken
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

    runBlocking {
        val addons = CurseClient.getAllAddonsByCriteria(
            gameId = 432,
            gameVersions = listOf("1.12", "1.12.1", "1.12.2")
        )

        addons!!.forEach {
            LOG.info("addon: ${it.id}")
            LOG.info("categories: ${it.categories.map { c -> c.id to c.name }}")
//            val files = CurseClient.getAddonFiles(it.id)
//            val gameVersions = files!!.flatMap { file -> file.gameVersion }.toSet()
//            LOG.info("versions: $gameVersions")
        }
    }
}
