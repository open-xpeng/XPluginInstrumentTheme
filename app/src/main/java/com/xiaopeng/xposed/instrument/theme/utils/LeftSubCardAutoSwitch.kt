package com.xiaopeng.xposed.instrument.theme.utils

object LeftSubCardAutoSwitch {
    const val NAVIGATION_CARD_INDEX: Int = 0
    const val MEDIA_CARD_INDEX: Int = 1

    fun resolve(rawCardIndex: Int, isNavigationActive: Boolean): Int {
        if (rawCardIndex != NAVIGATION_CARD_INDEX) {
            return rawCardIndex
        }
        return if (isNavigationActive) NAVIGATION_CARD_INDEX else MEDIA_CARD_INDEX
    }
}
