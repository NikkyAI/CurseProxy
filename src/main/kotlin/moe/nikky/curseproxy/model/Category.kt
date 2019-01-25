package moe.nikky.curseproxy.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Category(
        val id: Int,
        val name: String,
        val url: String,
        val avatarUrl: String
)