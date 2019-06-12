package moe.nikky.curseproxy.model

import kotlinx.serialization.Serializable
import voodoo.data.curse.ProjectID

@Serializable
data class AddOnFileDependency(
        val addonId: ProjectID,
        @Serializable(with = DependencyType.Companion::class)
        val type: DependencyType
)