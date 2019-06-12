package moe.nikky.curseproxy.curse.util

import kotlinx.coroutines.CoroutineScope

suspend inline fun measureTimeMillis(block: () -> Unit): Long {
    val start = System.currentTimeMillis()
    block()
    return System.currentTimeMillis() - start
}
