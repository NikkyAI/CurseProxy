package moe.nikky.curseproxy.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

enum class FileType {
    Release,
    Beta,
    Alpha;

    @Serializer(forClass = FileType::class)
    companion object: KSerializer<FileType> {
        override val descriptor = PrimitiveSerialDescriptor("FileType", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): FileType {
            return values()[decoder.decodeInt()-1]
        }

        override fun serialize(encoder: Encoder, obj: FileType) {
            encoder.encodeInt(obj.ordinal + 1)
        }
    }
}