package moe.nikky.curseproxy.model

import kotlinx.serialization.Serializable
import voodoo.data.curse.ProjectID

@Serializable
data class Attachment(
        val id: Int,
        val projectId: ProjectID,
        val description: String?,
        val isDefault: Boolean,
        val thumbnailUrl: String,
        val title: String,
        val url: String,
        val status: Int
)