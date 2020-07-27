package moe.nikky

import com.fasterxml.jackson.databind.ObjectMapper
import moe.nikky.ExtensioHelper.mapper
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*

/**
 * Created by nikky on 27/02/18.
 * @author Nikky
 * @version 1.0
 */
object ExtensioHelper : KoinComponent {
    private val mapper: ObjectMapper by inject()

    fun json(thing: Any?): String = mapper.writeValueAsString(thing)
}

val Any?.json: String
    get() = ExtensioHelper.json(this)

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
