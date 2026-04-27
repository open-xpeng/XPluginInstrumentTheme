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

    fun isNavigationActive(
        isTurnGuidanceVisible: Boolean,
        isTbtVisible: Boolean,
    ): Boolean {
        // TBT can be raised by lane guidance alone, so it must not drive left-card switching.
        return isTurnGuidanceVisible
    }
}
