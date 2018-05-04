package moe.nikky.curseproxy

import kotlinx.html.*
import moe.nikky.curseproxy.exceptions.AddonNotFoundException

/**
 * Created by nikky on 01/05/18.
 * @author Nikky
 * @version 1.0
 */

object VersionComparator : Comparator<String> {
    override fun compare(o1: String, o2: String): Int {
        val v1 = o1.split(".")
        val v2 = o2.split(".")
        var i = 0
        while (true) {
            val part1 = v1.getOrNull(i)
            val part2 = v2.getOrNull(i)

            if (part1 == null && part2 == null) return 0

            val version1 = part1?.toIntOrNull() ?: return -1
            val version2 = part2?.toIntOrNull() ?: return 1

            if (version1 != version2) {
                return Integer.compare(version1, version2)
            }
            i++
        }
    }

}

object Widget {

    fun HTML.widget(id: Int, versions: MutableList<String>) {
        val addon = CurseUtil.getAddon(id) ?: throw AddonNotFoundException(id)
        val files = CurseUtil.getAllFilesForAddOn(id)

        if (versions.isEmpty()) {
            val sorted = files.map { it.gameVersion.sortedWith(VersionComparator.reversed()).first() }.sortedWith(VersionComparator.reversed())
            LOG.info("sorted: $sorted")
            versions.add(sorted.first())
        }

        val fileMap = files.groupBy { it.gameVersion.sortedWith(VersionComparator.reversed()).first() }
                .mapValues {
                    it.value.sortedByDescending { it.fileDate }
                }.toSortedMap(VersionComparator.reversed())

//        val sorted = files.sortedWith(compareByDescending(VersionComparator) { it.gameVersion.sortedWith(VersionComparator).last() })
        fileMap.forEach { key, list ->
            LOG.info("version: $key")
            LOG.info("list: $list")
            list.forEach {
                LOG.info("file: ${it.fileName}")
            }
        }

        head {
            meta {
                name = "viewport"
                content = "width=device-width,initial-scale=1"
            }
            styleLink("/api/widget.css")
        }
        body("bg-transparent") {
            div {
                attributes["id"] = "widget"
                div("wrapper clearfix") {
                    val attachment = addon.attachments?.find { it.isDefault }!!

                    div("thumb") {
                        img(alt = attachment.description, src = attachment.thumbnailUrl) {
                        }
                    }
                    div("meta") {
                        span("line lead") {
                            a {
                                title = addon.name
                                target = "_blank"
                                attributes["id"] = "title-link"
                                href = addon.webSiteURL
                                +addon.name
                            }
                            small { +" by ${addon.primaryAuthorName}" }
                        }
                        span("line smaller") {
                            +"${addon.downloadCount.toInt()} Downloads"
                        }
                        span("line small") {
                            +addon.summary
                        }
                        span("line small") {
                            +"version info etc.."
                        }
                        div("line bottom clearfix") {
                            versions.forEach { version ->
                                val file = fileMap[version]?.first()
                                if (file != null) {
                                    a(classes = "files-button") {
                                        href = file.downloadURL
                                        target = "_blank"
                                        attributes["id"] = "download-button"
                                        +"Download ${file.fileName}"
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }

    }

}