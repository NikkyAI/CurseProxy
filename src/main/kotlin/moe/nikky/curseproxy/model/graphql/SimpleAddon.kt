package moe.nikky.curseproxy.model.graphql

import moe.nikky.curseproxy.model.Addon
import java.time.LocalDate

data class SimpleAddon(
    val id: Int,
    val gameID: Int,
    val name: String,
    val slug: String,
    val section: String,
    val dateModified: LocalDate,
    val dateCreated: LocalDate,
    val dateReleased: LocalDate,
    val categoryList: List<String>,
    val gameVersions: Set<String>
) {
    companion object {
        fun fromAddon(addon: Addon): SimpleAddon {
            return SimpleAddon(
                id = addon.id.value,
                gameID = addon.gameId,
                name = addon.name,
                slug = addon.slug,
//                sectionName = Section.fromId(addon.categorySection.id)?.sectionName ?: "unknown_${addon.sectionName}_${addon.categorySection.id}",
                section = addon.categorySection.name,
                dateModified = addon.dateModified.toLocalDate(),
                dateCreated = addon.dateCreated.toLocalDate(),
                dateReleased = addon.dateReleased.toLocalDate(),
                categoryList = addon.categories.map { it.name },
                gameVersions = addon.gameVersionLatestFiles.map { it.gameVersion }.toSet() + addon.latestFiles.flatMap { it.gameVersion }.toSet()
            )
        }
    }
}