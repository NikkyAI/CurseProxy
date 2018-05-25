package moe.nikky.curseproxy.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import kotlin.jvm.JvmStatic;

enum class FileStatus {
    NORMAL,
    SEMINORMAL,
    REPORTED,
    MALFORMED,
    LOCKED,
    INVALIDLAYOUT,
    HIDDEN,
    NEEDSAPPROVAL,
    DELETED,
    UNDERREVIEW,
    MALWAREDETECTED,
    WAITINGONPROJECT,
    CLIENTONLY;

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromString(key: String?): FileStatus? {
            return if (key == null)
                null
            else {
                val index = key.toIntOrNull() ?: return valueOf(key.toUpperCase())
                return values()[index - 1]
            }
        }
    }
}