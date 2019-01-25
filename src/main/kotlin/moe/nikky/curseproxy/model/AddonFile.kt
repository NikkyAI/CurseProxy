package moe.nikky.curseproxy.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
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
        val gameVersion: List<String>,
        val installMetadata: String?,
        val fileLength: Long
)