package com.imcys.sairen.ui.search

import com.imcys.sairen.core.common.ext.toObservableList
import com.imcys.sairen.core.network.NetWorkResult
import com.imcys.sairen.core.network.emptyNetWorkResult
import com.imcys.sairen.core.network.model.StockSuggest
import com.imcys.sairen.core.network.model.StockSuggestTable
import com.imcys.sairen.core.network.service.AppApiService
import com.tencent.kuikly.core.coroutines.Job
import com.tencent.kuikly.core.coroutines.launch
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.ObservableThreadSafetyMode
import com.tencent.kuikly.core.reactive.collection.ObservableList
import com.tencent.kuikly.core.reactive.handler.observable
import kotlinx.coroutines.delay


internal data class SearchPageState(
    val keyword: String = "",
    val result: NetWorkResult<StockSuggestTable> = emptyNetWorkResult(),
    val suggestions: ObservableList<StockSuggest> = ObservableList(),
)
internal class SearchStore(private val pager: Pager) {
    var state by pager.observable(ObservableThreadSafetyMode.NONE, SearchPageState())
        private set

    private var searchJob: Job? = null

    fun changeKeyword(keyword: String) {
        state = state.copy(keyword = keyword)
        searchJob?.cancel()
        val value = keyword.trim()
        if (value.isEmpty()) {
            state = state.copy(result = emptyNetWorkResult(), suggestions = ObservableList())
            return
        }
        searchJob = pager.lifecycleScope.launch {
            delay(250)
            AppApiService.getStockSuggest(pager, value).collect { result ->
                state = state.copy(
                    result = result,
                    suggestions = if (result is NetWorkResult.Success) {
                        result.data?.data.orEmpty().toObservableList()
                    } else state.suggestions,
                )
            }
        }
    }

}
