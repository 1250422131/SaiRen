package com.imcys.sairen.core.network.model

/** 东方财富行情接口支持的交易所市场。 */
enum class StockMarket(
    val displayName: String,
) {
    SHANGHAI("沪市"),
    SHENZHEN("深市"),
    BEIJING("北市"),
    HONG_KONG("港交所"),
    NASDAQ("纳斯达克"),
    NEW_YORK("纽约证券交易所"),
    AMERICAN("美国证券交易所"),
    ;

    fun category(stockCode: String): StockCategory = when (this) {
        SHANGHAI -> if (stockCode.startsWith("900")) StockCategory.B_SHARE else StockCategory.A_SHARE
        SHENZHEN -> if (stockCode.startsWith("2")) StockCategory.B_SHARE else StockCategory.A_SHARE
        BEIJING -> StockCategory.BEIJING_STOCK_EXCHANGE
        HONG_KONG -> StockCategory.HONG_KONG
        NASDAQ, NEW_YORK, AMERICAN -> StockCategory.US_STOCK
    }

    companion object {
        fun fromEastMoneyMarketCode(
            marketCode: String,
            stockCode: String,
        ): StockMarket? = when (marketCode) {
            "0" -> fromShenzhenOrBeijingStockCode(stockCode)
            "1" -> SHANGHAI
            "105" -> NASDAQ
            "106" -> NEW_YORK
            "107" -> AMERICAN
            "116", "128" -> HONG_KONG
            else -> null
        }

        private fun fromShenzhenOrBeijingStockCode(stockCode: String): StockMarket? = when {
            stockCode.startsWith("4") ||
                stockCode.startsWith("8") ||
                stockCode.startsWith("92") -> BEIJING

            stockCode.startsWith("0") ||
                stockCode.startsWith("2") ||
                stockCode.startsWith("3") -> SHENZHEN

            else -> null
        }
    }
}

/** 股票所属的业务市场分类。 */
enum class StockCategory(
    val displayName: String,
) {
    A_SHARE("A股"),
    B_SHARE("B股"),
    BEIJING_STOCK_EXCHANGE("北交所"),
    HONG_KONG("港股"),
    US_STOCK("美股"),
}

/** 将列表接口返回的市场号和证券代码转换为详情接口所需的 `secid`。 */
fun Stock.toEastMoneySecId(): String = "$eastMoneyMarketCode.$code"
fun StockDetail.toEastMoneySecId(): String = "$eastMoneyMarketCode.$code"

/**
 * 转换为新浪行情接口的 `symbol`（如 "sh600519"/"sz000001"/"bj430047"）。
 * 当前股票列表仅拉取 A 股，港股按新浪格式加 hk 前缀兜底；美股新浪格式特殊，原样返回代码。
 */
fun Stock.toSinaSymbol(): String = when (market) {
    StockMarket.SHANGHAI -> "sh$code"
    StockMarket.SHENZHEN -> "sz$code"
    StockMarket.BEIJING -> "bj$code"
    StockMarket.HONG_KONG -> "hk$code"
    null, StockMarket.NASDAQ, StockMarket.NEW_YORK, StockMarket.AMERICAN -> code
}
