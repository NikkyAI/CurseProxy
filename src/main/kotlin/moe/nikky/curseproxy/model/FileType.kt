package moe.nikky.curseproxy.model

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.IntDescriptor

enum class FileType {
    Release,
    Beta,
    Alpha;

    @Serializer(forClass = FileType::class)
    companion object {
        override val descriptor: SerialDescriptor = IntDescriptor

        override fun deserialize(decoder: Decoder): FileType {
            return values()[decoder.decodeInt()-1]
        }

        override fun serialize(encoder: Encoder, obj: FileType) {
            encoder.encodeInt(obj.ordinal + 1)
        }
    }
}