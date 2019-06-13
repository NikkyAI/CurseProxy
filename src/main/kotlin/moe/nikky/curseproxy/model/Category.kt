package moe.nikky.curseproxy.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
        val categoryId: Int,
        @SerialName("name") val categoryName: String,
        val url: String,
        val avatarUrl: String
)