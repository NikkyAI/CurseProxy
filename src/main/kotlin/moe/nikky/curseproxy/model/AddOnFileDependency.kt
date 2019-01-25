package moe.nikky.curseproxy.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)

data class AddOnFileDependency(
        val addOnId: Int,
        val type: DependencyType
)