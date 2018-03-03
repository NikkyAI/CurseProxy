package moe.nikky.cursemeta.exceptions

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by nikky on 03/03/18.
 * @author Nikky
 * @version 1.0
 */
data class MissingParameterException(
        @Expose @SerializedName("missing") val missing: String
) : MessageException("Parameter not found") {

}