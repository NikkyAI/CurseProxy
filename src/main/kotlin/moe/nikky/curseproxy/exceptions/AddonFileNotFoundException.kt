package moe.nikky.curseproxy.exceptions

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by nikky on 01/05/18.
 * @author Nikky
 * @version 1.0
 */
open class AddonFileNotFoundException(
        @Expose @SerializedName("unknown_id") val unknown_id: Int,
        @Expose @SerializedName("unknown_file_id") val unknown_file_id: Int
) : MessageException("AddonFile not found")