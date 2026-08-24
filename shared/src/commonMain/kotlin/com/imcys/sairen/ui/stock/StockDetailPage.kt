package com.imcys.sairen.ui.stock

import com.imcys.sairen.base.BasePager
import com.imcys.sairen.component.SRNavigationBar
import com.imcys.sairen.component.screen.stock.OHLCVInfo
import com.imcys.sairen.component.screen.stock.StockDetailTopInfo
import com.imcys.sairen.component.screen.stock.StockKChart
import com.imcys.sairen.core.network.NetWorkResult
import com.imcys.sairen.core.network.emptyNetWorkResult
import com.imcys.sairen.core.network.model.Stock
import com.imcys.sairen.core.network.model.StockDetail
import com.imcys.sairen.core.network.service.AppApiService
import com.imcys.sairen.core.network.toModel
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.coroutines.launch
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.List
import com.tencent.kuikly.core.views.View

@Page("stock_detail")
internal class StockDetailPage : BasePager() {

    private lateinit var baseData: Stock
    private var detailData by observable<NetWorkResult<StockDetail>>(emptyNetWorkResult())

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(Color(ctx.SRThemeColor.pageBackground))
                }

                SRNavigationBar {
                    attr {
                        enabledBack = true
                    }
                }

                // 内容
                List {
                    attr {
                        flex(1f)
                        margin(left = 20f, right = 20f, top = 10f)
                    }
                    attr {
                        flex(1f)
                    }
                    StockDetailTopInfo {
                        attr {
                            details = { ctx.detailData }
                        }
                    }
                    OHLCVInfo {
                        attr {
                            marginTop(30f)
                            details = { ctx.detailData }
                        }
                    }
                    StockKChart {
                        attr {
                            marginTop(15f)
                        }
                    }

                }


            }
        }
    }

    override fun created() {
        super.created()
        baseData = pagerData.params.toModel<Stock>()
        loadDetails()
    }


    private fun loadDetails() {
        lifecycleScope.launch {
            AppApiService.getStockDetail(this@StockDetailPage, baseData).collect {
                detailData = it
            }
        }
    }
}
