package com.imcys.sairen.ui.stock

import com.imcys.sairen.base.BasePager
import com.imcys.sairen.component.SRAutoError
import com.imcys.sairen.component.SRNavigationBar
import com.imcys.sairen.component.screen.stock.CorporateSummaryInfo
import com.imcys.sairen.component.screen.stock.OHLCVInfo
import com.imcys.sairen.component.screen.stock.StockAIProposal
import com.imcys.sairen.component.screen.stock.StockAIAnalysisStatus
import com.imcys.sairen.component.screen.stock.StockDetailTopInfo
import com.imcys.sairen.component.screen.stock.StockKChart
import com.imcys.sairen.core.chart.KLineLineSeries
import com.imcys.sairen.core.common.ext.toCommonAssets
import com.imcys.sairen.core.network.NetWorkResult
import com.imcys.sairen.core.network.ApiStatus
import com.imcys.sairen.core.network.model.Stock
import com.imcys.sairen.core.network.model.StockCompanyProfile
import com.imcys.sairen.core.network.toModel
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.List
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

@Page("stock_detail")
internal class StockDetailPage : BasePager() {

    private lateinit var baseData: Stock
    private lateinit var store: StockDetailStore

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
                SRAutoError({ ctx.store.state.detailResult }, onSuccessContent = {
                    Scroller {
                        attr {
                            showScrollerIndicator(false)
                            flex(1f)
                            margin(left = 20f, right = 20f, top = 10f)
                        }

                        StockDetailTopInfo {
                            attr {
                                details = { ctx.store.state.detailResult }
                            }
                        }

                        OHLCVInfo {
                            attr {
                                marginTop(30f)
                                details = { ctx.store.state.detailResult }
                            }
                        }

                        StockKChart {
                            attr {
                                marginTop(15f)
                                trends = { ctx.store.state.trendPoints }
                                additionalLines = {
                                    if (ctx.store.state.averageTrendPoints.isEmpty()) emptyList() else listOf(
                                        KLineLineSeries(
                                            points = ctx.store.state.averageTrendPoints,
                                            name = "均价",
                                            color = Color(0xFFFFA000),
                                            smooth = true
                                        )
                                    )
                                }
                            }
                            event {
                                onTabClick =
                                    { tab ->
                                        ctx.store.dispatch(
                                            StockDetailIntent.SelectChart(
                                                tab
                                            )
                                        )
                                    }
                            }
                        }

                        StockAIProposal {
                            attr {
                                marginTop(10f)
                                status = {
                                    val result = ctx.store.state.aiAnalysisResult
                                    when {
                                        result.status == ApiStatus.ERROR || result.data?.status == "failed" ->
                                            StockAIAnalysisStatus.FAILED

                                        result.data?.status == "completed" && result.data?.analysis != null ->
                                            StockAIAnalysisStatus.COMPLETED

                                        else -> StockAIAnalysisStatus.ANALYZING
                                    }
                                }
                                analysis = { ctx.store.state.aiAnalysisResult.data?.analysis }
                                errorMessage = {
                                    ctx.store.state.aiAnalysisResult.errorMsg
                                        ?: ctx.store.state.aiAnalysisResult.data?.error.orEmpty()
                                }
                                updatedAt = { ctx.store.state.aiAnalysisResult.data?.updatedAt.orEmpty() }
                            }
                            event {
                                onRetry = {
                                    ctx.store.dispatch(StockDetailIntent.LoadAIInfo)
                                }
                            }
                        }


                        SRAutoError<StockCompanyProfile>(
                            { ctx.store.state.stockCompanyInfo },
                            onSuccessContent = {
                                CorporateSummaryInfo{
                                    attr {
                                        marginTop(10f)
                                        details = { ctx.store.state.stockCompanyInfo }
                                    }
                                }
                            }, onLoadingContent = {}
                        )
                    }
                }, onRetry = {
                    ctx.store.dispatch(StockDetailIntent.Load)
                })

            }


        }
    }

    override fun created() {
        super.created()
        baseData = pagerData.params.toModel<Stock>()
        store = StockDetailStore(this, baseData)
        store.dispatch(StockDetailIntent.Load)
    }

}
