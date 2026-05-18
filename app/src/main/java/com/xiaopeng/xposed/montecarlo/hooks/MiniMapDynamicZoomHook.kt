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

object MiniMapDynamicZoomHook : (XC_LoadPackage.LoadPackageParam) -> Unit {

    private val mLogger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(this.javaClass.simpleName)

    override fun invoke(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        mMiniMapViewWrapperClass = XposedHelpers.findClass(
            "com.xiaopeng.montecarlo.navcore.mapdisplay.MiniMapViewWrapper",
            loadPackageParam.classLoader
        )
        if (mLogger.isInfoEnabled) {
            mLogger.info(
                "event=hook_class_resolved targetClass={}",
                mMiniMapViewWrapperClass?.name
            )
        }

        XposedHelpersWrapper.findAndHookMethod(
            "com.xiaopeng.montecarlo.dynamiclevel.base.DynamicLevelHelper",
            loadPackageParam.classLoader,
            "enableDynamicLevel",
            mXCMethodEnableDynamicLevel
        )
        if (mLogger.isInfoEnabled) {
            mLogger.info(
                "event=hook_registered targetClass={} targetMethod={}",
                "com.xiaopeng.montecarlo.dynamiclevel.base.DynamicLevelHelper",
                "enableDynamicLevel"
            )
        }

        XposedHelpersWrapper.findAndHookMethod(
            "com.xiaopeng.montecarlo.navcore.mapdisplay.MapViewWrapper",
            loadPackageParam.classLoader,
            "setMapLevel",
            Float::class.javaPrimitiveType!!,
            mXCMethodSetMapLevel
        )
        if (mLogger.isInfoEnabled) {
            mLogger.info(
                "event=hook_registered targetClass={} targetMethod={}",
                "com.xiaopeng.montecarlo.navcore.mapdisplay.MapViewWrapper",
                "setMapLevel"
            )
        }
    }

    private const val MAP_LEVEL_OFFSET = 1.5f
    private const val MAX_MAP_LEVEL = 19.0f
    private const val KEY_RAW_MAP_LEVEL = "xpit_raw_map_level"

    private var mMiniMapViewWrapperClass: Class<*>? = null

    private val mXCMethodSetMapLevel: XC_MethodHook = object : XCMethodHookWrapper() {
        override fun beforeHookedMethodCatching(methodHookParam: MethodHookParam) {
            super.beforeHookedMethodCatching(methodHookParam)

            val miniMapViewWrapperClass = mMiniMapViewWrapperClass
            if (miniMapViewWrapperClass == null) {
                if (mLogger.isDebugEnabled) {
                    mLogger.debug(
                        "event=hook_skipped targetMethod={} reason={}",
                        "setMapLevel",
                        "mini_map_view_wrapper_class_unresolved"
                    )
                }
                return
            }
            val thisObject = methodHookParam.thisObject
            if (thisObject == null) {
                if (mLogger.isDebugEnabled) {
                    mLogger.debug(
                        "event=hook_skipped targetMethod={} reason={}",
                        "setMapLevel",
                        "this_object_null"
                    )
                }
                return
            }
            if (!miniMapViewWrapperClass.isInstance(thisObject)) {
                return
            }

            val rawValue = methodHookParam.args[0] as? Float
            if (rawValue == null) {
                if (mLogger.isDebugEnabled) {
                    mLogger.debug(
                        "event=hook_skipped targetMethod={} reason={} valueClass={}",
                        "setMapLevel",
                        "map_level_not_float",
                        methodHookParam.args[0]?.javaClass?.name
                    )
                }
                return
            }
            if (mLogger.isDebugEnabled) {
                mLogger.debug(
                    "event=hook_value_resolved targetMethod={} rawValue={}",
                    "setMapLevel",
                    rawValue
                )
            }
            XposedHelpers.setAdditionalInstanceField(thisObject, KEY_RAW_MAP_LEVEL, rawValue)
            val adjustedLevel = rawValue + MAP_LEVEL_OFFSET
            val effectiveValue = if (adjustedLevel > MAX_MAP_LEVEL) {
                MAX_MAP_LEVEL
            } else {
                adjustedLevel
            }
            methodHookParam.args[0] = effectiveValue
            if (mLogger.isDebugEnabled) {
                mLogger.debug(
                    "event=hook_value_applied targetMethod={} rawValue={} effectiveValue={}",
                    "setMapLevel",
                    rawValue,
                    effectiveValue
                )
            }
        }
    }

    private val mXCMethodEnableDynamicLevel: XC_MethodHook = object : XCMethodHookWrapper() {
        override fun afterHookedMethodCatching(methodHookParam: MethodHookParam) {
            super.afterHookedMethodCatching(methodHookParam)

            val miniMapViewWrapperClass = mMiniMapViewWrapperClass
            if (miniMapViewWrapperClass == null) {
                if (mLogger.isDebugEnabled) {
                    mLogger.debug(
                        "event=hook_skipped targetMethod={} reason={}",
                        "enableDynamicLevel",
                        "mini_map_view_wrapper_class_unresolved"
                    )
                }
                return
            }
            val dynamicLevelHelper = methodHookParam.thisObject
            if (dynamicLevelHelper == null) {
                if (mLogger.isDebugEnabled) {
                    mLogger.debug(
                        "event=hook_skipped targetMethod={} reason={}",
                        "enableDynamicLevel",
                        "this_object_null"
                    )
                }
                return
            }
            val mapViewWrapper =
                XposedHelpers.getObjectField(dynamicLevelHelper, "mMapViewWrapper")
                    ?: run {
                        if (mLogger.isDebugEnabled) {
                            mLogger.debug(
                                "event=hook_skipped targetMethod={} reason={}",
                                "enableDynamicLevel",
                                "map_view_wrapper_null"
                            )
                        }
                        return
                    }
            if (!miniMapViewWrapperClass.isInstance(mapViewWrapper)) {
                return
            }

            val rawValue = XposedHelpers.getAdditionalInstanceField(mapViewWrapper, KEY_RAW_MAP_LEVEL) as? Float
            if (rawValue == null) {
                if (mLogger.isDebugEnabled) {
                    mLogger.debug(
                        "event=hook_skipped targetMethod={} reason={}",
                        "enableDynamicLevel",
                        "raw_map_level_missing"
                    )
                }
                return
            }
            if (mLogger.isDebugEnabled) {
                mLogger.debug(
                    "event=hook_value_resolved targetMethod={} rawValue={}",
                    "enableDynamicLevel",
                    rawValue
                )
            }
            XposedHelpers.setFloatField(dynamicLevelHelper, "mPreLevel", rawValue)
            if (mLogger.isDebugEnabled) {
                mLogger.debug(
                    "event=hook_value_applied targetMethod={} rawValue={} writtenValue={}",
                    "enableDynamicLevel",
                    rawValue,
                    rawValue
                )
            }
        }
    }
}
