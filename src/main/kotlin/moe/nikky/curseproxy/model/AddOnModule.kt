package moe.nikky.curseproxy.model

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AddOnModule(
        @JsonAlias("fimgerprint") val fingerprint: Long,
        val foldername: String
)
