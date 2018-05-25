package moe.nikky.curseproxy.model

data class CategorySection(
        val id: Int,
        val gameID: Int,
        val name: String,
        val packageType: PackageType,
        val path: String,
        val initialInclusionPattern: String? = ".",
        val extraIncludePattern: String? = ""
)