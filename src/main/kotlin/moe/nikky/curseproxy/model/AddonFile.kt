package moe.nikky.curseproxy.model

import java.util.*

data class AddonFile(
        val id: Int,
        val fileName: String,
        val fileNameOnDisk: String,
        val fileDate: Date,
        var releaseType: FileType,
        val fileStatus: FileStatus,
        val downloadURL: String,
        val isAlternate: Boolean,
        val alternateFileId: Int,
        val dependencies: List<AddOnFileDependency>?,
        val isAvailable: Boolean,
        var modules: List<AddOnModule>?,
        val packageFingerprint: Long,
        val gameVersion: List<String>
)