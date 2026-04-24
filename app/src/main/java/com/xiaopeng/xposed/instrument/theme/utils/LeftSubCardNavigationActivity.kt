package com.xiaopeng.xposed.instrument.theme.utils

object LeftSubCardNavigationActivity {
    fun isNavigationCardActive(
        isTurnGuidanceVisible: Boolean,
        isTbtVisible: Boolean,
    ): Boolean {
        // TBT can be raised by lane guidance alone, so it must not drive left-card switching.
        return isTurnGuidanceVisible
    }
}
