package com.imcys.sairen.core.network.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** 东方财富分时接口 `data` 字段的数据模型。 */
@Serializable
data class StockTrend(
    val code: String,
    val market: Int,
    val name: String,
    val decimal: Int,
    val preClose: Double?,
    val trends: List<StockTrendPoint>,
) : SRModel

/**
 * 分时折线图的单个点位。
 *
 * 东方财富将每个点位以 CSV 字符串返回，字段顺序为：
 * 时间、开盘价、收盘价、最高价、最低价、成交量、成交额、均价。
 */
@Serializable(with = StockTrendPointSerializer::class)
data class StockTrendPoint(
    val time: String,
    val openingPrice: Double?,
    val closingPrice: Double?,
    val highestPrice: Double?,
    val lowestPrice: Double?,
    val volume: Long?,
    val amount: Double?,
    val averagePrice: Double?,
)

private object StockTrendPointSerializer : KSerializer<StockTrendPoint> {

    override val descriptor = PrimitiveSerialDescriptor("StockTrendPoint", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): StockTrendPoint {
        val fields = decoder.decodeString().split(',')
        require(fields.size == 8) { "无效的分时点位数据: ${fields.joinToString(",")}" }
        return StockTrendPoint(
            time = fields[0],
            openingPrice = fields[1].toDoubleOrNull(),
            closingPrice = fields[2].toDoubleOrNull(),
            highestPrice = fields[3].toDoubleOrNull(),
            lowestPrice = fields[4].toDoubleOrNull(),
            volume = fields[5].toLongOrNull(),
            amount = fields[6].toDoubleOrNull(),
            averagePrice = fields[7].toDoubleOrNull(),
        )
    }

    override fun serialize(encoder: Encoder, value: StockTrendPoint) {
        encoder.encodeString(
            listOf(
                value.time,
                value.openingPrice,
                value.closingPrice,
                value.highestPrice,
                value.lowestPrice,
                value.volume,
                value.amount,
                value.averagePrice,
            ).joinToString(",") { it?.toString().orEmpty() }
        )
    }
}
