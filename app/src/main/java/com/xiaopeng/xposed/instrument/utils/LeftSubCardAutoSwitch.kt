/*
 * Copyright 2026 Reccmost
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xiaopeng.xposed.instrument.utils

object LeftSubCardAutoSwitch {
    private val mLogger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(this.javaClass.simpleName)

    const val NAVIGATION_CARD_INDEX: Int = 0
    const val MEDIA_CARD_INDEX: Int = 1
    const val NO_LEFT_MAP_SURFACE_TYPE: Int = 0
    const val LEFT_SD_MAP_SURFACE_TYPE: Int = 2

    fun resolve(rawCardIndex: Int, isNavigationActive: Boolean): Int {
        val result: Int = if (rawCardIndex != NAVIGATION_CARD_INDEX) {
            rawCardIndex
        } else if (isNavigationActive) {
            NAVIGATION_CARD_INDEX
        } else {
            MEDIA_CARD_INDEX
        }
        if (mLogger.isDebugEnabled) {
            mLogger.debug(
                "event=auto_switch_resolved rawValue={} effectiveValue={} navActive={}",
                rawCardIndex,
                result,
                isNavigationActive
            )
        }
        return result
    }

    fun resolveLeftMapSurfaceType(rawCardIndex: Int, isNavigationActive: Boolean): Int {
        val effectiveCardIndex = resolve(rawCardIndex = rawCardIndex, isNavigationActive = isNavigationActive)
        val result: Int = if (effectiveCardIndex == NAVIGATION_CARD_INDEX) {
            LEFT_SD_MAP_SURFACE_TYPE
        } else {
            NO_LEFT_MAP_SURFACE_TYPE
        }
        if (mLogger.isDebugEnabled) {
            mLogger.debug(
                "event=surface_type_resolved rawValue={} surfaceType={} navActive={}",
                rawCardIndex,
                result,
                isNavigationActive
            )
        }
        return result
    }

    fun isNavigationActive(
        isTurnGuidanceVisible: Boolean,
        isTbtVisible: Boolean,
    ): Boolean {
        // TBT can be raised by lane guidance alone, so it must not drive left-card switching.
        val result = isTurnGuidanceVisible
        if (mLogger.isDebugEnabled) {
            mLogger.debug(
                "event=navigation_state_resolved turnGuidanceVisible={} tbtVisible={} result={}",
                isTurnGuidanceVisible,
                isTbtVisible,
                result
            )
        }
        return result
    }
}
