package com.xiaopeng.xposed.instrument.theme.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class LeftSubCardHostStateTest {

    @Test
    fun `navigation card keeps left map view type active`() {
        assertEquals(
            LeftSubCardHostState.LEFT_VIEW_TYPE_MAP,
            LeftSubCardHostState.leftViewTypeForEffectiveCard(
                effectiveCardIndex = LeftSubCardAutoSwitch.NAVIGATION_CARD_INDEX,
            )
        )
    }

    @Test
    fun `non navigation cards clear left map view type`() {
        assertEquals(
            LeftSubCardHostState.LEFT_VIEW_TYPE_NONE,
            LeftSubCardHostState.leftViewTypeForEffectiveCard(
                effectiveCardIndex = LeftSubCardAutoSwitch.MEDIA_CARD_INDEX,
            )
        )
        assertEquals(
            LeftSubCardHostState.LEFT_VIEW_TYPE_NONE,
            LeftSubCardHostState.leftViewTypeForEffectiveCard(
                effectiveCardIndex = 3,
            )
        )
    }
}
