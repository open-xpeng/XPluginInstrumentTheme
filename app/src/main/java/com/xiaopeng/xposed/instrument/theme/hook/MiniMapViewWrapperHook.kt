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

        MiniMapViewCenterHook(loadPackageParam = loadPackageParam)

        try {
            mMiniMapViewWrapperClass = XposedHelpers.findClass(
                "com.xiaopeng.montecarlo.navcore.mapdisplay.MiniMapViewWrapper",
                loadPackageParam.classLoader
            )
            XposedHelpers.findAndHookMethod(
                /* className    = */ "com.xiaopeng.montecarlo.dynamiclevel.base.DynamicLevelHelper",
                /* classLoader  = */ loadPackageParam.classLoader,
                /* methodName   = */ "enableDynamicLevel",
                /* ...parameterTypesAndCallback = */ mXCMethodEnableDynamicLevel
            )
            XposedHelpers.findAndHookMethod(
                /* className    = */ "com.xiaopeng.montecarlo.navcore.mapdisplay.MapViewWrapper",
                /* classLoader  = */ loadPackageParam.classLoader,
                /* methodName   = */ "setMapLevel",
                /* ...parameterTypesAndCallback = */ Float::class.javaPrimitiveType!!,
                mXCMethodSetMapLevel
            )
        } catch (t: Throwable) {
            XposedBridge.log(t)
        }
    }

    private const val MAP_LEVEL_OFFSET = 1.5f
    private const val MAX_MAP_LEVEL = 19.0f
    private const val KEY_RAW_MAP_LEVEL = "xpit_raw_map_level"

    private var mMiniMapViewWrapperClass: Class<*>? = null

    private val mXCMethodSetMapLevel: XC_MethodHook = object : XCMethodHookCatching() {
        override fun beforeHookedMethodCatching(param: MethodHookParam) {
            super.beforeHookedMethodCatching(param)

            val miniMapViewWrapperClass = mMiniMapViewWrapperClass ?: return
            val thisObject = param.thisObject ?: return
            if (!miniMapViewWrapperClass.isInstance(thisObject)) {
                return
            }

            val level = param.args[0] as? Float ?: return
            XposedHelpers.setAdditionalInstanceField(thisObject, KEY_RAW_MAP_LEVEL, level)
            val adjustedLevel = level + MAP_LEVEL_OFFSET
            param.args[0] = if (adjustedLevel > MAX_MAP_LEVEL) {
                MAX_MAP_LEVEL
            } else {
                adjustedLevel
            }
        }
    }

    private val mXCMethodEnableDynamicLevel: XC_MethodHook = object : XCMethodHookCatching() {
        override fun afterHookedMethodCatching(param: MethodHookParam) {
            super.afterHookedMethodCatching(param)

            val miniMapViewWrapperClass = mMiniMapViewWrapperClass ?: return
            val mapViewWrapper = XposedHelpers.getObjectField(param.thisObject, "mMapViewWrapper") ?: return
            if (!miniMapViewWrapperClass.isInstance(mapViewWrapper)) {
                return
            }

            val level = XposedHelpers.getAdditionalInstanceField(mapViewWrapper, KEY_RAW_MAP_LEVEL) as? Float ?: return
            XposedHelpers.setFloatField(param.thisObject, "mPreLevel", level)
        }
    }
}
