package moe.nikky.curseproxy.exceptions

import kotlinx.serialization.Serializable
import moe.nikky.stackTraceString

/**
 * Created by nikky on 27/02/18.
 * @author Nikky
 * @version 1.0
 */
@Serializable
data class StackTraceMessage(
    val exception: String,
    val message: String,
    val stacktrace: List<String>,
) {
    constructor(e: Throwable) : this(
        exception = e.javaClass.name,
        message = e.message ?: "",
        stacktrace = e.stackTraceString.lines().map { it.replace("\t", "  ") },
    )
}