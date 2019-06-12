package moe.nikky.curseproxy.model

import kotlinx.serialization.Serializable

@Serializable
data class GameVersionLatestFile(
        val gameVersion: String,
        val projectFileId: Int,
        val projectFileName: String,
        @Serializable(with = FileType.Companion::class)
        val fileType: FileType
)

