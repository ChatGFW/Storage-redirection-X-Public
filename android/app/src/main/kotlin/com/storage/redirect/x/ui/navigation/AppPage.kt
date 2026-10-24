package com.storage.redirect.x.ui.navigation

import java.io.Serializable

// 主界面之上的页面，覆盖底栏
sealed interface AppPage : Serializable {
    data object Templates : AppPage
    data class RuleConfig(val packageName: String, val appName: String, val userId: Int) : AppPage
    data object Theme : AppPage
    data object Logs : AppPage
    data object About : AppPage
    data object Licenses : AppPage
    data object Update : AppPage
}
