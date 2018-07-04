package moe.nikky.curseproxy.model.graphql

import moe.nikky.curseproxy.model.Section
import java.time.LocalDate

data class Addon(
        val id: Int,
        val name: String,
        val slug: String,
        val primaryAuthorName: String?,
        val primaryCategoryName: String?,
        val sectionName: Section,
        val dateModified: LocalDate,
        val dateCreated: LocalDate,
        val dateReleased: LocalDate,
        val categoryList: String
)