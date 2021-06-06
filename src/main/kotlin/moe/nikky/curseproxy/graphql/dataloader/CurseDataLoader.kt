package moe.nikky.curseproxy.graphql

import com.expediagroup.graphql.server.execution.KotlinDataLoader
import kotlinx.coroutines.coroutineScope
import moe.nikky.curseproxy.curse.CurseClient
import moe.nikky.curseproxy.model.manifest.CurseManifest
import org.dataloader.DataLoader
import kotlinx.coroutines.future.future
import kotlinx.coroutines.runBlocking
import moe.nikky.curseproxy.model.Addon
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.suspendCoroutine

object CurseDataLoader: KotlinDataLoader<List<Int>, List<Addon>> {
    override val dataLoaderName = "CURSE_PROJECT_DATALOADER"

    override fun getDataLoader() = DataLoader<List<Int>, List<Addon>>() { ids ->
        runBlocking {
            future {
                val allCursemanifests = CurseClient.getAddons(projectIds = ids.flatten()) ?: listOf()

                ids.fold(mutableListOf()) { acc: MutableList<List<Addon>>, idSet ->
                    val matching = allCursemanifests.filter { idSet.contains(it.id) }
                    acc.add(matching)
                    acc
                }
            }
        }
    }
}