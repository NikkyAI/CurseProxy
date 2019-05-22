package moe.nikky.curseproxy.model

import com.fasterxml.jackson.annotation.JsonCreator
import kotlin.jvm.JvmStatic

enum class FileStatus {
    // Token: 0x04000041 RID: 65
    Processing,
    // Token: 0x04000042 RID: 66
    ChangesRequired,
    // Token: 0x04000043 RID: 67
    UnderReview,
    // Token: 0x04000044 RID: 68
    Approved,
    // Token: 0x04000045 RID: 69
    Rejected,
    // Token: 0x04000046 RID: 70
    MalwareDetected,
    // Token: 0x04000047 RID: 71
    Deleted,
    // Token: 0x04000048 RID: 72
    Archived,
    // Token: 0x04000049 RID: 73
    Testing,
    // Token: 0x0400004A RID: 74
    Released,
    // Token: 0x0400004B RID: 75
    ReadyForReview,
    // Token: 0x0400004C RID: 76
    Deprecated,
    // Token: 0x0400004D RID: 77
    Baking,
    // Token: 0x0400004E RID: 78
    AwaitingPublishing,
    // Token: 0x0400004F RID: 79
    FailedPublishing;

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