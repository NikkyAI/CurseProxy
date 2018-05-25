package voodoo.curse

import com.fasterxml.jackson.annotation.JsonCreator


/**
 * Created by nikky on 30/01/18.
 * @author Nikky
 * @version 1.0
 */

data class AddOn(
        val attachments: List<Attachment>? = emptyList(),
        val authors: List<Author> = emptyList(),
        val avatarUrl: String? = "",
        val categories: List<Category> = emptyList(),
        val categorySection: CategorySection,
        val commentCount: Int,
        val defaultFileId: Int,
        val downloadCount: Float,
        val gameId: Int,
        val gamePopularityRank: Int,
        val gameVersionLatestFiles: List<GameVersionLatestFile>,
        val iconId: Int,
        val id: Int,
        val installCount: Int,
        val isFeatured: Int,
        val latestFiles: List<AddOnFile>,
        val likes: Int,
        val name: String,
        val packageType: PackageType,
        val popularityScore: Float,
        val primaryAuthorName: String?,
        val primaryCategoryAvatarUrl: String?,
        val primaryCategoryId: Int?,
        val primaryCategoryName: String?,
        val rating: Int,
        val stage: ProjectStage,
        val status: ProjectStatus,
        val summary: String,
        val webSiteURL: String,
        val donationUrl: String?,
        val externalUrl: String?
)

data class AddOnFile(
        val id: Int,
        val alternateFileId: Int,
        val dependencies: List<AddOnFileDependency>?,
        val downloadURL: String,
        val fileDate: String,
        val fileName: String,
        val fileNameOnDisk: String,
        val fileStatus: FileStatus,
        val gameVersion: List<String>,
        val isAlternate: Boolean,
        val isAvailable: Boolean,
        val packageFingerprint: Long,
        var releaseType: FileType,
        var modules: List<AddOnModule>?
)

data class GameVersionLatestFile(
        val fileType: FileType,
        val gameVesion: String,
        val projectFileID: Int,
        val projectFileName: String
)

data class AddOnModule(
        val fingerprint: Long,
        val foldername: String
)

data class AddOnFileDependency(
        val addOnId: Int,
        val type: DependencyType
)

data class CategorySection(
        val gameID: Int,
        val id: Int,
        val extraIncludePattern: String? = "",
        val initialInclusionPattern: String? = ".",
        val name: String,
        val packageType: PackageType,
        val path: String
)

data class Category(
        val id: Int,
        val name: String,
        val url: String
)

data class Author(
        val name: String,
        val url: String
)

data class Attachment(
        val description: String?,
        val isDefault: Boolean,
        val thumbnailUrl: String,
        val title: String,
        val url: String
)

data class CurseManifest(
        val name: String,
        val version: String,
        val author: String,
        val minecraft: CurseMinecraft = CurseMinecraft(),
        val manifestType: String,
        val manifestVersion: Int = 1,
        val files: List<CurseFile> = emptyList(),
        val overrides: String = "overrides",
        val projectID: Int = -1
)

data class CurseMinecraft(
        val version: String = "",
        val modLoaders: List<CurseModLoader> = emptyList()
)

data class CurseModLoader(
        val id: String,
        val primary: Boolean
)

data class CurseFile(
        val projectID: Int,
        val fileID: Int,
        val required: Boolean
)

data class CurseFeed(
        val timestamp: Long,
        val data: List<AddOn> = emptyList()
)

enum class FileStatus {
    NORMAL,
    SEMINORMAL,
    REPORTED,
    MALFORMED,
    LOCKED,
    INVALIDLAYOUT,
    HIDDEN,
    NEEDSAPPROVAL,
    DELETED,
    UNDERREVIEW,
    MALWAREDETECTED,
    WAITINGONPROJECT,
    CLIENTONLY;

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromString(key: String?): FileStatus? {
            return if (key == null)
                null
            else {
                val index = key.toIntOrNull() ?: return valueOf(key.toUpperCase())
                return values()[index - 1]
            }
        }
    }
}

enum class FileType {
    RELEASE,
    BETA,
    ALPHA;

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromString(key: String?): FileType? {
            return if (key == null)
                null
            else {
                val index = key.toIntOrNull() ?: return valueOf(key.toUpperCase())
                return values()[index - 1]
            }
        }
    }
}

enum class DependencyType {
    REQUIRED,
    OPTIONAL,
    EMBEDDED;

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromString(key: String?): DependencyType? {
            return if (key == null)
                null
            else {
                val index = key.toIntOrNull() ?: return valueOf(key.toUpperCase())
                return values()[index - 1]
            }
        }
    }
}

enum class PackageType {
    FOLDER,
    CTOP,
    SINGLEFILE,
    CMOD2,
    MODPACK,
    MOD,
    ANY;

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromString(key: String?): PackageType? {
            return if (key == null)
                null
            else {
                val index = key.toIntOrNull() ?: return valueOf(key.toUpperCase())
                return values()[index - 1]
            }
        }
    }
}

enum class ProjectStage {
    ALPHA,
    BETA,
    DELETED,
    INACTIVE,
    MATURE,
    PLANNING,
    RELEASE,
    ABANDONED;

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromString(key: String?): ProjectStage? {
            return if (key == null)
                null
            else {
                val index = key.toIntOrNull() ?: return valueOf(key.toUpperCase())
                return values()[index - 1]
            }
        }
    }
}

enum class ProjectStatus {
    NORMAL,
    HIDDEN,
    DELETED;

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromString(key: String?): ProjectStatus? {
            return if (key == null)
                null
            else {
                val index = key.toIntOrNull() ?: return valueOf(key.toUpperCase())
                return values()[index - 1]
            }
        }
    }
}