package com.imcys.sairen.core.network

import com.imcys.sairen.core.common.ext.acquireNetworkModule
import com.imcys.sairen.core.network.model.SRModel
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.module.NetworkModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.pager.PageData
import com.tencent.kuikly.core.pager.Pager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement

const val SR_NETWORK_TAG = "SRNetwork"

@OptIn(ExperimentalSerializationApi::class)
val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
    allowSpecialFloatingPointValues = true
    explicitNulls = false
}

fun <T> emptyNetWorkResult(data: T? = null): NetWorkResult<T> = NetWorkResult.Default<T>(data, null)


sealed class NetWorkResult<out T>(
    open val status: ApiStatus,
    open val data: T?,
    open val responseData: ApiResponse<T?>?,
    open val errorMsg: String?
) {

    data class Default<out T>(
        override val data: T?,
        override val responseData: ApiResponse<T?>?
    ) : NetWorkResult<T>(
        status = ApiStatus.DEFAULT,
        data = data,
        responseData = responseData,
        errorMsg = null
    )

    data class Success<out T>(
        override val data: T?,
        override val responseData: ApiResponse<T?>?
    ) : NetWorkResult<T>(
        status = ApiStatus.SUCCESS,
        data = data,
        responseData = responseData,
        errorMsg = null
    )

    data class Error<out T>(
        override val data: T?,
        override val responseData: ApiResponse<T?>?,
        val exception: String
    ) : NetWorkResult<T>(
        status = ApiStatus.ERROR,
        data = data,
        responseData = responseData,
        errorMsg = exception
    )

    class Loading<out T> : NetWorkResult<T>(
        status = ApiStatus.LOADING,
        data = null,
        responseData = null,
        errorMsg = null
    )
}


enum class ApiStatus {
    SUCCESS,
    ERROR,
    LOADING,
    DEFAULT,
}

typealias FlowNetWorkResult<Data> = Flow<NetWorkResult<Data>>


inline fun <reified Body, reified Data> Pager.srRequest(
    url: String,
    param: Data? = null,
    isPost: Boolean = false,
    packagingBody: Boolean = false
) =
    callbackFlow<NetWorkResult<Body>> {
        trySend(NetWorkResult.Loading())
        val bodyJson = param?.let { json.encodeToString(param) } ?: "{}"
        val body = JSONObject(bodyJson)

        val requestLog = if (isPost) {
            "srRequest url=$url isPost=true body=$bodyJson"
        } else {
            val query = body.keys().asSequence()
                .joinToString("&") { key -> "$key=${body.opt(key)}" }
            "srRequest url=$url${if (query.isEmpty()) "" else "?$query"} isPost=false"
        }
        KLog.i(SR_NETWORK_TAG, requestLog)

        val handlersJson = JSONObject(
            json.encodeToString(
                mapOf(
                    "user-agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/151.0.0.0 Safari/537.36 Edg/151.0.0.0",
                )
            )
        )
        acquireNetworkModule().httpRequest(
            url,
            false,
            param = body,
            headers = handlersJson
        ) { data, success, errorMsg, response ->
            if (success) {
                try {
                    val responseBodyJsonStr = if (packagingBody) {
                        """
                            {"data":${data.optString("data")}}
                        """.trimIndent()
                    } else data.toString()
                    val responseObject =
                        json.decodeFromString<ApiResponse<Body>>(responseBodyJsonStr)
                    trySend(
                        NetWorkResult.Success(
                            data = responseObject.data,
                            responseData = responseObject,
                        )
                    )
                } catch (e: Exception) {
                    KLog.e(
                        SR_NETWORK_TAG,
                        "响应解析失败 url=$url body=${data.toString().take(300)} err=${e.message}"
                    )
                    trySend(
                        NetWorkResult.Error<Body>(
                            data = null,
                            responseData = null,
                            exception = "响应解析失败: ${e.message}"
                        )
                    )
                } finally {
                    close()
                }
            } else {
                KLog.e(SR_NETWORK_TAG, "请求失败 url=$url error=$errorMsg")
                trySend(
                    NetWorkResult.Error<Body>(
                        data = null,
                        responseData = null,
                        exception = errorMsg
                    )
                )
                close()
            }
        }
        awaitClose { }
    }.flowOn(Dispatchers.Unconfined)


fun <T, R> NetWorkResult<T>.mapData(transform: (T?, ApiResponse<T?>?) -> R?): NetWorkResult<R> {
    return when (this) {
        is NetWorkResult.Success -> {
            val transformedData = transform(data, responseData)
            val newResponse = this.responseData?.run { ->
                ApiResponse<R>(
                    code = code,
                    data = transformedData,
                )
            }
            NetWorkResult.Success<R>(transformedData, newResponse)
        }

        is NetWorkResult.Error -> NetWorkResult.Error(null, null, this.exception)
        is NetWorkResult.Loading -> NetWorkResult.Loading()
        is NetWorkResult.Default -> {
            val transformedData = transform(data, responseData)
            NetWorkResult.Default(transformedData, null)
        }
    }
}

inline fun <reified T : SRModel> T.toKJSONObject(): JSONObject {
    return JSONObject(json.encodeToString<T>(this))
}

inline fun <reified T : SRModel> JSONObject.toModel(): T {
    return json.decodeFromString(this.toString())
}

inline fun <reified T : SRModel> PageData.toModel(): T {
    return json.decodeFromString(this.params.toString())
}
