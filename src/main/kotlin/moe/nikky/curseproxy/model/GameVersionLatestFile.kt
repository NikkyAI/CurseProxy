package moe.nikky.curseproxy.model

data class GameVersionLatestFile(
        val gameVersion: String,
        val projectFileID: Int,
        val projectFileName: String,
        val fileType: FileType
)
