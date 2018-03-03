package moe.nikky.cursemeta.exceptions

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by nikky on 03/03/18.
 * @author Nikky
 * @version 1.0
 */
open class MessageException(
        @Expose @SerializedName("error") val error: String
) : Exception()