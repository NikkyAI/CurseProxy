package moe.nikky.curseproxy.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

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
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DependencyType", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): DependencyType {
            return DependencyType.values()[decoder.decodeInt()-1]
        }

        override fun serialize(encoder: Encoder, value: DependencyType) {
            encoder.encodeInt(value.ordinal + 1)
        }
    }
}