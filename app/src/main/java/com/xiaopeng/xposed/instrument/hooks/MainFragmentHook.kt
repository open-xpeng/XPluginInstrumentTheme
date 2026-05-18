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

package com.xiaopeng.xposed.instrument.hooks

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.xiaopeng.instrument.manager.SurfaceViewManager
import com.xiaopeng.instrument.view.MainFragment
import com.xiaopeng.instrument.viewmodel.NaviViewModel
import com.xiaopeng.xposed.commons.wrappers.XCMethodHookWrapper
import com.xiaopeng.xposed.instrument.utils.LeftSubCardAutoSwitch
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

object MainFragmentHook : (XC_LoadPackage.LoadPackageParam) -> Unit {

    private const val KEY_RAW_LEFT_SUB_CARD: String = "xplugin.left_raw_sub_card"
    private const val KEY_NAV_ACTIVE: String = "xplugin.left_nav_active"
    private const val KEY_TURN_GUIDANCE_VISIBLE: String = "xplugin.left_turn_guidance_visible"
    private const val KEY_TBT_VISIBLE: String = "xplugin.left_tbt_visible"

    private val mLogger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(this.javaClass.simpleName)

    override fun invoke(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        XposedHelpers.findAndHookMethod(MainFragment::class.java, "onViewCreated", View::class.java, Bundle::class.java, mXCMethodOnViewCreated)
        XposedHelpers.findAndHookMethod(MainFragment::class.java, "onResume", mXCMethodOnResume)
        XposedHelpers.findAndHookMethod(MainFragment::class.java, "onHiddenChanged", Boolean::class.java, mXCMethodOnHiddenChanged)
        XposedHelpers.findAndHookMethod(MainFragment::class.java, "showLeftSubCardView", Int::class.javaPrimitiveType, mXCMethodShowLeftSubCardView)

        // @formatter:off
        XposedBridge.hookAllMethods(MainFragment::class.java, "updateLeftListData"          , mXCMethodSkipMethodOnFragmentHidden)
        XposedBridge.hookAllMethods(MainFragment::class.java, "updateRightListData"         , mXCMethodSkipMethodOnFragmentHidden)
        XposedBridge.hookAllMethods(MainFragment::class.java, "updateLeftListHighPosition"  , mXCMethodSkipMethodOnFragmentHidden)
        XposedBridge.hookAllMethods(MainFragment::class.java, "updateRightListHighPosition" , mXCMethodSkipMethodOnFragmentHidden)
        XposedBridge.hookAllMethods(MainFragment::class.java, "showRightListView"           , mXCMethodSkipMethodOnFragmentHidden)
        XposedBridge.hookAllMethods(MainFragment::class.java, "showLeftListView"            , mXCMethodSkipMethodOnFragmentHidden)
        XposedBridge.hookAllMethods(MainFragment::class.java, "showLeftCardView"            , mXCMethodSkipMethodOnFragmentHidden)
        XposedBridge.hookAllMethods(MainFragment::class.java, "showRightCardView"           , mXCMethodSkipMethodOnFragmentHidden)
        XposedBridge.hookAllMethods(MainFragment::class.java, "showSubRightCardView"        , mXCMethodSkipMethodOnFragmentHidden)
        // @formatter:on
    }

    private val mXCMethodOnViewCreated: XC_MethodHook = object : XCMethodHookWrapper() {
        override fun afterHookedMethodCatching(methodHookParam: MethodHookParam) {
            super.afterHookedMethodCatching(methodHookParam)

            val fragment = methodHookParam.thisObject as MainFragment
            val activity = fragment.activity ?: return
            val naviViewModel = ViewModelProvider(activity)[NaviViewModel::class.java]

            val naviGuidenceVisibility = naviViewModel.getNaviGuidenceVisibility()
            val naviTBtVisibility = naviViewModel.getNaviTBtVisibility()
            XposedHelpers.setAdditionalInstanceField(fragment, KEY_TURN_GUIDANCE_VISIBLE, naviGuidenceVisibility.value == true)
            XposedHelpers.setAdditionalInstanceField(fragment, KEY_TBT_VISIBLE, naviTBtVisibility.value == true)
            updateNavigationCardState(fragment = fragment)

            if (mLogger.isInfoEnabled) {
                mLogger.info(
                    "event=hook_state_initialized fragment={} turnGuidanceVisible={} tbtVisible={} navActive={}",
                    fragment.javaClass.simpleName,
                    naviGuidenceVisibility.value == true,
                    naviTBtVisibility.value == true,
                    XposedHelpers.getAdditionalInstanceField(fragment, KEY_NAV_ACTIVE)
                )
            }

            naviGuidenceVisibility.observe(fragment.viewLifecycleOwner, OnNavigationSignalChanged(fragment = fragment, signalKey = KEY_TURN_GUIDANCE_VISIBLE))
            naviTBtVisibility.observe(fragment.viewLifecycleOwner, OnNavigationSignalChanged(fragment = fragment, signalKey = KEY_TBT_VISIBLE))
        }
    }

    private val mXCMethodOnResume: XC_MethodHook = object : XCMethodHookWrapper() {
        override fun beforeHookedMethodCatching(methodHookParam: MethodHookParam) {
            val fragment = methodHookParam.thisObject as MainFragment
            if (mLogger.isDebugEnabled) {
                mLogger.debug(
                    "event=hook_enter targetMethod={} fragment={} hidden={}",
                    "onResume",
                    fragment.javaClass.simpleName,
                    fragment.isHidden
                )
            }
            if (fragment.isHidden) {
                if (mLogger.isDebugEnabled) {
                    mLogger.debug("event=hook_skipped targetMethod={} reason={}", "onResume", "fragment_hidden")
                }
                return
            }
            syncLeftMapSurfaceType(fragment = fragment)
        }
    }

    private val mXCMethodOnHiddenChanged: XC_MethodHook = object : XCMethodHookWrapper() {
        override fun afterHookedMethodCatching(methodHookParam: MethodHookParam) {
            val fragment = methodHookParam.thisObject as MainFragment
            val isHidden = methodHookParam.args[0] as Boolean
            if (mLogger.isInfoEnabled) {
                mLogger.info(
                    "event=hook_state_changed targetMethod={} fragment={} newValue={}",
                    "onHiddenChanged",
                    fragment.javaClass.simpleName,
                    isHidden
                )
            }
            if (isHidden.not()) {
                val rawCardIndex = XposedHelpers.getAdditionalInstanceField(fragment, KEY_RAW_LEFT_SUB_CARD) as? Int
                if (mLogger.isDebugEnabled) {
                    mLogger.debug(
                        "event=hook_value_resolved targetMethod={} rawValue={}",
                        "onHiddenChanged",
                        rawCardIndex
                    )
                }
                if (rawCardIndex != null) {
                    XposedHelpers.callMethod(fragment, "showLeftSubCardView", rawCardIndex)
                }
                SurfaceViewManager.getInstance().resumeMainMap()
                if (mLogger.isInfoEnabled) {
                    mLogger.info(
                        "event=hook_value_applied targetMethod={} result={}",
                        "onHiddenChanged",
                        "resume_main_map"
                    )
                }
            }
        }
    }

    private val mXCMethodShowLeftSubCardView: XC_MethodHook = object : XCMethodHookWrapper() {
        override fun beforeHookedMethodCatching(methodHookParam: MethodHookParam) {
            super.beforeHookedMethodCatching(methodHookParam)

            val fragment = methodHookParam.thisObject as MainFragment
            val rawCardIndex = methodHookParam.args[0] as Int

            if (mLogger.isDebugEnabled) {
                mLogger.debug(
                    "event=hook_enter targetMethod={} fragment={} rawValue={} hidden={}",
                    "showLeftSubCardView",
                    fragment.javaClass.simpleName,
                    rawCardIndex,
                    fragment.isHidden
                )
            }
            XposedHelpers.setAdditionalInstanceField(fragment, KEY_RAW_LEFT_SUB_CARD, rawCardIndex)
            if (fragment.isHidden) {
                if (mLogger.isDebugEnabled) {
                    mLogger.debug(
                        "event=hook_skipped targetMethod={} reason={} rawValue={}",
                        "showLeftSubCardView",
                        "fragment_hidden",
                        rawCardIndex
                    )
                }
                methodHookParam.result = null
                return
            }

            val isNavigationActive = XposedHelpers.getAdditionalInstanceField(fragment, KEY_NAV_ACTIVE) as? Boolean ?: false
            val effectiveCardIndex = LeftSubCardAutoSwitch.resolve(rawCardIndex = rawCardIndex, isNavigationActive = isNavigationActive)
            if (mLogger.isDebugEnabled) {
                mLogger.debug(
                    "event=hook_value_resolved targetMethod={} rawValue={} effectiveValue={} navActive={}",
                    "showLeftSubCardView",
                    rawCardIndex,
                    effectiveCardIndex,
                    isNavigationActive
                )
            }
            syncLeftMapSurfaceType(rawCardIndex = rawCardIndex, isNavigationActive = isNavigationActive)
            methodHookParam.args[0] = effectiveCardIndex
            if (mLogger.isDebugEnabled) {
                mLogger.debug(
                    "event=hook_value_applied targetMethod={} rawValue={} effectiveValue={}",
                    "showLeftSubCardView",
                    rawCardIndex,
                    effectiveCardIndex
                )
            }
        }
    }

    private val mXCMethodSkipMethodOnFragmentHidden: XC_MethodHook = object : XCMethodHookWrapper() {
        override fun beforeHookedMethodCatching(methodHookParam: MethodHookParam) {
            super.beforeHookedMethodCatching(methodHookParam)

            val fragment = methodHookParam.thisObject as MainFragment
            if (fragment.isHidden) {
                methodHookParam.result = null
            }
        }
    }

    private fun updateNavigationCardState(fragment: MainFragment): Boolean {
        val previous = XposedHelpers.getAdditionalInstanceField(fragment, KEY_NAV_ACTIVE) as? Boolean
        val isTurnGuidanceVisible = XposedHelpers.getAdditionalInstanceField(fragment, KEY_TURN_GUIDANCE_VISIBLE) as? Boolean ?: false
        val isTbtVisible = XposedHelpers.getAdditionalInstanceField(fragment, KEY_TBT_VISIBLE) as? Boolean ?: false
        val isNavigationActive = LeftSubCardAutoSwitch.isNavigationActive(
            isTurnGuidanceVisible = isTurnGuidanceVisible,
            isTbtVisible = isTbtVisible,
        )
        XposedHelpers.setAdditionalInstanceField(fragment, KEY_NAV_ACTIVE, isNavigationActive)
        if (previous != isNavigationActive && mLogger.isInfoEnabled) {
            mLogger.info(
                "event=hook_state_changed fragment={} previousState={} currentState={} turnGuidanceVisible={} tbtVisible={}",
                fragment.javaClass.simpleName,
                previous,
                isNavigationActive,
                isTurnGuidanceVisible,
                isTbtVisible
            )
        }
        return previous != isNavigationActive
    }

    private fun syncLeftMapSurfaceType(fragment: MainFragment) {
        val rawCardIndex = XposedHelpers.getAdditionalInstanceField(fragment, KEY_RAW_LEFT_SUB_CARD) as? Int ?: return
        val isNavigationActive = XposedHelpers.getAdditionalInstanceField(fragment, KEY_NAV_ACTIVE) as? Boolean ?: false
        if (mLogger.isDebugEnabled) {
            mLogger.debug(
                "event=hook_enter targetMethod={} fragment={} rawValue={} navActive={}",
                "syncLeftMapSurfaceType",
                fragment.javaClass.simpleName,
                rawCardIndex,
                isNavigationActive
            )
        }
        syncLeftMapSurfaceType(rawCardIndex = rawCardIndex, isNavigationActive = isNavigationActive)
    }

    private fun syncLeftMapSurfaceType(rawCardIndex: Int, isNavigationActive: Boolean) {
        val leftMapSurfaceType = LeftSubCardAutoSwitch.resolveLeftMapSurfaceType(
            rawCardIndex = rawCardIndex,
            isNavigationActive = isNavigationActive,
        )
        SurfaceViewManager.getInstance().setLeftViewType(leftMapSurfaceType)
        if (mLogger.isDebugEnabled) {
            mLogger.debug(
                "event=hook_value_applied targetMethod={} rawValue={} navActive={} surfaceType={}",
                "syncLeftMapSurfaceType",
                rawCardIndex,
                isNavigationActive,
                leftMapSurfaceType
            )
        }
    }

    private class OnNavigationSignalChanged(
        private val fragment: MainFragment,
        private val signalKey: String,
    ) : Observer<Boolean> {
        override fun onChanged(value: Boolean) {
            if (mLogger.isDebugEnabled) {
                mLogger.debug(
                    "event=hook_state_changed targetMethod={} fragment={} signalKey={} currentState={}",
                    "navigationObserver",
                    fragment.javaClass.simpleName,
                    signalKey,
                    value == true
                )
            }
            XposedHelpers.setAdditionalInstanceField(fragment, signalKey, value == true)
            if (!updateNavigationCardState(fragment = fragment)) {
                if (mLogger.isDebugEnabled) {
                    mLogger.debug(
                        "event=hook_skipped targetMethod={} reason={} signalKey={}",
                        "navigationObserver",
                        "navigation_state_unchanged",
                        signalKey
                    )
                }
                return
            }

            val rawCardIndex = XposedHelpers.getAdditionalInstanceField(fragment, KEY_RAW_LEFT_SUB_CARD) as? Int ?: return
            if (mLogger.isDebugEnabled) {
                mLogger.debug(
                    "event=hook_value_resolved targetMethod={} signalKey={} rawValue={}",
                    "navigationObserver",
                    signalKey,
                    rawCardIndex
                )
            }
            XposedHelpers.callMethod(fragment, "showLeftSubCardView", rawCardIndex)
        }
    }
}
