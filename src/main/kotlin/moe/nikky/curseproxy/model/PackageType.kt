package moe.nikky.curseproxy.model

import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor

enum class PackageType {
    FOLDER,
    CTOP,
    SINGLEFILE,
    CMOD2,
    MODPACK,
    MOD,
    ANY;

    @Serializer(forClass = PackageType::class)
    companion object: KSerializer<PackageType> {
        override val descriptor = PrimitiveSerialDescriptor("PackageType", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): PackageType {
            return values()[decoder.decodeInt()-1]
        }

        override fun serialize(encoder: Encoder, obj: PackageType) {
            encoder.encodeInt(obj.ordinal + 1)
        }
    }
}