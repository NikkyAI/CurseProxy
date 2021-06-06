package moe.nikky

import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*

/**
 * Created by nikky on 27/02/18.
 * @author Nikky
 * @version 1.0
 */

fun File.encodeBase64(): String{
    val bytes = this.readBytes()
    return Base64.getEncoder().encodeToString(bytes)
}

val Throwable.stackTraceString: String
    get() {
        val sw = StringWriter()
        this.printStackTrace(PrintWriter(sw))
        return sw.toString()
    }
