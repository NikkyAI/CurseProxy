package moe.nikky.curseproxy.model

import kotlinx.serialization.Serializable

@Serializable
data class Author(
        val name: String,
        val url: String
)