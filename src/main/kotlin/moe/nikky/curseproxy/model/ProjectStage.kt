package moe.nikky.curseproxy.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import kotlin.jvm.JvmStatic;

enum class ProjectStage {
    ALPHA,
    BETA,
    DELETED,
    INACTIVE,
    MATURE,
    PLANNING,
    RELEASE,
    ABANDONED;

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromString(key: String?): ProjectStage? {
            return if (key == null)
                null
            else {
                val index = key.toIntOrNull() ?: return valueOf(key.toUpperCase())
                return values()[index - 1]
            }
        }
    }
}