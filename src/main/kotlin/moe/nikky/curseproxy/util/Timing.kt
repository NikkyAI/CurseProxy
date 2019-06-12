package moe.nikky.curseproxy.util

public inline fun <R> measureTimeMillis1(block: () -> R): Pair<R, Long> {
    val start = System.currentTimeMillis()
    val result = block()
    return result to (System.currentTimeMillis() - start)
}
