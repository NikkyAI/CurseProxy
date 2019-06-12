package moe.nikky.curseproxy.model

import kotlinx.serialization.Serializable

@Serializable
data class Attachment(
        val id: Int,
        val projectId: Int,
        val description: String?,
        val isDefault: Boolean,
        val thumbnailUrl: String,
        val title: String,
        val url: String,
        val status: Int
)