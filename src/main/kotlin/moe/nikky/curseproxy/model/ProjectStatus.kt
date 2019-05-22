package moe.nikky.curseproxy.model

import com.fasterxml.jackson.annotation.JsonCreator
import kotlin.jvm.JvmStatic

enum class ProjectStatus {
    // Token: 0x04000074 RID: 116
    New,
    // Token: 0x04000075 RID: 117
    ChangesRequired,
    // Token: 0x04000076 RID: 118
    UnderSoftReview,
    // Token: 0x04000077 RID: 119
    Approved,
    // Token: 0x04000078 RID: 120
    Rejected,
    // Token: 0x04000079 RID: 121
    ChangesMade,
    // Token: 0x0400007A RID: 122
    Inactive,
    // Token: 0x0400007B RID: 123
    Abandoned,
    // Token: 0x0400007C RID: 124
    Deleted,
    // Token: 0x0400007D RID: 125
    UnderReview;

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromString(key: String?): ProjectStatus? {
            return if (key == null)
                null
            else {
                val index = key.toIntOrNull() ?: return valueOf(key.toUpperCase())
                return values()[index - 1]
            }
        }
    }
}