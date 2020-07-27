package moe.nikky.curseproxy.util

import mu.KotlinLogging
import org.slf4j.Logger
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun <R> measureTimeMillisInline(block: () -> R): Pair<R, Long> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    val start = System.currentTimeMillis()
    val result = block()
    return result to (System.currentTimeMillis() - start)
}

@OptIn(ExperimentalContracts::class)
inline fun <R> measureMillisAndReport(
        operationName: String,
        report: (String) -> Unit,
        block: () -> R
): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    val start = System.currentTimeMillis()
    val result = block()
    val resultTime = System.currentTimeMillis() - start
    report("operation: '$operationName' took $resultTime ms")
    return result
}