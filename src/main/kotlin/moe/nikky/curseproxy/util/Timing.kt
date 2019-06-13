package moe.nikky.curseproxy.util

import org.slf4j.Logger
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@UseExperimental(ExperimentalContracts::class)
public inline fun <R> measureTimeMillis1(block: () -> R): Pair<R, Long> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    val start = System.currentTimeMillis()
    val result = block()
    return result to (System.currentTimeMillis() - start)
}

@UseExperimental(ExperimentalContracts::class)
inline fun <R> measureMillisAndReport(logger: Logger, operationName: String, block: () -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    val start = System.currentTimeMillis()
    val result = block()
    val resultTime = System.currentTimeMillis() - start
    logger.info("operation: '$operationName' took $resultTime ms")
    return result
}