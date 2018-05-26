package moe.nikky.curseproxy.model

import java.time.LocalDate

data class SparseAddon(
        val id: Int? = null,
        val addonId: Int,
        val name: String,
        val primaryAuthorName: String?,
        val primaryCategoryName: String?,
        val sectionName: String,
        val dateModified: LocalDate,
        val dateCreated: LocalDate,
        val dateReleased: LocalDate
)