package moe.nikky.curseproxy.model

import com.fasterxml.jackson.annotation.JsonCreator
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.IntDescriptor

enum class DependencyType {
    // Token: 0x04000055 RID: 85
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


    @Serializer(forClass = DependencyType::class)
    companion object: KSerializer<DependencyType> {
        override val descriptor: SerialDescriptor = IntDescriptor

        override fun deserialize(decoder: Decoder): DependencyType {
            return DependencyType.values()[decoder.decodeInt()-1]
        }

        override fun serialize(encoder: Encoder, obj: DependencyType) {
            encoder.encodeInt(obj.ordinal + 1)
        }
    }
}