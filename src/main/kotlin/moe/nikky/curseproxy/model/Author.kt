package moe.nikky.curseproxy.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Author(
        val name: String,
        val url: String
)