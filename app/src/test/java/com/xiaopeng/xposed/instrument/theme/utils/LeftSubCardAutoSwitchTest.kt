package com.xiaopeng.xposed.instrument.theme.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class LeftSubCardAutoSwitchTest {

    @Test
    fun `keeps non-navigation cards unchanged`() {
        assertEquals(2, LeftSubCardAutoSwitch.resolve(rawCardIndex = 2, isNavigationActive = false))
        assertEquals(4, LeftSubCardAutoSwitch.resolve(rawCardIndex = 4, isNavigationActive = true))
    }

    @Test
    fun `switches navigation card to media when navigation is inactive`() {
        assertEquals(1, LeftSubCardAutoSwitch.resolve(rawCardIndex = 0, isNavigationActive = false))
    }

    @Test
    fun `keeps navigation card when navigation is active`() {
        assertEquals(0, LeftSubCardAutoSwitch.resolve(rawCardIndex = 0, isNavigationActive = true))
    }

    @Test
    fun `tbt only does not activate navigation card`() {
        assertEquals(
            false,
            LeftSubCardAutoSwitch.isNavigationActive(
                isTurnGuidanceVisible = false,
                isTbtVisible = true,
            )
        )
    }

    @Test
    fun `turn guidance activates navigation card regardless of tbt visibility`() {
        assertEquals(
            true,
            LeftSubCardAutoSwitch.isNavigationActive(
                isTurnGuidanceVisible = true,
                isTbtVisible = false,
            )
        )
        assertEquals(
            true,
            LeftSubCardAutoSwitch.isNavigationActive(
                isTurnGuidanceVisible = true,
                isTbtVisible = true,
            )
        )
    }
}
