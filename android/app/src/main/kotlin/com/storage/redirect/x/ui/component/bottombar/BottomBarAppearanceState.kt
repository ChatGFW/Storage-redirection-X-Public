package com.storage.redirect.x.ui.component.bottombar

import com.storage.redirect.x.data.cache.AppPreferences
import com.storage.redirect.x.data.cache.PreferenceKeys

// 底栏外观：悬浮样式与模糊相互独立，模糊对固定与悬浮底栏都生效
data class BottomBarAppearanceState(
    val isFloatingBottomBarEnabled: Boolean = false,
    val isBottomBarBlurEnabled: Boolean = true,
) {
    companion object {
        fun fromPreferences(
            readBoolean: (String, Boolean) -> Boolean = AppPreferences::getBoolean,
        ): BottomBarAppearanceState {
            return BottomBarAppearanceState(
                isFloatingBottomBarEnabled = readBoolean(PreferenceKeys.FLOATING_BOTTOM_BAR_ENABLED, false),
                isBottomBarBlurEnabled = readBoolean(PreferenceKeys.BOTTOM_BAR_BLUR_ENABLED, true),
            )
        }
    }
}
