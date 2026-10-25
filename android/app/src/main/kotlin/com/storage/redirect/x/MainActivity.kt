package com.storage.redirect.x

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.storage.redirect.x.data.cache.DeviceInfoCache
import com.storage.redirect.x.data.cache.HomeDataCache
import com.storage.redirect.x.data.cache.PreferenceKeys
import com.storage.redirect.x.util.BackupRestoreNotifier
import java.util.Locale

class MainActivity : ComponentActivity() {

    // debug 指令 page.open 的待消费页面，App 层切页后置空
    private var pendingDebugPage by mutableStateOf<String?>(null)

    override fun attachBaseContext(newBase: Context) {
        // 在 Context 初始化前应用已保存的语言设置
        val prefs = newBase.getSharedPreferences("app_cache", Context.MODE_PRIVATE)
        val langIndex = prefs.getInt(PreferenceKeys.LANGUAGE, 0)
        val tags = arrayOf("", "en", "zh")
        val tag = tags.getOrElse(langIndex) { "" }

        if (tag.isNotEmpty()) {
            val locale = Locale.forLanguageTag(tag)
            val config = Configuration(newBase.resources.configuration)
            config.setLocale(locale)
            super.attachBaseContext(newBase.createConfigurationContext(config))
        } else {
            super.attachBaseContext(newBase)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // 同步恢复缓存，确保首帧即有数据；AppPreferences 已在 SrxApplication 初始化
        DeviceInfoCache.restoreFromPrefs()
        HomeDataCache.restoreFromPrefs()
        BackupRestoreNotifier.ensureChannel(this)

        setContent {
            App(
                pendingDebugPage = pendingDebugPage,
                onDebugPageConsumed = { pendingDebugPage = null },
            )
        }

        consumeDebugPage(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        consumeDebugPage(intent)
    }

    private fun consumeDebugPage(intent: Intent?) {
        if (intent?.action != DebugPage.ACTION_OPEN_PAGE) return
        pendingDebugPage = intent.getStringExtra(DebugPage.EXTRA_PAGE)
    }
}
