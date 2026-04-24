package com.xiaopeng.xposed.instrument.theme.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LeftSubCardNavigationActivityTest {

    @Test
    fun `tbt only does not activate navigation card`() {
        assertFalse(
            LeftSubCardNavigationActivity.isNavigationCardActive(
                isTurnGuidanceVisible = false,
                isTbtVisible = true,
            )
        )
    }

    @Test
    fun `turn guidance activates navigation card regardless of tbt visibility`() {
        assertTrue(
            LeftSubCardNavigationActivity.isNavigationCardActive(
                isTurnGuidanceVisible = true,
                isTbtVisible = false,
            )
        )
        assertTrue(
            LeftSubCardNavigationActivity.isNavigationCardActive(
                isTurnGuidanceVisible = true,
                isTbtVisible = true,
            )
        )
    }
}
