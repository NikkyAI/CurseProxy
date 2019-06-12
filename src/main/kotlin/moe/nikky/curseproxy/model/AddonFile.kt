package moe.nikky.curseproxy.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import kotlinx.serialization.Serializable
import moe.nikky.curseproxy.serialization.LocalDateTimeSerializer
import voodoo.data.curse.FileID
import java.time.LocalDateTime
import java.util.*

@Serializable
data class AddonFile(
        val id: FileID,
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
        val fileLength: Long
)