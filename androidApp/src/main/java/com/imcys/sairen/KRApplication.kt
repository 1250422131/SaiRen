package com.imcys.sairen

import android.app.Application
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import com.jakewharton.threetenabp.AndroidThreeTen

class KRApplication : Application(), ImageLoaderFactory {

    init {
        application = this
    }

    override fun onCreate() {
        super.onCreate()
        // kotlinx-datetime（KuiklyBase KBA 版）在 Android 上基于 threetenabp，
        // 低 API 设备（< 26）没有 java.time，必须先初始化，否则使用 datetime 时会崩溃
        AndroidThreeTen.init(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
                add(SvgDecoder.Factory())
            }
            .build()
    }

    companion object {
        lateinit var application: Application
    }
}
