package com.imcys.sairen.ui.agent

import com.imcys.sairen.core.network.NetWorkResult
import com.imcys.sairen.core.network.model.ChatMessage
import com.imcys.sairen.core.network.model.ChatMessageContent
import com.imcys.sairen.core.network.service.AppApiService
import com.tencent.kuikly.core.coroutines.launch
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.ObservableThreadSafetyMode
import com.tencent.kuikly.core.reactive.collection.ObservableList
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import kotlinx.serialization.json.JsonPrimitive

internal data class AgentChatState(
    val isLoadingHistory: Boolean = false,
    val isSending: Boolean = false,
    val hasMoreHistory: Boolean = false,
    val nextBeforeId: Long? = null,
    val errorMessage: String = "",
)

internal sealed interface AgentChatIntent {
    data object LoadHistory : AgentChatIntent
    data object LoadOlderHistory : AgentChatIntent
    data class SendMessage(val content: String) : AgentChatIntent
}

internal class AgentChatStore(
    private val pager: Pager,
    private val onMessagesChanged: () -> Unit,
) {
    val messages: ObservableList<ChatMessage> by pager.observableList(ObservableThreadSafetyMode.NONE)

    var state by pager.observable(ObservableThreadSafetyMode.NONE, AgentChatState())
        private set

    private var hasLoaded = false
    private var nextLocalMessageId = -1L

    fun dispatch(intent: AgentChatIntent) {
        when (intent) {
            AgentChatIntent.LoadHistory -> loadHistory()
            AgentChatIntent.LoadOlderHistory -> loadHistory(state.nextBeforeId)
            is AgentChatIntent.SendMessage -> sendMessage(intent.content)
        }
    }

    private fun loadHistory(beforeId: Long? = null) {
        if (state.isLoadingHistory || (beforeId == null && hasLoaded)) return
        reduce { copy(isLoadingHistory = true, errorMessage = "") }
        pager.lifecycleScope.launch {
            AppApiService.getChatHistory(pager, beforeId).collect { result ->
                when (result) {
                    is NetWorkResult.Loading -> Unit
                    is NetWorkResult.Success -> {
                        val page = result.data
                        if (page == null) {
                            reduce { copy(isLoadingHistory = false, errorMessage = "历史消息响应为空") }
                            return@collect
                        }
                        if (beforeId == null) {
                            messages.diffUpdate(page.messages)
                            hasLoaded = true
                            onMessagesChanged()
                        } else {
                            val existingIds = messages.mapTo(mutableSetOf()) { it.id }
                            messages.addAll(0, page.messages.filterNot { it.id in existingIds })
                        }
                        reduce {
                            copy(
                                isLoadingHistory = false,
                                hasMoreHistory = page.hasMore,
                                nextBeforeId = page.nextBeforeId,
                            )
                        }
                    }

                    is NetWorkResult.Error -> reduce {
                        copy(isLoadingHistory = false, errorMessage = result.errorMsg.orEmpty())
                    }

                    is NetWorkResult.Default -> Unit
                }
            }
        }
    }

    private fun sendMessage(rawContent: String) {
        val content = rawContent.trim()
        if (content.isEmpty() || state.isSending) return

        val localRequestId = "local_${-nextLocalMessageId}"
        val localUserMessage = ChatMessage(
            id = nextLocalMessageId--,
            requestId = localRequestId,
            role = "user",
            contents = listOf(ChatMessageContent("text", JsonPrimitive(content))),
            status = "completed",
        )
        messages.add(localUserMessage)
        reduce { copy(isSending = true, errorMessage = "") }
        onMessagesChanged()

        pager.lifecycleScope.launch {
            AppApiService.sendChatMessage(pager, content).collect { result ->
                when (result) {
                    is NetWorkResult.Loading -> Unit
                    is NetWorkResult.Success -> {
                        val responseMessages = result.data?.messages
                        if (responseMessages.isNullOrEmpty()) {
                            finishSendingWithError(localRequestId, "消息响应为空，请稍后重试")
                            return@collect
                        }
                        messages.remove(localUserMessage)
                        responseMessages.forEach { message ->
                            val index = messages.indexOfFirst { it.id == message.id }
                            if (index >= 0) messages[index] = message else messages.add(message)
                        }
                        reduce { copy(isSending = false) }
                        onMessagesChanged()
                    }

                    is NetWorkResult.Error -> {
                        val error = result.errorMsg.orEmpty().ifBlank { "消息发送失败，请稍后重试" }
                        finishSendingWithError(localRequestId, error)
                    }

                    is NetWorkResult.Default -> Unit
                }
            }
        }
    }

    private fun finishSendingWithError(requestId: String, error: String) {
        messages.add(
            ChatMessage(
                id = nextLocalMessageId--,
                requestId = requestId,
                role = "assistant",
                contents = listOf(ChatMessageContent("text", JsonPrimitive(error))),
                status = "failed",
            )
        )
        reduce { copy(isSending = false, errorMessage = error) }
        onMessagesChanged()
    }

    private fun reduce(transform: AgentChatState.() -> AgentChatState) {
        state = state.transform()
    }
}
