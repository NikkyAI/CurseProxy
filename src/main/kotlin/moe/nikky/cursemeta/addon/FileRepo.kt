package moe.nikky.cursemeta.addon

import addons.curse.AddOnFile
import moe.nikky.cursemeta.addon.AddOnServiceClient.Companion.client

/**
 * Created by nikky on 27/02/18.
 * @author Nikky
 * @version 1.0
 */
object FileRepo {

    fun get(addonID: Int): List<addons.curse.AddOnFile> {

        val files = client.getAllFilesForAddOn(addonID)
        files.forEach {
            //                if (it.attachments == null) {
//                    LOG.info("attachment of ${it.name} was null")
//                    it.attachments = listOf()
//                }
        }
        return files

    }

    fun get(addonID: Int, fileID: Int): AddOnFile? {
        return client.getAddOnFile(addonID, fileID)
    }

    fun getChangelog(addonID: Int, fileID: Int): String? {
        return client.v2GetChangeLog(addonID, fileID)
    }

}