package com.storage.redirect.x.ui.component.bottombar

import com.storage.redirect.x.data.cache.PreferenceKeys
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BottomBarAppearanceStateTest {

    @Test
    fun `fromPreferences keeps fixed bottom bar with blur by default`() {
        val state = BottomBarAppearanceState.fromPreferences { _, default -> default }

        assertFalse(state.isFloatingBottomBarEnabled)
        assertTrue(state.isBottomBarBlurEnabled)
    }

    @Test
    fun `blur preference applies without floating bottom bar`() {
        val state = BottomBarAppearanceState.fromPreferences { key, default ->
            when (key) {
                PreferenceKeys.FLOATING_BOTTOM_BAR_ENABLED -> false
                PreferenceKeys.BOTTOM_BAR_BLUR_ENABLED -> false
                else -> default
            }
        }

        assertFalse(state.isFloatingBottomBarEnabled)
        assertFalse(state.isBottomBarBlurEnabled)
    }

    @Test
    fun `fromPreferences reads current preference keys`() {
        val reads = linkedMapOf<String, Boolean>()

        BottomBarAppearanceState.fromPreferences { key, default ->
            reads[key] = default
            default
        }

        assertTrue(reads.containsKey(PreferenceKeys.FLOATING_BOTTOM_BAR_ENABLED))
        assertTrue(reads.containsKey(PreferenceKeys.BOTTOM_BAR_BLUR_ENABLED))
    }
}
