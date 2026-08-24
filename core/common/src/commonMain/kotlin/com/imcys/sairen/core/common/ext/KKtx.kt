package com.imcys.sairen.core.common.ext

import com.tencent.kuikly.core.base.Attr
import com.tencent.kuikly.core.base.DeclarativeBaseView
import com.tencent.kuikly.core.base.attr.ImageUri
import com.tencent.kuikly.core.base.event.Event
import com.tencent.kuikly.core.module.NetworkModule
import com.tencent.kuikly.core.module.RouterModule

fun <A : Attr, E : Event> DeclarativeBaseView<A, E>.acquireRouterModule(): RouterModule {
    return acquireModule(RouterModule.MODULE_NAME)
}

fun <A : Attr, E : Event> DeclarativeBaseView<A, E>.acquireNetworkModule(): NetworkModule {
    return acquireModule(NetworkModule.MODULE_NAME)
}

fun String.toPageAssets() = ImageUri.pageAssets(this)

fun String.toCommonAssets() = ImageUri.commonAssets(this)