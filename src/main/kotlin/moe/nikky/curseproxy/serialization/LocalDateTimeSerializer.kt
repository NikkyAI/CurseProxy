package moe.nikky.curseproxy.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializer(forClass = LocalDateTime::class)
object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)
    val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss") //.SSS'Z'")

    override fun serialize(encoder: Encoder, obj: LocalDateTime) {
        encoder.encodeString(formatter.format(obj))
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.from(formatter.parse(decoder.decodeString().substringBeforeLast('Z').substringBeforeLast('.')))
    }
}