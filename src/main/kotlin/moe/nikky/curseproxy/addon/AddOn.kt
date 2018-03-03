//package moe.nikky.curseproxy.addon
//
///**
// * Created by nikky on 27/02/18.
// * @author Nikky
// * @version 1.0
// */
//data class AddOn(
//        val Attachments: List<Attachment>,
//        val Authors: List<Author>,
//        val Categories: List<Category>,
//        val CategorySection: CategorySection,
//        val CommentCount: Int,
//        val DefaultFileId: Int,
//        val DownloadCount: Double,
//        val GameId: Int,
//        val GamePopularityRank: Int,
//        val GameVersionLatestFiles: List<GameVersionLatestFile>,
//        val IconId: Int,
//        val Id: Int,
//        val InstallCount: Int,
//        val IsFeatured: Int,
//        val LatestFiles: List<AddOnFile>,
//        val Likes: Int,
//        val Name: String,
//        val PackageType: PackageType,
//        val PopularityScore: Double,
//        val PrimaryAuthorName: String,
//        val PrimaryCategoryAvatarUrl: String,
//        val PrimaryCategoryId: Int,
//        val PrimaryCategoryName: String,
//        val Rating: Int,
//        val Stage: ProjectStage,
//        val Status: ProjectStatus,
//        val Summary: String,
//        val WebSiteURL: String
//) {
//    companion object {
//        fun convert(addon: addons.curse.AddOn): AddOn {
//            val attachments = addon.attachments ?: listOf()
//            return AddOn(
//                    Attachments = attachments.map { Attachment.convert(it) },
//                    Authors = addon.authors.map { Author.convert(it) },
//                    Categories = addon.categories.map { Category.convert(it) },
//                    CategorySection = CategorySection.convert(addon.categorySection),
//                    CommentCount = addon.commentCount,
//                    DefaultFileId = addon.defaultFileId,
//                    DownloadCount = addon.downloadCount,
//                    GameId = addon.gameId,
//                    GamePopularityRank = addon.gamePopularityRank,
//                    GameVersionLatestFiles = addon.gameVersionLatestFiles.map { GameVersionLatestFile.convert(it) },
//                    IconId = addon.iconId,
//                    Id = addon.id,
//                    InstallCount = addon.installCount,
//                    IsFeatured = addon.isFeatured,
//                    LatestFiles = addon.latestFiles.map { AddOnFile.convert(it) },
//                    Likes = addon.likes,
//                    Name = addon.name,
//                    PackageType = PackageType.valueOf(addon.packageType.value),
//                    PopularityScore = addon.popularityScore,
//                    PrimaryAuthorName = addon.primaryAuthorName,
//                    PrimaryCategoryAvatarUrl = addon.primaryCategoryAvatarUrl,
//                    PrimaryCategoryId = addon.primaryCategoryId,
//                    PrimaryCategoryName = addon.primaryCategoryName,
//                    Rating = addon.rating,
//                    Stage = ProjectStage.valueOf(addon.stage.value),
//                    Status = ProjectStatus.valueOf(addon.status.value),
//                    Summary = addon.summary,
//                    WebSiteURL = addon.webSiteURL
//            )
//        }
//    }
//}
//
//data class AddOnFile(
//        val AlternateFileId: Int,
//        val Dependencies: List<AddOnFileDependency>,
//        val DownloadURL: String,
//        val FileDate: String,
//        val FileName: String,
//        val FileNameOnDisk: String,
//        val FileStatus: FileStatus,
//        val GameVersion: List<String>,
//        val Id: Int,
//        val IsAlternate: Boolean,
//        val IsAvailable: Boolean,
//        val Modules: List<Module>,
//        val PackageFingerprint: Long,
//        val ReleaseType: FileType
//) {
//    companion object {
//        fun convert(file: addons.curse.AddOnFile): AddOnFile {
//            return AddOnFile(
//                    AlternateFileId = file.alternateFileId,
//                    Dependencies = file.dependencies.map { AddOnFileDependency.convert(it) },
//                    DownloadURL = file.downloadURL,
//                    FileDate = file.fileDate.toString(),
//                    FileName = file.fileDate.toString(),
//                    FileNameOnDisk = file.fileNameOnDisk,
//                    FileStatus = FileStatus.valueOf(file.fileStatus.value),
//                    GameVersion = file.gameVersion,
//                    Id = file.id,
//                    IsAlternate = file.isAlternate,
//                    IsAvailable = file.isAvailable,
//                    Modules = file.modules.map { Module.convert(it) },
//                    PackageFingerprint = file.packageFingerprint,
//                    ReleaseType = FileType.valueOf(file.releaseType.value)
//            )
//        }
//    }
//}
//
//data class AddOnFileDependency(
//        val AddonId: Int,
//        val Type: DependencyType
//) {
//    companion object {
//        fun convert(dependency: addons.curse.AddOnFileDependency): AddOnFileDependency {
//            return AddOnFileDependency(
//                    dependency.addOnId,
//                    DependencyType.valueOf(dependency.type.value)
//            )
//        }
//    }
//}
//
//data class Module(
//        val Fingerprint: Long,
//        val Foldername: String
//) {
//    companion object {
//        fun convert(module: addons.curse.AddOnModule): Module {
//            return Module(
//                    Fingerprint = module.fingerprint,
//                    Foldername = module.foldername
//            )
//        }
//    }
//}
//
//data class Author(
//        val Name: String,
//        val Url: String
//) {
//    companion object {
//        fun convert(author: addons.curse.AddOnAuthor): Author {
//            return Author(
//                    Name = author.name,
//                    Url = author.url
//            )
//        }
//    }
//}
//
//data class CategorySection(
//        val GameID: Int,
//        val ID: Int,
//        val InitialInclusionPattern: String,
//        val Name: String,
//        val PackageType: PackageType,
//        val Path: String
//) {
//    companion object {
//        fun convert(section: addons.curse.CategorySection): CategorySection {
//            return CategorySection(
//                    GameID = section.gameID,
//                    ID = section.id,
//                    InitialInclusionPattern = section.initialInclusionPattern,
//                    Name = section.name,
//                    PackageType = PackageType.valueOf(section.packageType.value),
//                    Path = section.path
//            )
//        }
//    }
//}
//
//data class Category(
//        val Id: Int,
//        val Name: String,
//        val URL: String
//) {
//    companion object {
//        fun convert(category: addons.curse.AddOnCategory): Category {
//            return Category(
//                    Id = category.id,
//                    Name = category.name,
//                    URL = category.url
//            )
//        }
//    }
//}
//
//data class Attachment(
//        val Description: String,
//        val IsDefault: Boolean,
//        val ThumbnailUrl: String,
//        val Title: String,
//        val Url: String
//) {
//    companion object {
//        fun convert(attachment: addons.curse.AddOnAttachment): Attachment {
//            return Attachment(
//                    Description = attachment.description,
//                    IsDefault = attachment.isDefault,
//                    ThumbnailUrl = attachment.thumbnailUrl,
//                    Title = attachment.title,
//                    Url = attachment.url
//            )
//        }
//    }
//}
//
//data class GameVersionLatestFile(
//        val FileType: FileType,
//        val GameVesion: String,
//        val ProjectFileID: Int,
//        val ProjectFileName: String
//) {
//    companion object {
//        fun convert(file: org.datacontract.schemas._2004._07.curse_addons.GameVersionLatestFile): GameVersionLatestFile {
//            return GameVersionLatestFile(
//                    FileType = FileType.valueOf(file.fileType.value),
//                    GameVesion = file.gameVesion,
//                    ProjectFileID = file.projectFileID,
//                    ProjectFileName = file.projectFileName
//            )
//        }
//    }
//}
//
//enum class PackageType(val id: Int) {
//    Folder(1), Ctoc(2), SingleFile(3), Cmod2(4), ModPack(5), Mod(6)
//}
//
//enum class FileType(val id: Int) {
//    Release(1), Beta(2), Alpha(3)
//}
//
//enum class DependencyType(val id: Int) {
//    Required(1), Optional(2), Embedded(3)
//}
//
//enum class FileStatus {
//     Normal,
//     SemiNormal,
//     Reported,
//     Malformed,
//     Locked,
//     InvalidLayout,
//     Hidden,
//     NeedsApproval,
//     Deleted,
//     UnderReview,
//     MalwareDetected,
//     WaitingOnProject,
//     ClientOnly,
//}
//
//enum class ProjectStage {
//    Alpha,
//    Beta,
//    Deleted,
//    Inactive,
//    Mature,
//    Planning,
//    Release,
//    Abandoned,
//}
//
//enum class ProjectStatus(id: Int) {
//    Normal(1),
//    Hidden(2),
//    Deleted(3)
//}