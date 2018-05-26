package moe.nikky.curseproxy.curse

import moe.nikky.curseproxy.VersionComparator
import moe.nikky.curseproxy.model.Addon
import moe.nikky.curseproxy.model.AddonFile

/**
 * Created by nikky on 26/05/18.
 * @author Nikky
 * @version 1.0
 */

fun Addon.files(versions: List<String>): List<AddonFile> {
    val files = CurseClient.getAddonFiles(id) ?: emptyList()
    return if (versions.isEmpty()) {
        files.sortedByDescending { it.fileDate }
    } else {
        files.filter { it.gameVersion.intersect(versions).isNotEmpty() }.sortedByDescending { it.fileDate }
    }
}

fun Addon.filesLatestVersion(versions: List<String>): List<AddonFile> {
    val files = CurseClient.getAddonFiles(id) ?: emptyList()
    return if (versions.isEmpty()) {
        val version = files.map { it.gameVersion.sortedWith(VersionComparator.reversed()).first() }.sortedWith(VersionComparator.reversed()).first()
        files.filter { it.gameVersion.contains(version) }.sortedByDescending { it.fileDate }
    } else {
        files.filter { it.gameVersion.intersect(versions).isNotEmpty() }.sortedByDescending { it.fileDate }
    }
}

fun Addon.latestFile(versions: List<String>) = filesLatestVersion(versions).first()
