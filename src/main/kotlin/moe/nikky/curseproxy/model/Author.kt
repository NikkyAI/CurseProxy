package moe.nikky.curseproxy.model

import kotlinx.serialization.Serializable
import voodoo.data.curse.ProjectID

@Serializable
data class Author(
        val name: String,
        val url: String,
        val projectId: ProjectID,
        val id: Int,
        val projectTitleId: Int?,
        val projectTitleTitle: String?,
        val userId: Int,
        val twitchId: Int?
)