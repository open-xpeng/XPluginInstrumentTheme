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

object MiniMapViewCenterHook : (XC_LoadPackage.LoadPackageParam) -> Unit {

    override fun invoke(loadPackageParam: XC_LoadPackage.LoadPackageParam) {

        try {
            XposedHelpers.findAndHookMethod(
                /* className    = */ "com.xiaopeng.montecarlo.navcore.mapdisplay.MiniMapViewWrapper",
                /* classLoader  = */ loadPackageParam.classLoader,
                /* methodName   = */ "getDefaultMapViewTop",
                /* ...parameterTypesAndCallback = */ mXCMethodGetDefaultMapViewTop
            )
        } catch (t: Throwable) {
            XposedBridge.log(t)
        }

        try {
            XposedHelpers.findAndHookMethod(
                /* className    = */ "com.xiaopeng.montecarlo.navcore.mapdisplay.MiniMapViewWrapper",
                /* classLoader  = */ loadPackageParam.classLoader,
                /* methodName   = */ "setMapMode",
                /* ...parameterTypesAndCallback = */
                Int::class.javaPrimitiveType!!,
                Boolean::class.javaPrimitiveType!!,
                Boolean::class.javaPrimitiveType!!,
                mXCMethodSetMapMode
            )
        } catch (t: Throwable) {
            XposedBridge.log(t)
        }
    }

    private const val FULL_MAP_HEIGHT = 720
    private const val FULL_MAP_VIEW_TOP = 560

    private val mXCMethodGetDefaultMapViewTop: XC_MethodHook = object : XCMethodHookCatching() {
        override fun afterHookedMethodCatching(param: MethodHookParam) {
            super.afterHookedMethodCatching(param)

            val mapHeight = XposedHelpers.callMethod(param.thisObject, "getMapHeight") as? Int ?: return
            if (mapHeight == FULL_MAP_HEIGHT) {
                param.result = FULL_MAP_VIEW_TOP
            }
        }
    }

    private val mXCMethodSetMapMode: XC_MethodHook = object : XCMethodHookCatching() {
        override fun afterHookedMethodCatching(param: MethodHookParam) {
            super.afterHookedMethodCatching(param)

            val mapHeight = XposedHelpers.callMethod(param.thisObject, "getMapHeight") as? Int ?: return
            if (mapHeight != FULL_MAP_HEIGHT) {
                return
            }

            val mapLeft = XposedHelpers.callMethod(param.thisObject, "getDefaultMapViewLeft") as? Int ?: return
            XposedHelpers.callMethod(param.thisObject, "setMapViewLeftTop", mapLeft, FULL_MAP_VIEW_TOP)
        }
    }
}
