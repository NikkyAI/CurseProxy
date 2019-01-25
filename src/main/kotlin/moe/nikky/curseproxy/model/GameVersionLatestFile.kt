package moe.nikky.curseproxy.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class GameVersionLatestFile(
        val gameVersion: String,
        val projectFileID: Int,
        val projectFileName: String,
        val fileType: FileType
)
