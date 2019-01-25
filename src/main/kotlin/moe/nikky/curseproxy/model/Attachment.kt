package moe.nikky.curseproxy.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Attachment(
        val id: Int,
        val projectId: Int,
        val description: String?,
        val isDefault: Boolean,
        val thumbnailUrl: String,
        val title: String,
        val url: String
)