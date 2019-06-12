package moe.nikky.curseproxy.model

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.IntDescriptor

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

    @Serializer(forClass = ProjectStatus::class)
    companion object : KSerializer<ProjectStatus> {
        override val descriptor: SerialDescriptor = IntDescriptor

        override fun deserialize(decoder: Decoder): ProjectStatus {
            return values()[decoder.decodeInt() - 1]
        }

        override fun serialize(encoder: Encoder, obj: ProjectStatus) {
            encoder.encodeInt(obj.ordinal + 1)
        }
    }
}