package moe.nikky.curseproxy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import moe.nikky.curseproxy.curse.CurseClient
import moe.nikky.curseproxy.curse.auth.AuthToken
import moe.nikky.curseproxy.di.mainModule
import moe.nikky.curseproxy.model.Section
import org.koin.core.Koin
import org.koin.log.PrintLogger
import org.koin.standalone.StandAloneContext

fun main() {

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

    runBlocking(Dispatchers.IO) {
        //        (1..700).forEach { gameId ->
//            val gameName = CurseClient.getAddonsByCriteria(
//                gameId = gameId,
//                pageSize = 1
//            )?.firstOrNull()?.gameName
//                ?: return@forEach
//            val pool = newFixedThreadPoolContext(3, "pool")
//            (0..6000).map { id ->
//                async(pool) {
//                    val addons = CurseClient.getAddonsByCriteria(
//                        gameId = gameId,
//                        sectionId = id,
//                        pageSize = 1
//                    )
//                    addons?.firstOrNull()?.let { a ->
//                        a.categorySection.id to "sectionName: ${a.sectionName} name: ${a.categorySection.name}"
//                    }
//                }
//            }.also { deferredSections ->
//                val sections =
//                    deferredSections.awaitAll().filterNotNull().associate { it }
//
//                pool.close()
//                LOG.info("sections for '$gameName' gameId: $gameId")
//                sections.forEach { (k, v) ->
//                    LOG.info("$k = $v")
//                }
//            }
//        }
//
//
//        exitProcess(0)

        val categories = listOf(423)
        val section = Section.MODS.id
        val versions = listOf("1.13.2")
        val addons = CurseClient.getAllAddonsByCriteria(
//            searchFilter = "neat",
//            pageSize = 20,
            gameId = 432,
            sectionId = section,
            categoryIds = categories,
            gameVersions = versions
        )

        val contains1710 = addons!!.map {
            //            LOG.info("addon: ${it.id}")
//            LOG.info("categories: ${it.categorySection.let { s -> s.id to s.name }}")
            val files = CurseClient.getAddonFiles(it.id)
            val gameVersions = files!!.flatMap { file -> file.gameVersion }.toSet()
            LOG.info("versions: $gameVersions")
            LOG.info("categories: ${it.categories}")
            require(section == it.categorySection.id) {
                "does not match section: $section"
            }
            require(it.categories.any { c -> categories.contains(c.id) }) {
                "does not contain any category: $categories"
            }
            require(versions.any { gameVersions.contains(it) }) {
                "does not contain all gameversions: $versions"
            }
            gameVersions.contains("1.7.10")

        }

        LOG.info("gameVersions.contains(\"1.7.10\"): ${contains1710.any { it }}")

        val categoriesResult = addons.flatMap { it.categories.map { it.id } }.toSet()
        val sectionsResult = addons.map { it.categorySection.id }.toSet()
//        require(categories.all { categoriesResult.contains(it) }) {
//            "$sectionsResult does not contains all $categories"
//        }
        require(sectionsResult.contains(section)) {
            "$sectionsResult does not contains all $section"
        }
        LOG.info("sections: $sectionsResult")
        LOG.info("categories: $categoriesResult")
    }
}
