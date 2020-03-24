package moe.nikky.curseproxy.model

import kotlinx.serialization.Serializable
import moe.nikky.curseproxy.serialization.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class AddonFile(
        val id: Int,
        val fileName: String,
        @Serializable(with = LocalDateTimeSerializer::class)
        val fileDate: LocalDateTime,
        @Serializable(with = FileType.Companion::class)
        var releaseType: FileType,
        @Serializable(with = FileStatus.Companion::class)
        val fileStatus: FileStatus,
        val downloadUrl: String,
        val isAlternate: Boolean,
        val alternateFileId: Int,
        val dependencies: List<AddOnFileDependency>,
        val isAvailable: Boolean,
        var modules: List<AddOnModule>,
        val packageFingerprint: Long,
        val gameVersion: List<String>,
        val installMetadata: String?,
        val serverPackFileId: Int?,
        val fileLength: Long
)