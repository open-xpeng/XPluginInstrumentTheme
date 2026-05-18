/*
 * Copyright 2026 Sollyu
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

package com.xiaopeng.xposed.commons.wrappers

import de.robv.android.xposed.XC_MethodHook
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class XCMethodHookWrapper : XC_MethodHook() {

    internal val mLogger: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    final override fun beforeHookedMethod(methodHookParam: MethodHookParam) {
        super.beforeHookedMethod(methodHookParam)
        runCatching { beforeHookedMethodCatching(methodHookParam) }
            .onFailure { mLogger.error("XCMethodHookWrapper:beforeHookedMethod", it) }
    }

    open fun beforeHookedMethodCatching(methodHookParam: MethodHookParam) {
    }

    final override fun afterHookedMethod(methodHookParam: MethodHookParam) {
        super.afterHookedMethod(methodHookParam)
        runCatching { afterHookedMethodCatching(methodHookParam) }
            .onFailure { mLogger.error("XCMethodHookWrapper:afterHookedMethod", it) }
    }

    open fun afterHookedMethodCatching(methodHookParam: MethodHookParam) {
    }

}
