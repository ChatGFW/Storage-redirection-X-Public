package com.storage.redirect.x.ui.page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storage.redirect.x.data.cache.AppPreferences
import com.storage.redirect.x.data.cache.PreferenceKeys
import com.storage.redirect.x.data.model.UpdateChannel
import com.storage.redirect.x.data.repository.UpdateRepository
import com.storage.redirect.x.data.repository.UpdateResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UpdateUiState(
    val shouldCheckOnLaunch: Boolean,
    val channel: UpdateChannel,
    val isChecking: Boolean = false,
    val result: UpdateResult? = null,
    val isDialogVisible: Boolean = false,
)

class UpdateViewModel : ViewModel() {
    private val repository = UpdateRepository()
    private val state = MutableStateFlow(
        UpdateUiState(
            shouldCheckOnLaunch = AppPreferences.getBoolean(PreferenceKeys.CHECK_UPDATE_ON_LAUNCH, true),
            channel = UpdateChannel.entries[AppPreferences.getInt(PreferenceKeys.UPDATE_CHANNEL, 0)],
        )
    )
    val uiState = state.asStateFlow()
    private var checkJob: Job? = null

    init {
        viewModelScope.launch {
            // 首屏显示后再检查，启动时只提示确实存在的新版本。
            delay(2_000)
            if (state.value.shouldCheckOnLaunch && state.value.result == null) checkForUpdate(false)
        }
    }

    fun setCheckOnLaunch(isEnabled: Boolean) {
        AppPreferences.put(PreferenceKeys.CHECK_UPDATE_ON_LAUNCH, isEnabled)
        state.update { it.copy(shouldCheckOnLaunch = isEnabled) }
    }

    fun setChannel(channel: UpdateChannel) {
        checkJob?.cancel()
        AppPreferences.put(PreferenceKeys.UPDATE_CHANNEL, channel.ordinal)
        state.update { it.copy(channel = channel, isChecking = false, result = null, isDialogVisible = false) }
    }

    fun checkForUpdate(isUserRequested: Boolean = true) {
        if (state.value.isChecking) return
        val channel = state.value.channel
        state.update { it.copy(isChecking = true, result = null, isDialogVisible = false) }
        checkJob = viewModelScope.launch {
            val result = repository.checkForUpdate(channel)
            state.update {
                if (it.channel != channel) return@update it
                it.copy(
                    isChecking = false,
                    result = result,
                    isDialogVisible = isUserRequested || result is UpdateResult.Available,
                )
            }
        }
    }

    fun dismissDialog() {
        state.update { it.copy(isDialogVisible = false) }
    }
}
