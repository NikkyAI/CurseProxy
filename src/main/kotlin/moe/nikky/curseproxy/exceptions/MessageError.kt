package moe.nikky.curseproxy.exceptions

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by nikky on 03/03/18.
 * @author Nikky
 * @version 1.0
 */
@Serializable
sealed class MessageError {
    abstract val message: String

    @Serializable
    @SerialName("project_not_found")
    open class AddonNotFound(
        @Required override val message: String = "CurseAddon not found",
        val unknown_id: Int,
    ) : MessageError()

    @Serializable
    @SerialName("file_not_found")
    class AddonFileNotFound(
        @Required override val message: String = "AddonFile not found",
        val unknownId: Int,
        val unknownFileId: Int,
    ) : MessageError()

    @Serializable
    @SerialName("missing_parameter")
    data class MissingParameter(
        @Required override val message: String = "Parameter not found",
        val missing: String
    ) : MessageError()
}