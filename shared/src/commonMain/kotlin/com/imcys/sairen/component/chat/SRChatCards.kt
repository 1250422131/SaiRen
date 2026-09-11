package com.imcys.sairen.component.chat

import com.imcys.sairen.base.BasePager
import com.imcys.sairen.base.utils.changePercentColor
import com.imcys.sairen.base.utils.formatChangePercent
import com.imcys.sairen.component.SRCard
import com.imcys.sairen.core.chart.KLineLineChart
import com.imcys.sairen.core.chart.KLineLineChartConfig
import com.imcys.sairen.core.chart.KLineLinePoint
import com.imcys.sairen.core.common.ext.acquireRouterModule
import com.imcys.sairen.core.network.toKJSONObject
import com.imcys.sairen.core.network.model.Stock
import com.imcys.sairen.core.network.model.ChatStockBasic
import com.imcys.sairen.core.network.model.ChatStockCompanyCard
import com.imcys.sairen.core.network.model.ChatStockKLineCard
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.core.views.layout.Column
import com.tencent.kuikly.core.views.layout.Row

internal fun ViewContainer<*, *>.ChatStockBasicCard(stock: ChatStockBasic) {
    val colors = (getPager() as BasePager).SRThemeColor
    SRCard {
        attr {
            alignSelfStretch()
            marginBottom(8f)
            padding(14f)
            backgroundColor(Color(colors.stockInfoCard))
        }
        Row {
            attr { alignItemsCenter() }
            Column {
                attr { flex(1f) }
                Text { attr { text(stock.name.ifBlank { "股票行情" }); fontSize(17f); fontWeightBold(); color(colors.primaryText) } }
                Text { attr { text(listOf(stock.code, stock.marketName, stock.currency).filter { it.isNotBlank() }.joinToString(" · ")); marginTop(3f); fontSize(12f); color(colors.secondaryText) } }
            }
            Column {
                attr { alignItemsFlexEnd() }
                Text { attr { text(stock.latestPrice.ifBlank { "-" }); fontSize(20f); fontWeightBold(); color(changePercentColor(stock.changePercent, colors)) } }
                Text { attr { text(formatChangePercent(stock.changePercent)); fontSize(13f); color(changePercentColor(stock.changePercent, colors)) } }
            }
        }
        Row {
            attr { marginTop(14f) }
            QuoteValue("今开", stock.openingPrice)
            QuoteValue("最高", stock.highestPrice)
            QuoteValue("最低", stock.lowestPrice)
            QuoteValue("昨收", stock.previousClosePrice)
        }
    }
}

internal fun ViewContainer<*, *>.ChatStockKLineCard(card: ChatStockKLineCard) {
    val container = this
    val colors = (getPager() as BasePager).SRThemeColor
    val points = card.kLines.mapIndexedNotNull { index, item ->
        item.close?.toFloatOrNull()?.let { KLineLinePoint(index.toLong(), it, item.day.takeLast(5)) }
    }
    SRCard {
        attr {
            alignSelfStretch()
            marginBottom(8f)
            padding(14f)
            backgroundColor(Color(colors.stockInfoCard))
        }
        Text { attr { text(listOf(card.stock.name, card.stock.code, card.stock.marketName, card.stock.currency).filter { it.isNotBlank() }.joinToString(" · ")); fontSize(16f); fontWeightBold(); color(colors.primaryText) } }
        Text { attr { text("日 K 收盘走势 · 点击查看详情"); marginTop(3f); fontSize(12f); color(colors.secondaryText) } }
        View {
            attr { height(180f); marginTop(12f) }
            KLineLineChart {
                attr {
                    absolutePositionAllZero()
                    this.points = { points }
                    config = KLineLineChartConfig(
                        lineColor = Color(colors.primary),
                        fillTopColor = Color(colors.primaryContainer),
                        fillBottomColor = Color(colors.transparent),
                    )
                }
            }
            View {
                attr { absolutePositionAllZero() }
                event { click { container.openChatStockDetail(card.stock) } }
            }
        }
    }
}

internal fun ViewContainer<*, *>.ChatStockCompanyCard(card: ChatStockCompanyCard) {
    val colors = (getPager() as BasePager).SRThemeColor
    val company = card.company
    SRCard {
        attr {
            alignSelfStretch()
            marginBottom(8f)
            padding(14f)
            backgroundColor(Color(colors.stockInfoCard))
        }
        Text { attr { text(company.companyName.ifBlank { card.stock.name }); fontSize(16f); fontWeightBold(); color(colors.primaryText) } }
        val tags = listOf(company.industry, company.exchange).filter { it.isNotBlank() }.joinToString(" · ")
        if (tags.isNotBlank()) Text { attr { text(tags); marginTop(5f); fontSize(12f); color(colors.primary) } }
        if (company.summary.isNotBlank()) Text { attr { text(company.summary); marginTop(10f); fontSize(14f); lines(Int.MAX_VALUE); color(colors.primaryText) } }
        val managers = listOf("董事长" to company.chairman, "总经理" to company.generalManager)
            .filter { it.second.isNotBlank() }.joinToString("   ") { "${it.first}：${it.second}" }
        if (managers.isNotBlank()) Text { attr { text(managers); marginTop(10f); fontSize(12f); color(colors.secondaryText) } }
    }
}

private fun ViewContainer<*, *>.QuoteValue(label: String, value: String) {
    val colors = (getPager() as BasePager).SRThemeColor
    Column {
        attr { flex(1f); alignItemsCenter() }
        Text { attr { text(label); fontSize(11f); color(colors.secondaryText) } }
        Text { attr { text(value.ifBlank { "-" }); marginTop(4f); fontSize(13f); fontWeightMedium(); color(colors.primaryText) } }
    }
}

private fun ViewContainer<*, *>.openChatStockDetail(stock: ChatStockBasic) {
    if (stock.code.isBlank()) return
    val marketCode = stock.marketCode.ifBlank {
        if (stock.code.length == 5) "116" else if (stock.code.startsWith("6")) "1" else "0"
    }
    acquireRouterModule().openPage(
        "stock_detail",
        Stock(
            code = stock.code,
            eastMoneyMarketCode = marketCode,
            name = stock.name,
            latestPrice = stock.latestPrice,
            changePercent = stock.changePercent,
            changeAmount = stock.changeAmount,
            volume = stock.volume,
            amount = stock.amount,
        ).toKJSONObject(),
    )
}
