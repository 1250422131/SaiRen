package com.imcys.sairen.core.network.service

import com.imcys.sairen.core.network.FlowNetWorkResult
import com.imcys.sairen.core.network.model.StockDetail
import com.imcys.sairen.core.network.model.Stock
import com.imcys.sairen.core.network.model.StockList
import com.imcys.sairen.core.network.model.toEastMoneySecId
import com.imcys.sairen.core.network.srRequest
import com.tencent.kuikly.core.pager.Pager


object AppApiService {

    const val BASE_URL = "https://push2delay.eastmoney.com/api"



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
}
