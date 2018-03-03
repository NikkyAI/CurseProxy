package moe.nikky.cursemeta.addon

import addons.curse.AddOn
import addons.curse.AddOnFile
import com.curse.addonservice.*
import com.google.gson.Gson
import com.thiakil.curseapi.login.CurseAuth
import com.thiakil.curseapi.login.CurseToken
import com.thiakil.curseapi.login.LoginSession
import com.thiakil.curseapi.soap.AddOnService
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import moe.nikky.cursemeta.LOG
import moe.nikky.cursemeta.auth.CurseCredentials
import moe.nikky.exceptionString
import org.datacontract.schemas._2004._07.curse_addonservice_requests.AddOnFileKey
import java.io.File

/**
 * Created by nikky on 27/02/18.
 * @author Nikky
 * @version 1.0
 */
class AddOnServiceClient(private val stub: AddOnService) : AddOnService by stub {
    companion object {
        private var internalClient: AddOnServiceClient? = null
        lateinit var session: LoginSession
        val client: AddOnServiceClient
            get() {
                if (internalClient == null) {
                    val auth = CurseCredentials.load()
                    session = CurseAuth
                            .getResponseFromCurseAccount(
                                    auth.username,
                                    auth.password
                            ).session
//                            .getResponseFromTwitchOauth("").session

                    val token = CurseToken(session.userID, session.token)
                    internalClient = AddOnServiceClient(AddOnService.initialise(token))

                    LOG.debug("expires: ${session.expires}")
                    LOG.info("renewAfter: ${session.renewAfter}")
                }

                val now = System.currentTimeMillis()
                val diff = session.renewAfter - now
//                LOG.debug("time until renew: ${diff / 1000.0 / 60.0 / 60.0} Hours")
//                LOG.debug("time until expire: ${(session.expires - now) / 1000.0 / 60.0 / 60.0} Hours")
                if (diff < 0) {
                    val response = CurseAuth.renewAccessToken(session.token)
                    LOG.debug("old token: ${session.token}")
                    session.token = response.Token
                    session.renewAfter = response.RenewAfter
                    session.expires = response.Expires

                    LOG.debug("new token: ${session.token}")
                    LOG.debug("new expires: ${session.expires / 1000.0 / 60.0 / 60.0}")
                    LOG.debug("new renewAfter: ${session.renewAfter / 1000.0 / 60.0 / 60.0}")
                    val token = CurseToken(session.userID, session.token)
                    internalClient = AddOnServiceClient(AddOnService.initialise(token))
                }

                return internalClient!!
            }
    }

    override fun getAddOn(id: Int): AddOn? {
        val request = GetAddOn()
        request.id = id
        val response = stub.getAddOn(id) ?: return null

        IDCache.set(id, response.latestFiles.map { it.id })
        IDCache.save()

        return response
    }

    override fun v2GetAddOns(vararg ids: Int): List<AddOn> {
        return ids.toList().chunked(8192).map {
            //LOG.debug("requesting $ids")
            val response = try {
                stub.v2GetAddOns(*it.toIntArray()) ?: listOf()
            } catch (e: Exception) {
                LOG.error(e.exceptionString)
                ids.forEach {

                    try {
//                        LOG.debug("requesting $it")
                        val junk = getAddOn(it)
                    } catch (e: Exception) {
                        LOG.error("$it ${e.message} https://cursemeta.nikky.moe/api/addon/$it")
                    }
                }
                return listOf()
            }

            IDCache.set(response.map { it.id })
            IDCache.save()

            response
        }.flatten()
    }

    override fun v2GetAddOnDescription(id: Int): String? {
        val response = stub.v2GetAddOnDescription(id) ?: return null

        IDCache.set(id)
        IDCache.save()

        return response
    }

    override fun getAddOnFile(addonID: Int, fileID: Int): AddOnFile? {
        val response = stub.getAddOnFile(addonID, fileID)
        //if (!response) listOf<AddOn>()

        IDCache.set(addonID, listOf(fileID))
        IDCache.save()

        return response
    }

    override fun getAddOnFiles(vararg keys: AddOnFileKey): Int2ObjectMap<MutableList<AddOnFile>>? {
        LOG.debug("requesting files $keys")
        val chunks = keys.toList().chunked(8192).map { keysChunk ->
            stub.getAddOnFiles(*keysChunk.toTypedArray())
                    ?: mapOf<Int, MutableList<AddOnFile>>() as Int2ObjectMap<MutableList<AddOnFile>>

        }
        val destination = Int2ObjectLinkedOpenHashMap<MutableList<AddOnFile>>()
        chunks.forEach {
            it.forEach { (key, value) ->
                val list = destination[key] ?: mutableListOf()
                list.addAll(value)
                IDCache.set(key, value.map { it.id })
                if (list.isNotEmpty())
                    destination[key] = list
            }
        }
        IDCache.save()

        return destination
    }

    override fun getAllFilesForAddOn(addonID: Int): List<AddOnFile> {
        val fileIDs = IDCache.getFileIDs(addonID) ?: mutableSetOf()
        val fileList = when {
            fileIDs.isNotEmpty() -> {
                val map = stub.getAddOnFiles(*fileIDs.map { AddOnFileKey(addonID, it) }.toTypedArray())
                if (map == null)
                    listOf()
                else
                    map[addonID] ?: listOf()
            }
            else -> mutableListOf()
        }

        val files = stub.getAllFilesForAddOn(addonID) ?: listOf()

        val ids = files.map { it.id }
        IDCache.set(addonID, ids)
        IDCache.save()

        fileList.forEach { newFile ->
            if (!ids.contains(newFile.id)) {
                files += newFile
            }
        }

        return files
    }

}