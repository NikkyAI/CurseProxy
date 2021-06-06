package moe.nikky.curseproxy.exceptions

/**
 * Created by nikky on 03/03/18.
 * @author Nikky
 * @version 1.0
 */
sealed class MessageException(
    val error: String,
) : Exception() {
    abstract fun error(): MessageError

    data class AddonNotFound(
        val unknown_id: Int,
    ) : MessageException("CurseAddon not found") {
        override fun error() = MessageError.AddonNotFound(
            unknown_id = unknown_id
        )
    }

    data class AddonFileNotFound(
        val unknown_id: Int,
        val unknown_file_id: Int,
    ) : MessageException("AddonFile not found") {
        override fun error(): MessageError {
            return MessageError.AddonFileNotFound(
                unknownId = unknown_id,
                unknownFileId = unknown_file_id
            )
        }
    }

    data class MissingParameter(
        val missing: String,
    ) : MessageException("Parameter not found") {
        override fun error(): MessageError {
            return MessageError.MissingParameter(missing = missing)
        }
    }
}