package moe.nikky.curseproxy.exceptions

import moe.nikky.stackTraceString

/**
 * Created by nikky on 27/02/18.
 * @author Nikky
 * @version 1.0
 */
data class StackTraceMessage(private val e: Throwable) {
    val exception: String = e.javaClass.name
    val message: String = e.message ?: ""
    val stacktrace: List<String> = e.stackTraceString.split('\n')
}