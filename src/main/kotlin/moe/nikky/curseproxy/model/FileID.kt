package voodoo.data.curse

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// TODO: inline
@Serializable(with = FileID.Companion::class)
data class FileID(val value: Int) {
    override fun toString(): String {
        return value.toString()
    }

    val valid: Boolean
        get() = value > 0

    @Serializer(forClass = FileID::class)
    companion object : KSerializer<FileID> {
        override val descriptor = PrimitiveSerialDescriptor("FileID", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): FileID {
            return FileID(decoder.decodeInt())
        }

        override fun serialize(encoder: Encoder, value: FileID) {
            encoder.encodeInt(value.value)
        }

        val INVALID = FileID(-1)
    }
}