package moe.nikky.curseproxy.exceptions

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import moe.nikky.stackTraceString

/**
 * Created by nikky on 27/02/18.
 * @author Nikky
 * @version 1.0
 */
data class StackTraceMessage(private val e: Throwable) {
    @Expose @SerializedName("exception")  val exception: String = e.javaClass.name
    @Expose @SerializedName("message")  val message: String = e.message ?: ""
    @Expose @SerializedName("stacktrace")  val stacktrace: List<String> = e.stackTraceString.split('\n')
}