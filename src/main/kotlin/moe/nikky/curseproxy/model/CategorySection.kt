package moe.nikky.curseproxy.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class CategorySection(
        val id: Int,
        val gameID: Int,
        val name: String,
        val packageType: PackageType,
        val path: String,
        val initialInclusionPattern: String? = ".",
        val extraIncludePattern: String? = ""
)