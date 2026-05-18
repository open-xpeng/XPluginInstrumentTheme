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

package com.xiaopeng.xposed.montecarlo.hooks

import com.xiaopeng.xposed.commons.wrappers.XCMethodHookWrapper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.sollyu.xposed.hyper.hdh.commons.wrappers.XposedHelpersWrapper

object MiniMapViewCenterHook : (XC_LoadPackage.LoadPackageParam) -> Unit {

    override fun invoke(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        XposedHelpersWrapper.findAndHookMethod(
            "com.xiaopeng.montecarlo.navcore.mapdisplay.MiniMapViewWrapper",
            loadPackageParam.classLoader,
            "getDefaultMapViewTop",
            mXCMethodGetDefaultMapViewTop
        )
        XposedHelpers.findAndHookMethod(
            "com.xiaopeng.montecarlo.navcore.mapdisplay.MiniMapViewWrapper",
            loadPackageParam.classLoader,
            "setMapMode",
            Int::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!,
            mXCMethodSetMapMode
        )

    }

    private const val FULL_MAP_HEIGHT = 720
    private const val FULL_MAP_VIEW_TOP = 560

    private val mXCMethodGetDefaultMapViewTop: XC_MethodHook = object : XCMethodHookWrapper() {
        override fun afterHookedMethodCatching(methodHookParam: MethodHookParam) {
            super.afterHookedMethodCatching(methodHookParam)

            val mapHeight = XposedHelpers.callMethod(methodHookParam.thisObject, "getMapHeight") as? Int ?: return
            if (mapHeight == FULL_MAP_HEIGHT) {
                methodHookParam.result = FULL_MAP_VIEW_TOP
            }
        }
    }

    private val mXCMethodSetMapMode: XC_MethodHook = object : XCMethodHookWrapper() {
        override fun afterHookedMethodCatching(methodHookParam: MethodHookParam) {
            super.afterHookedMethodCatching(methodHookParam)

            val mapHeight = XposedHelpers.callMethod(methodHookParam.thisObject, "getMapHeight") as? Int ?: return
            if (mapHeight != FULL_MAP_HEIGHT) {
                return
            }

            val mapLeft = XposedHelpers.callMethod(methodHookParam.thisObject, "getDefaultMapViewLeft") as? Int ?: return
            XposedHelpers.callMethod(methodHookParam.thisObject, "setMapViewLeftTop", mapLeft, FULL_MAP_VIEW_TOP)
        }
    }
}
