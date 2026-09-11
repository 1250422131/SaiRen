package com.imcys.sairen.adapter

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Scale
import com.tencent.kuikly.core.render.android.KuiklyRenderViewContext
import com.tencent.kuikly.core.render.android.adapter.HRImageLoadOption
import com.tencent.kuikly.core.render.android.adapter.IKRImageAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class KRCoilImageAdapter(
    val context: Context,
    private val coroutineScope: CoroutineScope
) : IKRImageAdapter {
    override fun fetchDrawable(
        imageLoadOption: HRImageLoadOption,
        callback: (drawable: Drawable?) -> Unit
    ) {
        when {
            imageLoadOption.isBase64() -> loadFromBase64(imageLoadOption, callback)
            imageLoadOption.isWebUrl() || imageLoadOption.isAssets() || imageLoadOption.isFile() -> {
                requestImage(imageLoadOption, callback)
            }
        }
    }

    override fun getDrawableWidth(
        kuiklyRenderViewContext: KuiklyRenderViewContext,
        drawable: Drawable
    ): Float = drawable.intrinsicWidth.toFloat()

    override fun getDrawableHeight(
        kuiklyRenderViewContext: KuiklyRenderViewContext,
        drawable: Drawable
    ): Float = drawable.intrinsicHeight.toFloat()

    private fun requestImage(
        imageLoadOption: HRImageLoadOption,
        callback: (drawable: Drawable?) -> Unit,
    ) {
        val src = if (imageLoadOption.isAssets()) {
            val assetPath = imageLoadOption.src.substring(HRImageLoadOption.SCHEME_ASSETS.length)
            "file:///android_asset/$assetPath"
        } else {
            imageLoadOption.src
        }
        val request = ImageRequest.Builder(context)
            .data(src)
            .allowHardware(false)
            .apply {
                if (imageLoadOption.needResize && imageLoadOption.requestWidth > 0 && imageLoadOption.requestHeight > 0) {
                    size(imageLoadOption.requestWidth, imageLoadOption.requestHeight)
                    scale(
                        when (imageLoadOption.scaleType) {
                            ImageView.ScaleType.CENTER_CROP -> Scale.FILL
                            else -> Scale.FIT
                        }
                    )
                }
            }
            .build()

        coroutineScope.launch {
            val result = context.imageLoader.execute(request)
            callback((result as? SuccessResult)?.drawable)
        }
    }

    private fun loadFromBase64(
        imageLoadOption: HRImageLoadOption,
        callback: (drawable: Drawable?) -> Unit,
    ) {
        coroutineScope.launch {
            val drawable = withContext(Dispatchers.Default) {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                val bytes = Base64.decode(imageLoadOption.src.split(",")[1], Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                try {
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888
                    options.inJustDecodeBounds = false
                    try {
                        options.inSampleSize = calculateInSampleSize(
                            options,
                            imageLoadOption.requestWidth,
                            imageLoadOption.requestHeight,
                        )
                    } catch (e: ArithmeticException) {
                        Log.d("ECHRImageAdapter", "loadFromBase64: $e")
                    }
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                    BitmapDrawable(Resources.getSystem(), bitmap)
                } catch (e: OutOfMemoryError) {
                    Log.d("ECHRImageAdapter", "oom happen: $e")
                    null
                }
            }
            callback(drawable)
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int,
    ): Int {
        return if (reqWidth != 0 && reqHeight != 0 && reqWidth != -1 && reqHeight != -1) {
            var height = options.outHeight
            var width = options.outWidth
            var inSampleSize = 1
            while (height > reqHeight && width > reqWidth) {
                val heightRatio = (height.toFloat() / reqHeight.toFloat()).roundToInt()
                val widthRatio = (width.toFloat() / reqWidth.toFloat()).roundToInt()
                val ratio = if (heightRatio > widthRatio) heightRatio else widthRatio
                if (ratio < 2) {
                    break
                }
                width = width shr 1
                height = height shr 1
                inSampleSize = inSampleSize shl 1
            }
            inSampleSize
        } else {
            1
        }
    }
}
