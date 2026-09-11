package com.imcys.sairen.component

import com.imcys.sairen.core.common.module.BridgeModule
import com.imcys.sairen.base.BasePager
import com.imcys.sairen.core.common.ext.acquireBridgeModule
import com.imcys.sairen.core.common.ext.toCommonAssets
import com.imcys.sairen.core.network.ApiResponse
import com.imcys.sairen.core.network.ApiStatus
import com.imcys.sairen.core.network.NetWorkResult
import com.imcys.sairen.theme.SRRadius
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.velse
import com.tencent.kuikly.core.directives.velseif
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

private enum class SRAutoErrorContentSlot {
    Success,
    Loading,
    Default,
    Error,
}

internal fun <T> ViewContainer<*, *>.SRAutoError(
    netWorkResult: () -> NetWorkResult<T>,
    onLoadingContent: ViewBuilder? = {
        View {
            attr {
                flex(1f)
                justifyContentCenter()
                alignItemsCenter()
            }
            View {
                attr {
                    alignItemsCenter()
                }
                Image {
                    attr {
                        size(200f, 200f)
                        borderRadius(100f)
                        if ((getPager() as BasePager).nightModel == true) {
                            src("page_loading_dart.gif".toCommonAssets())
                        } else {
                            src("page_loading_light.gif".toCommonAssets())
                        }
                    }
                }

                Text {
                    attr {
                        marginTop(10f)
                        text("正在探索中...")
                        fontSize(18f)
                        fontWeightBold()
                    }
                }
            }
        }
    },
    onDefaultContent: ViewBuilder? = onLoadingContent,
    onSuccessContent: ViewBuilder = {},
    onErrorContent: (ViewContainer<*, *>.(String?, ApiResponse<T?>?) -> Unit)? = null,
    onRetry: (() -> Unit)? = null,
) {

    fun contentSlot(): SRAutoErrorContentSlot {
        return when (netWorkResult().status) {
            ApiStatus.SUCCESS -> SRAutoErrorContentSlot.Success
            ApiStatus.ERROR -> SRAutoErrorContentSlot.Error
            ApiStatus.LOADING -> {
                if (onLoadingContent != null) {
                    SRAutoErrorContentSlot.Loading
                } else {
                    SRAutoErrorContentSlot.Success
                }
            }

            ApiStatus.DEFAULT -> {
                when {
                    onDefaultContent != null -> SRAutoErrorContentSlot.Default
                    onLoadingContent != null -> SRAutoErrorContentSlot.Loading
                    else -> SRAutoErrorContentSlot.Success
                }
            }
        }
    }

    vif({ contentSlot() == SRAutoErrorContentSlot.Success }) {
        onSuccessContent()
    }
    velseif({ contentSlot() == SRAutoErrorContentSlot.Loading }) {
        onLoadingContent?.invoke(this) ?: onSuccessContent()
    }
    velseif({ contentSlot() == SRAutoErrorContentSlot.Default }) {
        onDefaultContent?.invoke(this) ?: onSuccessContent()
    }
    velse {
        val result = netWorkResult()
        onErrorContent?.invoke(this, result.errorMsg, result.responseData)
            ?: SRCommonError(
                errorMsg = { netWorkResult().errorMsg.orEmpty() },
                onRetry = onRetry,
            )
    }
}

internal fun ViewContainer<*, *>.SRCommonError(
    errorMsg: () -> String,
    onRetry: (() -> Unit)? = null,
) {
    val owner = this
    val pager = getPager() as BasePager

    View {
        attr {
            alignSelfStretch()
            margin(15f)
        }

        View {
            attr {
                flexDirectionRow()
                alignItemsCenter()
                padding(left = 12f, right = 8f, top = 10f, bottom = 10f)
                backgroundColor(Color(pager.SRThemeColor.errorBackground))
                borderRadius(SRRadius.LARGE)
            }

            Text {
                attr {
                    flex(1f)
                    maxHeight(100f)
                    text("错误信息：${errorMsg()}")
                    color(Color(pager.SRThemeColor.error))
                    fontSize(14f)
                }
            }

            View {
                attr {
                    minWidth(48f)
                    minHeight(40f)
                    marginLeft(8f)
                    allCenter()
                }
                event {
                    click {
                        owner.acquireBridgeModule().run {
                            copyToPasteboard(errorMsg())
                            toast("错误信息已复制")
                        }
                    }
                }

                Text {
                    attr {
                        text("复制")
                        color(Color(pager.SRThemeColor.error))
                        fontSize(14f)
                        fontWeightMedium()
                    }
                }
            }
        }

        if (onRetry != null) {
            View {
                attr {
                    alignSelfStretch()
                    minHeight(44f)
                    marginTop(6f)
                    allCenter()
                    backgroundColor(Color(pager.SRThemeColor.tabIndicator))
                    borderRadius(SRRadius.LARGE)
                }
                event {
                    click { onRetry() }
                }

                Text {
                    attr {
                        text("点击重试")
                        color(Color.WHITE)
                        fontSize(15f)
                        fontWeightMedium()
                    }
                }
            }
        }
    }
}
