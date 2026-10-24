package com.storage.redirect.x.ui.page

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.storage.redirect.x.BuildConfig
import com.storage.redirect.x.R
import com.storage.redirect.x.data.cache.AppPreferences
import com.storage.redirect.x.data.cache.PreferenceKeys
import com.storage.redirect.x.ui.component.bottombar.BottomBarAppearanceState
import com.storage.redirect.x.util.BackupRestoreNotifier
import com.storage.redirect.x.ui.component.PAGE_CONTENT_TOP_PADDING
import com.storage.redirect.x.ui.component.PAGE_GROUP_GAP
import com.storage.redirect.x.ui.component.PAGE_HORIZONTAL_PADDING
import com.storage.redirect.x.ui.component.PreferenceIcon
import com.storage.redirect.x.ui.component.SrxTopAppBar
import com.storage.redirect.x.ui.icon.AppIcons
import com.storage.redirect.x.ui.theme.AppThemeSettings
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference

@Composable
@SuppressLint("LocalContextGetResourceValueCall")
fun SettingsPage(
    themeSettings: AppThemeSettings = AppThemeSettings(),
    onThemeSettingsChange: (AppThemeSettings) -> Unit = {},
    bottomBarAppearance: BottomBarAppearanceState = BottomBarAppearanceState(),
    onBottomBarAppearanceChange: (BottomBarAppearanceState) -> Unit = {},
    bottomInnerPadding: androidx.compose.ui.unit.Dp = 0.dp,
) {
    val context = LocalContext.current
    val resources by rememberUpdatedState(LocalResources.current)
    val settingsViewModel: SettingsViewModel = viewModel()
    val uiState by settingsViewModel.uiState.collectAsState()
    var isThemePageVisible by remember { mutableStateOf(false) }

    LaunchedEffect(settingsViewModel) {
        settingsViewModel.events.collect { event ->
            when (event) {
                is SettingsUiEvent.BackupFinished -> {
                    val result = event.result
                    val message = if (result.isSuccess) {
                        resources.getQuantityString(R.plurals.settings_backup_success_with_count, result.packageCount, result.packageCount)
                    } else {
                        resources.getQuantityString(R.plurals.settings_backup_failed_with_count, result.packageCount, result.packageCount)
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    BackupRestoreNotifier.notifyBackupResult(context, result.isSuccess, result.packageCount)
                    settingsViewModel.refreshRootStatus()
                }

                is SettingsUiEvent.RestoreFinished -> {
                    val result = event.result
                    val message = if (result.isSuccess) {
                        resources.getQuantityString(
                            R.plurals.settings_restore_success_with_count,
                            result.restoredPackageCount,
                            result.restoredPackageCount,
                        )
                    } else {
                        resources.getQuantityString(
                            R.plurals.settings_restore_failed_with_count,
                            result.restoredPackageCount,
                            result.restoredPackageCount,
                        )
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    BackupRestoreNotifier.notifyRestoreResult(
                        context,
                        result.isSuccess,
                        result.restoredPackageCount,
                    )
                    settingsViewModel.refreshRootStatus()
                }
            }
        }
    }

    if (isThemePageVisible) {
        ColorPalettePage(
            themeSettings = themeSettings,
            onThemeSettingsChange = onThemeSettingsChange,
            bottomBarAppearance = bottomBarAppearance,
            onBottomBarAppearanceChange = onBottomBarAppearanceChange,
            onBack = { isThemePageVisible = false },
            bottomInnerPadding = bottomInnerPadding,
        )
        return
    }

    SettingsPageMiuix(
        settingsViewModel = settingsViewModel,
        uiState = uiState,
        onOpenTheme = { isThemePageVisible = true },
        bottomInnerPadding = bottomInnerPadding,
    )
}

@Composable
private fun SettingsPageMiuix(
    settingsViewModel: SettingsViewModel,
    uiState: SettingsUiState,
    onOpenTheme: () -> Unit,
    bottomInnerPadding: androidx.compose.ui.unit.Dp = 0.dp,
) {
    val context = LocalContext.current

    if (uiState.isLogsPageVisible) {
        LogsPage(
            onBack = { settingsViewModel.setLogsPageVisible(false) },
            bottomInnerPadding = bottomInnerPadding,
        )
        return
    }

    if (uiState.isLicensesPageVisible) {
        OpenSourceLicensesPage(
            onBack = { settingsViewModel.setLicensesPageVisible(false) },
            bottomInnerPadding = bottomInnerPadding,
        )
        return
    }

    if (uiState.isUpdatePageVisible) {
        UpdatePage(
            onBack = { settingsViewModel.setUpdatePageVisible(false) },
            bottomInnerPadding = bottomInnerPadding,
        )
        return
    }

    if (uiState.isAboutPageVisible) {
        AboutPage(
            onBack = { settingsViewModel.setAboutPageVisible(false) },
            onOpenLicenses = { settingsViewModel.setLicensesPageVisible(true) },
            bottomInnerPadding = bottomInnerPadding,
        )
        return
    }

    val languageOptions = listOf(
        stringResource(R.string.language_system),
        stringResource(R.string.language_en),
        stringResource(R.string.language_zh_cn),
    )
    val languageIndex = AppPreferences.getInt(PreferenceKeys.LANGUAGE, 0).coerceIn(languageOptions.indices)
    val scrollBehavior = MiuixScrollBehavior(rememberTopAppBarState())
    val canOperateRoot = uiState.hasRoot == true && !uiState.isWorking
    val rootActionSummary = when {
        uiState.hasRoot != true -> stringResource(R.string.settings_backup_restore_root_required)
        uiState.isWorking -> stringResource(R.string.settings_action_running)
        else -> stringResource(R.string.settings_fuse_fixer_summary)
    }

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }
        if (uiState.hasRoot != true) {
            return@rememberLauncherForActivityResult
        }
        settingsViewModel.exportPackageConfigsZip(context.contentResolver, uri)
    }
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }
        if (uiState.hasRoot != true) {
            return@rememberLauncherForActivityResult
        }
        settingsViewModel.restorePackageConfigsFromUri(context.contentResolver, uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("page_settings"),
    ) {
        SrxTopAppBar(
            title = stringResource(R.string.settings_title),
            scrollBehavior = scrollBehavior
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(PAGE_CONTENT_TOP_PADDING))

            // 外观设置
            SmallTitle(text = stringResource(R.string.settings_appearance))

            Card(modifier = Modifier.padding(horizontal = PAGE_HORIZONTAL_PADDING)) {
                ArrowPreference(
                    title = stringResource(R.string.theme_title),
                    summary = stringResource(R.string.settings_theme_summary),
                    startAction = { PreferenceIcon(AppIcons.Palette) },
                    onClick = onOpenTheme,
                )

                OverlayDropdownPreference(
                    title = stringResource(R.string.language_title),
                    startAction = { PreferenceIcon(AppIcons.Translate) },
                    items = languageOptions,
                    selectedIndex = languageIndex,
                    onSelectedIndexChange = { index ->
                        AppPreferences.put(PreferenceKeys.LANGUAGE, index)
                        // 通过 recreate 触发 attachBaseContext 应用新语言
                        (context as? Activity)?.recreate()
                    }
                )
            }

            Spacer(Modifier.height(PAGE_GROUP_GAP))

            SmallTitle(text = stringResource(R.string.settings_tools))

            Card(modifier = Modifier.padding(horizontal = PAGE_HORIZONTAL_PADDING)) {
                SwitchPreference(
                    title = stringResource(R.string.settings_fuse_fixer),
                    summary = rootActionSummary,
                    startAction = { PreferenceIcon(AppIcons.Tool, isEnabled = canOperateRoot) },
                    checked = uiState.isFuseFixerEnabled,
                    onCheckedChange = { enabled ->
                        settingsViewModel.setFuseFixerEnabled(enabled)
                    },
                    enabled = canOperateRoot,
                )

                ArrowPreference(
                    title = stringResource(R.string.logs_title),
                    summary = stringResource(R.string.settings_logs_entry_desc),
                    startAction = { PreferenceIcon(AppIcons.Document) },
                    onClick = { settingsViewModel.setLogsPageVisible(true) }
                )
            }

            Spacer(Modifier.height(PAGE_GROUP_GAP))

            SmallTitle(text = stringResource(R.string.settings_backup_restore))

            Card(modifier = Modifier.padding(horizontal = PAGE_HORIZONTAL_PADDING)) {
                ArrowPreference(
                    title = stringResource(R.string.settings_backup_title),
                    startAction = { PreferenceIcon(AppIcons.Backup, isEnabled = canOperateRoot) },
                    onClick = {
                        if (!canOperateRoot) return@ArrowPreference
                        val fileName = "storage_redirect_x_apps_backup_${System.currentTimeMillis()}.zip"
                        backupLauncher.launch(fileName)
                    },
                    enabled = canOperateRoot
                )

                ArrowPreference(
                    title = stringResource(R.string.settings_restore_title),
                    startAction = { PreferenceIcon(AppIcons.Restore, isEnabled = canOperateRoot) },
                    onClick = {
                        if (!canOperateRoot) return@ArrowPreference
                        restoreLauncher.launch(
                            arrayOf(
                                "application/zip",
                                "application/octet-stream",
                                "application/json",
                                "text/plain",
                            )
                        )
                    },
                    enabled = canOperateRoot
                )
            }

            Spacer(Modifier.height(PAGE_GROUP_GAP))

            // 关于
            SmallTitle(text = stringResource(R.string.settings_about))

            Card(modifier = Modifier.padding(horizontal = PAGE_HORIZONTAL_PADDING)) {
                ArrowPreference(
                    title = stringResource(R.string.settings_about),
                    summary = "v${BuildConfig.VERSION_NAME}",
                    startAction = { PreferenceIcon(AppIcons.Info) },
                    modifier = Modifier.testTag("settings_about_entry"),
                    onClick = { settingsViewModel.setAboutPageVisible(true) }
                )

                ArrowPreference(
                    title = stringResource(R.string.settings_update),
                    startAction = { PreferenceIcon(AppIcons.Update) },
                    modifier = Modifier.testTag("settings_update_entry"),
                    onClick = { settingsViewModel.setUpdatePageVisible(true) }
                )
            }

            Spacer(Modifier.height(PAGE_GROUP_GAP + bottomInnerPadding))
        }
    }
}
