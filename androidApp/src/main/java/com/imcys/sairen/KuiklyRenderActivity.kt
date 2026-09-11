package com.imcys.sairen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.imcys.sairen.adapter.KRCoilImageAdapter
import com.tencent.kuikly.core.render.android.IKuiklyRenderExport
import com.tencent.kuikly.core.render.android.KuiklyRenderView
import com.tencent.kuikly.core.render.android.adapter.KuiklyRenderAdapterManager
import com.tencent.kuikly.core.render.android.css.ktx.toMap
import com.tencent.kuikly.core.render.android.expand.KuiklyRenderViewBaseDelegatorDelegate
import com.tencent.kuikly.core.render.android.expand.KuiklyRenderViewBaseDelegator
import com.imcys.sairen.adapter.KRColorParserAdapter
import com.imcys.sairen.adapter.KRFontAdapter
import com.imcys.sairen.adapter.KRImageAdapter
import com.imcys.sairen.adapter.KRLogAdapter
import com.imcys.sairen.adapter.KRRouterAdapter
import com.imcys.sairen.adapter.KRThreadAdapter
import com.imcys.sairen.adapter.KRUncaughtExceptionHandlerAdapter
import com.imcys.sairen.module.KRBridgeModule
import com.imcys.sairen.module.KRShareModule
import org.json.JSONObject

class KuiklyRenderActivity : AppCompatActivity(), KuiklyRenderViewBaseDelegatorDelegate {

    private lateinit var hrContainerView: ViewGroup
    private lateinit var loadingView: View
    private lateinit var errorView: View

    private val kuiklyRenderViewDelegator = KuiklyRenderViewBaseDelegator(this)

    private val pageName: String
        get() {
            val pn = intent.getStringExtra(KEY_PAGE_NAME) ?: ""
            return if (pn.isNotEmpty()) {
                return pn
            } else {
                "router"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_hr)
        initKuiklyAdapter()
        setupImmersiveMode()
        // 确保输入法弹出时窗口重新测量，避免对话页底部输入框被键盘遮挡。
        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        hrContainerView = findViewById(R.id.hr_container)
        ViewCompat.setOnApplyWindowInsetsListener(hrContainerView) { view, insets ->
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            view.updatePadding(bottom = imeBottom)
            insets
        }
        ViewCompat.requestApplyInsets(hrContainerView)
        loadingView = findViewById(R.id.hr_loading)
        errorView = findViewById(R.id.hr_error)
        kuiklyRenderViewDelegator.onAttach(hrContainerView, "", pageName, createPageData())
    }

    override fun onDestroy() {
        super.onDestroy()
        kuiklyRenderViewDelegator.onDetach()
    }

    override fun onPause() {
        super.onPause()
        kuiklyRenderViewDelegator.onPause()
    }

    override fun onResume() {
        super.onResume()
        kuiklyRenderViewDelegator.onResume()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setupImmersiveMode()
        kuiklyRenderViewDelegator.sendEvent(
            EVENT_THEME_DID_CHANGED,
            mapOf(KEY_IS_NIGHT_MODE to isSystemInNightMode(newConfig)),
        )
    }

    override fun registerExternalModule(kuiklyRenderExport: IKuiklyRenderExport) {
        super.registerExternalModule(kuiklyRenderExport)
        with(kuiklyRenderExport) {
            moduleExport(KRBridgeModule.MODULE_NAME) {
                KRBridgeModule()
            }
            moduleExport(KRShareModule.MODULE_NAME) {
                KRShareModule()
            }
        }
    }

    override fun registerExternalRenderView(kuiklyRenderExport: IKuiklyRenderExport) {
        super.registerExternalRenderView(kuiklyRenderExport)
        with(kuiklyRenderExport) {

        }
    }

    private fun createPageData(): Map<String, Any> {
        val param = argsToMap()
        param["appId"] = 1
        param[KEY_IS_NIGHT_MODE] = isSystemInNightMode()

        return param
    }

    private fun isSystemInNightMode(configuration: Configuration = resources.configuration): Boolean {
        return configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES
    }

    private fun argsToMap(): MutableMap<String, Any> {
        val jsonStr = intent.getStringExtra(KEY_PAGE_DATA) ?: return mutableMapOf()
        return JSONObject(jsonStr).toMap()
    }

    private fun setupImmersiveMode() {
        setDecorFitsSystemWindows(window)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Color.TRANSPARENT
        } else {
            0x66000000
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val cutoutMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            if (window.attributes.layoutInDisplayCutoutMode != cutoutMode) {
                window.attributes = window.attributes.apply {
                    layoutInDisplayCutoutMode = cutoutMode
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }

        setSystemBarIconAppearance(window, !isEffectiveNightMode())
    }

    /** 以 APP 内「外观」设置为准；未设置过则跟随系统 */
    private fun isEffectiveNightMode(): Boolean {
        return appForcedNightMode ?: isSystemInNightMode()
    }

    private fun setDecorFitsSystemWindows(window: Window) {
        if (Build.VERSION.SDK_INT < 35) {
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            } else {
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or flags
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
    }

    private fun setSystemBarIconAppearance(window: Window, light: Boolean) {
        val appearance = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                if (light) appearance else 0,
                appearance,
            )
            return
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        var flags = window.decorView.systemUiVisibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = if (light) {
                flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flags = if (light) {
                flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
        }
        window.decorView.systemUiVisibility = flags
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP &&
            kuiklyRenderViewDelegator.onBackPressed()
        ) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    private fun initKuiklyAdapter() {
        with(KuiklyRenderAdapterManager) {
            krImageAdapter = KRCoilImageAdapter(
                KRApplication.application,
                this@KuiklyRenderActivity.lifecycleScope,
            )
            krLogAdapter = KRLogAdapter
            krUncaughtExceptionHandlerAdapter = KRUncaughtExceptionHandlerAdapter
            krFontAdapter = KRFontAdapter
            krColorParseAdapter = KRColorParserAdapter(KRApplication.application)
            krRouterAdapter = KRRouterAdapter
            krThreadAdapter = KRThreadAdapter()
        }
    }

    companion object {

        private const val KEY_PAGE_NAME = "pageName"
        private const val KEY_PAGE_DATA = "pageData"
        private const val KEY_IS_NIGHT_MODE = "isNightMode"
        private const val EVENT_THEME_DID_CHANGED = "themeDidChanged"

        /**
         * APP 内「外观」设置生效的深浅色；由 Kuikly 侧 setAppNightMode 写入。
         * null 表示还没设置过，此时跟随系统。
         */
        private var appForcedNightMode: Boolean? = null

        /**
         * Kuikly 侧切换主题后即时刷新系统栏图标明暗。
         *
         * 背景：强制暗色（而系统仍是亮色）时，系统栏图标需要跟着反色，否则状态栏会「看不见」。
         */
        fun refreshSystemBarAppearance(activity: Activity?, isNightMode: Boolean) {
            appForcedNightMode = isNightMode
            val window = activity?.window ?: return
            val appearance = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.setSystemBarsAppearance(
                    if (isNightMode) 0 else appearance,
                    appearance,
                )
                return
            }
            var flags = window.decorView.systemUiVisibility
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags = if (isNightMode) {
                    flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                } else {
                    flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            }
            window.decorView.systemUiVisibility = flags
        }

        fun start(context: Context, pageName: String, pageData: JSONObject) {
            val starter = Intent(context, KuiklyRenderActivity::class.java)
            starter.putExtra(KEY_PAGE_NAME, pageName)
            starter.putExtra(KEY_PAGE_DATA, pageData.toString())
            context.startActivity(starter)
        }

    }
}
