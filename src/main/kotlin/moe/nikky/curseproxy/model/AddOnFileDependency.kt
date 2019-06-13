package moe.nikky.curseproxy.model

import kotlinx.serialization.Serializable

@Serializable
data class AddOnFileDependency(
        val addonId: Int,
        @Serializable(with = DependencyType.Companion::class)
        val type: DependencyType
)