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

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.xiaopeng.instrument.manager.SurfaceViewManager
import com.xiaopeng.instrument.view.MainFragment
import com.xiaopeng.instrument.viewmodel.NaviViewModel
import com.xiaopeng.xposed.instrument.theme.utils.LeftSubCardAutoSwitch
import com.xiaopeng.xposed.instrument.theme.utils.XCMethodHookCatching
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

object MainFragmentHook : (XC_LoadPackage.LoadPackageParam) -> Unit {

    private const val KEY_RAW_LEFT_SUB_CARD: String = "xplugin.left_raw_sub_card"
    private const val KEY_NAV_ACTIVE: String = "xplugin.left_nav_active"
    private const val KEY_TURN_GUIDANCE_VISIBLE: String = "xplugin.left_turn_guidance_visible"
    private const val KEY_TBT_VISIBLE: String = "xplugin.left_tbt_visible"

    override fun invoke(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        XposedHelpers.findAndHookMethod(MainFragment::class.java, "onViewCreated", View::class.java, Bundle::class.java, mXCMethodOnViewCreated)
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

    private val mXCMethodOnViewCreated: XC_MethodHook = object : XCMethodHookCatching() {
        override fun afterHookedMethodCatching(param: MethodHookParam) {
            super.afterHookedMethodCatching(param)

            val fragment = param.thisObject as MainFragment
            val activity = fragment.activity ?: return
            val naviViewModel = ViewModelProvider(activity)[NaviViewModel::class.java]

            val naviGuidenceVisibility = naviViewModel.getNaviGuidenceVisibility()
            val naviTBtVisibility = naviViewModel.getNaviTBtVisibility()
            XposedHelpers.setAdditionalInstanceField(fragment, KEY_TURN_GUIDANCE_VISIBLE, naviGuidenceVisibility.value == true)
            XposedHelpers.setAdditionalInstanceField(fragment, KEY_TBT_VISIBLE, naviTBtVisibility.value == true)
            updateNavigationCardState(fragment = fragment)

            naviGuidenceVisibility.observe(fragment, OnNavigationSignalChanged(fragment = fragment, signalKey = KEY_TURN_GUIDANCE_VISIBLE))
            naviTBtVisibility.observe(fragment, OnNavigationSignalChanged(fragment = fragment, signalKey = KEY_TBT_VISIBLE))
        }
    }

    private val mXCMethodOnHiddenChanged: XC_MethodHook = object : XCMethodHookCatching() {
        override fun afterHookedMethodCatching(param: MethodHookParam) {
            val fragment = param.thisObject as MainFragment
            val isHidden = param.args[0] as Boolean
            if (isHidden.not()) {
                val rawCardIndex = XposedHelpers.getAdditionalInstanceField(fragment, KEY_RAW_LEFT_SUB_CARD) as? Int
                if (rawCardIndex != null) {
                    XposedHelpers.callMethod(fragment, "showLeftSubCardView", rawCardIndex)
                }
                SurfaceViewManager.getInstance().resumeMainMap()
            }
        }
    }

    private val mXCMethodShowLeftSubCardView: XC_MethodHook = object : XCMethodHookCatching() {
        override fun beforeHookedMethodCatching(param: MethodHookParam) {
            super.beforeHookedMethodCatching(param)

            val fragment = param.thisObject as MainFragment
            val rawCardIndex = param.args[0] as Int

            XposedHelpers.setAdditionalInstanceField(fragment, KEY_RAW_LEFT_SUB_CARD, rawCardIndex)
            if (fragment.isHidden) {
                param.result = null
                return
            }

            val isNavigationActive = XposedHelpers.getAdditionalInstanceField(fragment, KEY_NAV_ACTIVE) as? Boolean ?: false
            param.args[0] = LeftSubCardAutoSwitch.resolve(rawCardIndex = rawCardIndex, isNavigationActive = isNavigationActive)
        }
    }

    private val mXCMethodSkipMethodOnFragmentHidden: XC_MethodHook = object : XCMethodHookCatching() {
        override fun beforeHookedMethodCatching(param: MethodHookParam) {
            super.beforeHookedMethodCatching(param)

            val fragment = param.thisObject as MainFragment
            if (fragment.isHidden) {
                param.result = null
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
        return previous != isNavigationActive
    }

    private class OnNavigationSignalChanged(
        private val fragment: MainFragment,
        private val signalKey: String,
    ) : Observer<Boolean> {
        override fun onChanged(value: Boolean) {
            XposedHelpers.setAdditionalInstanceField(fragment, signalKey, value == true)
            if (!updateNavigationCardState(fragment = fragment)) {
                return
            }

            val rawCardIndex = XposedHelpers.getAdditionalInstanceField(fragment, KEY_RAW_LEFT_SUB_CARD) as? Int ?: return
            XposedHelpers.callMethod(fragment, "showLeftSubCardView", rawCardIndex)
        }
    }
}
