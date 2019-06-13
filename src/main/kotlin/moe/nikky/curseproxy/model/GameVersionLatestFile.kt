package moe.nikky.curseproxy.model

import kotlinx.serialization.Serializable
import voodoo.data.curse.FileID

@Serializable
data class GameVersionLatestFile(
        val gameVersion: String,
        val projectFileId: FileID,
        val projectFileName: String,
        @Serializable(with = FileType.Companion::class)
        val fileType: FileType
)

