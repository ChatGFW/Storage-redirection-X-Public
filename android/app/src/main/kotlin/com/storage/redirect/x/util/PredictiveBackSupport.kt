package com.storage.redirect.x.util

import android.content.pm.ApplicationInfo
import android.os.Build
import org.lsposed.hiddenapibypass.HiddenApiBypass

// 系统没有运行时开关预测性返回的公开 API，照搬 only_player 走 ApplicationInfo 隐藏方法；API 34+ 才生效
object PredictiveBackSupport {
    fun setEnabled(applicationInfo: ApplicationInfo, isEnabled: Boolean): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return false
        return runCatching {
            HiddenApiBypass.addHiddenApiExemptions(
                "Landroid/content/pm/ApplicationInfo;->setEnableOnBackInvokedCallback",
            )
            val method = ApplicationInfo::class.java.getDeclaredMethod(
                "setEnableOnBackInvokedCallback",
                Boolean::class.javaPrimitiveType,
            )
            method.isAccessible = true
            method.invoke(applicationInfo, isEnabled)
        }.onFailure {
            Logger.error("Failed to toggle predictive back", it)
        }.isSuccess
    }
}
