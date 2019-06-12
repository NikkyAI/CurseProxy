package moe.nikky.curseproxy.model

import kotlinx.serialization.Serializable

@Serializable
data class Author(
        val name: String,
        val url: String,
        val projectId: Int,
        val id: Int,
        val projectTitleId: Int?,
        val projectTitleTitle: String?,
        val userId: Int,
        val twitchId: Int?
)