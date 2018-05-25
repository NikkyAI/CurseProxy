package moe.nikky.curseproxy.model

import com.fasterxml.jackson.annotation.JsonAlias


data class AddOnModule(
        @JsonAlias("fimgerprint") val fingerprint: Long,
        val foldername: String
)
