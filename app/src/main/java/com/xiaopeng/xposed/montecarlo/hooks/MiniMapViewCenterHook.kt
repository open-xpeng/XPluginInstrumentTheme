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

    private val mLogger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(this.javaClass.simpleName)

    override fun invoke(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        XposedHelpersWrapper.findAndHookMethod(
            "com.xiaopeng.montecarlo.navcore.mapdisplay.MiniMapViewWrapper",
            loadPackageParam.classLoader,
            "getDefaultMapViewTop",
            mXCMethodGetDefaultMapViewTop
        )
        if (mLogger.isInfoEnabled) {
            mLogger.info(
                "event=hook_registered targetClass={} targetMethod={}",
                "com.xiaopeng.montecarlo.navcore.mapdisplay.MiniMapViewWrapper",
                "getDefaultMapViewTop"
            )
        }
        XposedHelpersWrapper.findAndHookMethod(
            "com.xiaopeng.montecarlo.navcore.mapdisplay.MiniMapViewWrapper",
            loadPackageParam.classLoader,
            "setMapMode",
            Int::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!,
            mXCMethodSetMapMode
        )
        if (mLogger.isInfoEnabled) {
            mLogger.info(
                "event=hook_registered targetClass={} targetMethod={}",
                "com.xiaopeng.montecarlo.navcore.mapdisplay.MiniMapViewWrapper",
                "setMapMode"
            )
        }
    }

    private const val FULL_MAP_HEIGHT = 720
    private const val FULL_MAP_VIEW_TOP = 560

    private val mXCMethodGetDefaultMapViewTop: XC_MethodHook = object : XCMethodHookWrapper() {
        override fun afterHookedMethodCatching(methodHookParam: MethodHookParam) {
            super.afterHookedMethodCatching(methodHookParam)

            val miniMapViewWrapper = methodHookParam.thisObject
            val mapHeight = XposedHelpers.callMethod(miniMapViewWrapper, "getMapHeight") as? Int
            if (mapHeight == null) {
                if (mLogger.isDebugEnabled) {
                    mLogger.debug(
                        "event=hook_skipped targetMethod={} reason={}",
                        "getDefaultMapViewTop",
                        "map_height_unavailable"
                    )
                }
                return
            }
            if (mapHeight != FULL_MAP_HEIGHT) {
                return
            }
            val rawValue = methodHookParam.result as? Int
            methodHookParam.result = FULL_MAP_VIEW_TOP
            if (mLogger.isDebugEnabled) {
                mLogger.debug(
                    "event=hook_value_applied targetMethod={} rawValue={} effectiveValue={} mapHeight={}",
                    "getDefaultMapViewTop",
                    rawValue,
                    FULL_MAP_VIEW_TOP,
                    mapHeight
                )
            }
        }
    }

    private val mXCMethodSetMapMode: XC_MethodHook = object : XCMethodHookWrapper() {
        override fun afterHookedMethodCatching(methodHookParam: MethodHookParam) {
            super.afterHookedMethodCatching(methodHookParam)

            val miniMapViewWrapper = methodHookParam.thisObject
            val mapHeight = XposedHelpers.callMethod(miniMapViewWrapper, "getMapHeight") as? Int
            if (mapHeight == null) {
                if (mLogger.isDebugEnabled) {
                    mLogger.debug(
                        "event=hook_skipped targetMethod={} reason={}",
                        "setMapMode",
                        "map_height_unavailable"
                    )
                }
                return
            }
            if (mapHeight != FULL_MAP_HEIGHT) {
                return
            }

            val mapLeft = XposedHelpers.callMethod(miniMapViewWrapper, "getDefaultMapViewLeft") as? Int
            if (mapLeft == null) {
                if (mLogger.isDebugEnabled) {
                    mLogger.debug(
                        "event=hook_skipped targetMethod={} reason={}",
                        "setMapMode",
                        "map_left_unavailable"
                    )
                }
                return
            }
            XposedHelpers.callMethod(miniMapViewWrapper, "setMapViewLeftTop", mapLeft, FULL_MAP_VIEW_TOP)
            if (mLogger.isDebugEnabled) {
                mLogger.debug(
                    "event=hook_value_applied targetMethod={} mapMode={} animated={} force={} mapLeft={} appliedTop={} mapHeight={}",
                    "setMapMode",
                    methodHookParam.args.getOrNull(0) as? Int,
                    methodHookParam.args.getOrNull(1) as? Boolean,
                    methodHookParam.args.getOrNull(2) as? Boolean,
                    mapLeft,
                    FULL_MAP_VIEW_TOP,
                    mapHeight
                )
            }
        }
    }
}
