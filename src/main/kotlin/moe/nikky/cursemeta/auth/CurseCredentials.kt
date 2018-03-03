package moe.nikky.cursemeta.auth

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import moe.nikky.cursemeta.LOG
import java.io.File

data class CurseCredentials(
        @Expose @SerializedName("username") val username: String,
        @Expose @SerializedName("password") val password: String
) {
    companion object {
        private val credentialsFile = File("auth.json")
        private val gson = GsonBuilder().setPrettyPrinting().create()

        fun load(): CurseCredentials {
            if(!credentialsFile.exists()) {
                credentialsFile.createNewFile()
                credentialsFile.writeText(gson.toJson(CurseCredentials("user", "pass")))
                LOG.error("generated auth file templat")
                LOG.error("please fill out '$credentialsFile'")
                System.exit(-1)
            }
            println("parsing $credentialsFile")
            return gson.fromJson(credentialsFile.readText(), CurseCredentials::class.java)
        }
    }
}