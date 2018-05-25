package moe.nikky.curseproxy.curse

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import moe.nikky.curseproxy.curse.CurseUtil.getAllFilesForAddOn
import moe.nikky.curseproxy.LOG
import moe.nikky.curseproxy.VersionComparator
import moe.nikky.curseproxy.model.Addon
import moe.nikky.curseproxy.model.AddonFile
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

/**
 * Created by nikky on 01/05/18.
 * @author Nikky
 * @version 1.0
 */
@Deprecated("use new CurseClient")
object CurseUtil : KoinComponent{
    val META_URL = "https://cursemeta.dries007.net"
    val useragent = "curseProxy (https://github.com/nikky/CurseProxy)"

    private val mapper: ObjectMapper by inject()

    fun getAddon(addonId: Int): Addon? {
        val url = "${META_URL}/api/v2/direct/GetAddOn/$addonId"

        LOG.debug("get $url")
        val (_, _, result) = url.httpGet()
                .header("User-Agent" to useragent)
                .responseString()
        return when (result) {
            is Result.Success -> {
                mapper.readValue(result.value)
            }
            is Result.Failure -> {
                LOG.error(result.error.toString())
                null
            }
        }
    }

    fun getAllFilesForAddOn(addonId: Int): List<AddonFile> {
        val url = "${META_URL}/api/v2/direct/GetAllFilesForAddOn/$addonId"

        LOG.debug("get $url")
        val (_, _, result) = url.httpGet()
                .header("User-Agent" to useragent)
                .responseString()
        return when (result) {
            is Result.Success -> {
                mapper.readValue(result.value)
            }
            else -> throw Exception("failed getting cursemeta data")
        }
    }

    fun getAddonFile(addonId: Int, fileId: Int): AddonFile? {
        val url = "${META_URL}/api/v2/direct/GetAddOnFile/$addonId/$fileId"

        LOG.debug("get $url")
        val (_, _, result) = url.httpGet()
                .header("User-Agent" to useragent)
                .responseString()
        return when (result) {
            is Result.Success -> {
                mapper.readValue(result.value)
            }
            else -> null
        }
    }
}

fun Addon.files(versions: List<String>): List<AddonFile> {
    val files = getAllFilesForAddOn(id)
    return if (versions.isEmpty()) {
        files.sortedByDescending { it.fileDate }
    } else {
        files.filter { it.gameVersion.intersect(versions).isNotEmpty() }.sortedByDescending { it.fileDate }
    }
}

fun Addon.filesLatestVersion(versions: List<String>): List<AddonFile> {
    val files = getAllFilesForAddOn(id)
    return if (versions.isEmpty()) {
        val version = files.map { it.gameVersion.sortedWith(VersionComparator.reversed()).first() }.sortedWith(VersionComparator.reversed()).first()
        files.filter { it.gameVersion.contains(version) }.sortedByDescending { it.fileDate }
    } else {
        files.filter { it.gameVersion.intersect(versions).isNotEmpty() }.sortedByDescending { it.fileDate }
    }
}

fun Addon.latestFile(versions: List<String>) = filesLatestVersion(versions).first()
