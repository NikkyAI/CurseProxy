package moe.nikky.curseproxy.model.manifest

data class CurseMinecraft(
        val version: String = "",
        val modLoaders: List<CurseModLoader> = emptyList()
)