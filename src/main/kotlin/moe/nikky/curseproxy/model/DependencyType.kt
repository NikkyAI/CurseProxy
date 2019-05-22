package moe.nikky.curseproxy.model

import com.fasterxml.jackson.annotation.JsonCreator

enum class DependencyType {
    EmbeddedLibrary,
    // Token: 0x04000056 RID: 86
    OptionalDependency,
    // Token: 0x04000057 RID: 87
    RequiredDependency,
    // Token: 0x04000058 RID: 88
    Tool,
    // Token: 0x04000059 RID: 89
    Incompatible,
    // Token: 0x0400005A RID: 90
    Include;

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromString(key: String?): DependencyType? {
            return if (key == null)
                null
            else {
                val index = key.toIntOrNull() ?: return valueOf(key.toUpperCase())
                return values()[index - 1]
            }
        }
    }
}