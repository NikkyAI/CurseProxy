package moe.nikky.curseproxy.model

import kotlinx.serialization.Serializable
import moe.nikky.curseproxy.serialization.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Addon(
    val id: Int,
    val name: String,
    val authors: List<Author> = emptyList(),
    val attachments: List<Attachment> = emptyList(),
    val websiteUrl: String,
    val gameId: Int,
    val summary: String,
    val defaultFileId: Int,
    val downloadCount: Float,
    val latestFiles: List<AddonFile>,
    val categories: List<Category> = emptyList(),
    @Serializable(with = ProjectStatus.Companion::class)
    val status: ProjectStatus,
    val categorySection: CategorySection,
    val slug: String,
    val gameVersionLatestFiles: List<GameVersionLatestFile>,
    val popularityScore: Float,
    val gamePopularityRank: Int,
    val gameName: String,
    val portalName: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dateModified: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dateCreated: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dateReleased: LocalDateTime,
    val isAvailable: Boolean,
    val primaryLanguage: String,
    val isFeatured: Boolean
)