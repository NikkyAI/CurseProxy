package moe.nikky.curseproxy.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategorySection(
        @SerialName("id") val categorySectionId: Int,
        @SerialName("gameId") val categorySectionGameId: Int,
        @SerialName("name") val categorySectionName: String,
        @Serializable(with = PackageType.Companion::class)
        val packageType: PackageType,
        val path: String,
        val initialInclusionPattern: String? = ".",
        val extraIncludePattern: String? = ""
)