package com.storage.redirect.x

import android.app.Application
import com.storage.redirect.x.data.cache.AppPreferences
import com.storage.redirect.x.data.cache.PreferenceKeys
import com.storage.redirect.x.util.PredictiveBackSupport

class SrxApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppPreferences.init(this)
        // 须在 Activity 创建窗口前设置，窗口按此决定是否启用预测返回
        PredictiveBackSupport.setEnabled(
            applicationInfo = applicationInfo,
            isEnabled = AppPreferences.getBoolean(PreferenceKeys.PREDICTIVE_BACK_ENABLED),
        )
    }
}
