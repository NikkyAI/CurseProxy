package moe.nikky.curseproxy.model.feed

import moe.nikky.curseproxy.model.Addon

data class CurseFeed(
        val timestamp: Long,
        val data: List<Addon> = emptyList()
)