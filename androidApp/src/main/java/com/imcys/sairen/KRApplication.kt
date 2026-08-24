package com.imcys.sairen

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class KRApplication : Application() {

    init {
        application = this
    }

    override fun onCreate() {
        super.onCreate()
        // kotlinx-datetime（KuiklyBase KBA 版）在 Android 上基于 threetenabp，
        // 低 API 设备（< 26）没有 java.time，必须先初始化，否则使用 datetime 时会崩溃
        AndroidThreeTen.init(this)
    }

    companion object {
        lateinit var application: Application
    }
}
