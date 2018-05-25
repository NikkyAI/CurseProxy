package moe.nikky.curseproxy.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import kotlin.jvm.JvmStatic;

enum class PackageType {
    FOLDER,
    CTOP,
    SINGLEFILE,
    CMOD2,
    MODPACK,
    MOD,
    ANY;

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromString(key: String?): PackageType? {
            return if (key == null)
                null
            else {
                val index = key.toIntOrNull() ?: return valueOf(key.toUpperCase())
                return values()[index - 1]
            }
        }
    }
}