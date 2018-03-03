package moe.nikky.curseproxy.exceptions

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by nikky on 03/03/18.
 * @author Nikky
 * @version 1.0
 */
data class AddOnNotFoundException(
        @Expose @SerializedName("addonID") val addonID: Int
) : MessageException("AddOn Not found")