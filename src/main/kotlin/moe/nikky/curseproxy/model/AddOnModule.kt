package moe.nikky.curseproxy.model

import kotlinx.serialization.Serializable

@Serializable
data class AddOnModule(
        val fingerprint: Long,
        val foldername: String
)
