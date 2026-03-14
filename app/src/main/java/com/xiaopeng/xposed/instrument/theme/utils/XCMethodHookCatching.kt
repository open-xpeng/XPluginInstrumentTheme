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

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

open class XCMethodHookCatching : XC_MethodHook() {
    final override fun beforeHookedMethod(param: MethodHookParam) {
        super.beforeHookedMethod(param)
        runCatching { beforeHookedMethodCatching(param) }
            .onFailure { XposedBridge.log(/* t = */ it) }
    }

    open fun beforeHookedMethodCatching(param: MethodHookParam) {
    }

    final override fun afterHookedMethod(param: MethodHookParam) {
        super.afterHookedMethod(param)
        runCatching { afterHookedMethodCatching(param) }
            .onFailure { XposedBridge.log(/* t = */ it) }
    }

    open fun afterHookedMethodCatching(param: MethodHookParam) {
    }
}
