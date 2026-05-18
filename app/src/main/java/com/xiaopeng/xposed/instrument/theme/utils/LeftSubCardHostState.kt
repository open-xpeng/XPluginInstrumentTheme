package com.xiaopeng.xposed.instrument.theme.utils

object LeftSubCardHostState {
    const val LEFT_VIEW_TYPE_NONE: Int = 0
    const val LEFT_VIEW_TYPE_MAP: Int = 2

    fun leftViewTypeForEffectiveCard(effectiveCardIndex: Int): Int {
        return if (effectiveCardIndex == LeftSubCardAutoSwitch.NAVIGATION_CARD_INDEX) {
            LEFT_VIEW_TYPE_MAP
        } else {
            LEFT_VIEW_TYPE_NONE
        }
    }
}
