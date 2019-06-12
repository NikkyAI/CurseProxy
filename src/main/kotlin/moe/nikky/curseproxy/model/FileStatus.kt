package moe.nikky.curseproxy.model

import com.fasterxml.jackson.annotation.JsonCreator
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.IntDescriptor
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

    @Serializer(forClass = FileStatus::class)
    companion object: KSerializer<FileStatus> {
        override val descriptor: SerialDescriptor = IntDescriptor

        override fun deserialize(decoder: Decoder): FileStatus {
            return values()[decoder.decodeInt()-1]
        }

        override fun serialize(encoder: Encoder, obj: FileStatus) {
            encoder.encodeInt(obj.ordinal + 1)
        }
    }
}