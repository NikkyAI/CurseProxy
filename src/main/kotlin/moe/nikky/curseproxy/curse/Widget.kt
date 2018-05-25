package moe.nikky.curseproxy.curse

import kotlinx.html.*
import moe.nikky.curseproxy.LOG
import moe.nikky.curseproxy.VersionComparator
import moe.nikky.curseproxy.exceptions.AddonNotFoundException

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