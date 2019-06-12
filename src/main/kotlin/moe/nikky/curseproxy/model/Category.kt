package moe.nikky.curseproxy.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
        val categoryId: Int,
        val name: String,
        val url: String,
        val avatarUrl: String
)