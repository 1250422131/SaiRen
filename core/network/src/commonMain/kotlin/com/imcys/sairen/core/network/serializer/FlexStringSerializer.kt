package com.imcys.sairen.core.network.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

object FlexStringSerializer : KSerializer<String> {

    override val descriptor =
        PrimitiveSerialDescriptor("FlexString", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        val jsonDecoder = decoder as? JsonDecoder
            ?: error("FlexStringSerializer 只能用于 kotlinx.serialization JSON 解码")
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> element.content // content 对数字字面量同样返回原始文本
            JsonNull -> ""
            else -> ""
        }
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}