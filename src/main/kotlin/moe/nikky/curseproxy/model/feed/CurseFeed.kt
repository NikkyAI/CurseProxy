package moe.nikky.curseproxy.model.feed

import moe.nikky.curseproxy.model.CurseAddon

data class CurseFeed(
        val timestamp: Long,
        val data: List<CurseAddon> = emptyList()
)