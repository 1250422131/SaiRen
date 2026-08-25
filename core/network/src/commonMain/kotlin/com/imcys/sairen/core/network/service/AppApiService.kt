package com.imcys.sairen.core.network.service

import com.imcys.sairen.core.network.FlowNetWorkResult
import com.imcys.sairen.core.network.model.SinaKLinePoint
import com.imcys.sairen.core.network.model.SinaMinlinePoint
import com.imcys.sairen.core.network.model.StockDetail
import com.imcys.sairen.core.network.model.Stock
import com.imcys.sairen.core.network.model.StockList
import com.imcys.sairen.core.network.model.toEastMoneySecId
import com.imcys.sairen.core.network.srRequest
import com.tencent.kuikly.core.pager.Pager


object AppApiService {

    /** 东财实时/延时行情（股票列表、详情等实时类接口） */
    const val BASE_URL = "https://push2delay.eastmoney.com/api"

    /** 新浪历史 K 线接口（图表数据源：五日/日/周/月K），返回顶层 JSON 数组 */
    const val SINA_KLINE_URL =
        "https://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData"

    /** 新浪当日分时接口（1 分钟线），返回顶层 JSON 数组 */
    const val SINA_MINLINE_URL =
        "https://quotes.sina.cn/cn/api/json_v2.php/CN_MinlineService.getMinlineData"



    fun getStockList(
        pager: Pager,
        pn: Int = 1,
        pz: Int = 30,
        po: Int = 1,
        np: Int = 1,
        fltt: Int = 2,
        invt: Int = 2,
        fid: String = "f12",
        fs: String = "m:0 t:6,m:0 t:80,m:1 t:2,m:1 t:23",
        fields: String = "f12,f13,f14,f2,f3,f4,f5,f6"
    ): FlowNetWorkResult<StockList> =
        pager.srRequest(
            "$BASE_URL/qt/clist/get", param = mapOf(
                "pn" to pn.toString(),
                "pz" to pz.toString(),
                "po" to po.toString(),
                "np" to np.toString(),
                "fltt" to fltt.toString(),
                "invt" to invt.toString(),
                "fid" to fid,
                "fs" to fs,
                "fields" to fields,
            )
        )

    /**
     * 获取股票实时详情。
     *
     * [stock] 来自股票列表接口，使用其中的东财市场号和股票代码生成 `secid`。
     */
    fun getStockDetail(
        pager: Pager,
        stock: Stock,
        fltt: Int = 2,
        invt: Int = 2,
        fields: String = "f43,f44,f45,f46,f47,f48,f57,f58,f60,f107,f116,f117,f168,f169,f170",
    ): FlowNetWorkResult<StockDetail> =
        pager.srRequest(
            "$BASE_URL/qt/stock/get", param = mapOf(
                "secid" to stock.toEastMoneySecId(),
                "fltt" to fltt.toString(),
                "invt" to invt.toString(),
                "fields" to fields,
            )
        )

    /**
     * 新浪历史 K 线（图表数据源：五日/日/周/月K）。
     * [symbol] 为新浪格式（如 "sh600519"），[scale] 单位为分钟：
     * 5=5 分钟线（五日）、240=日K、1200=周K（按周聚合）、7200=月K（按自然月聚合）。
     * 数据不复权，字段见 [SinaKLinePoint]。
     */
    fun getStockKLine(
        pager: Pager,
        symbol: String,
        scale: Int,
        datalen: Int = 120,
    ): FlowNetWorkResult<List<SinaKLinePoint>> = pager.srRequest(
        SINA_KLINE_URL, param = mapOf(
            "symbol" to symbol,
            "scale" to scale.toString(),
            "ma" to "no",
            "datalen" to datalen.toString(),
        ), packagingBody = true
    )

    /**
     * 新浪当日分时（1 分钟线，含 09:25 集合竞价点与均价）。
     * [symbol] 为新浪格式（如 "sh600519"）。
     */
    fun getStockMinline(
        pager: Pager,
        symbol: String,
    ): FlowNetWorkResult<List<SinaMinlinePoint>> = pager.srRequest(
        SINA_MINLINE_URL, param = mapOf(
            "symbol" to symbol,
            "dpc" to "1",
        ), packagingBody = true
    )
}
