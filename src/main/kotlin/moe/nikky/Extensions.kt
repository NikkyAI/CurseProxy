package moe.nikky

import com.google.gson.GsonBuilder
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Created by nikky on 27/02/18.
 * @author Nikky
 * @version 1.0
 */

val Throwable.stackTraceString: String
    get() {
        val sw = StringWriter()
        this.printStackTrace(PrintWriter(sw))
        return sw.toString()
    }