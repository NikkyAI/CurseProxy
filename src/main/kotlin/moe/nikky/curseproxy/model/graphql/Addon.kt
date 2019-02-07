package moe.nikky.curseproxy.model.graphql

import moe.nikky.curseproxy.model.CurseAddon
import moe.nikky.curseproxy.model.Section
import java.time.LocalDate
import java.time.ZoneId

data class Addon(
    val id: Int,
    val gameID: Int,
    val name: String,
    val slug: String,
    val primaryAuthorName: String?,
    val primaryCategoryName: String?,
    val sectionName: Section,
    val dateModified: LocalDate,
    val dateCreated: LocalDate,
    val dateReleased: LocalDate,
    val categoryList: String
) {
    companion object {
        fun fromCurseAddon(curseAddon: CurseAddon) = Addon(
            id = curseAddon.id,
            gameID = curseAddon.gameId,
            name = curseAddon.name,
            slug = curseAddon.slug,
            primaryAuthorName = curseAddon.primaryAuthorName,
            primaryCategoryName = curseAddon.primaryCategoryName,
            sectionName = Section.fromCategory(curseAddon.categorySection)!!,
            dateModified = curseAddon.dateModified.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            dateCreated = curseAddon.dateCreated.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            dateReleased = curseAddon.dateReleased.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            categoryList = curseAddon.categoryList
        )
    }
}