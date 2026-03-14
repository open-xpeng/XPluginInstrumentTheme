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

package com.xiaopeng.xposed.instrument.theme.hook

import com.xiaopeng.xposed.instrument.theme.utils.XCMethodHookCatching
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

object MiniMapViewWrapperHook : (XC_LoadPackage.LoadPackageParam) -> Unit {

    override fun invoke(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        XposedHelpers.findAndHookMethod(
            /* className    = */ "com.xiaopeng.montecarlo.navcore.mapdisplay.MiniMapViewWrapper",
            /* classLoader  = */ loadPackageParam.classLoader,
            /* methodName   = */ "getDefaultMapViewTop",
            /* ...parameterTypesAndCallback = */ mXCMethodGetDefaultMapViewTop
        )
    }

    private val mXCMethodGetDefaultMapViewTop: XC_MethodHook = object : XCMethodHookCatching() {
        override fun afterHookedMethodCatching(param: MethodHookParam) {
            super.afterHookedMethodCatching(param)

            val result = param.result as Int
            XposedBridge.log("getDefaultMapViewTop, result=${result}")

            param.result = when (result) {
                363  -> 363 /* 正常卡牌偏移量 */
                396  -> 600 /* 全屏卡牌偏移量 600指往下偏移 */
                else -> param.result
            }
        }
    }
}
