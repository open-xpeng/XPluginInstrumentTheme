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

import com.xiaopeng.instrument.manager.SurfaceViewManager
import com.xiaopeng.instrument.view.MainFragment
import com.xiaopeng.xposed.instrument.theme.utils.XCMethodHookCatching
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

object MainFragmentHook : (XC_LoadPackage.LoadPackageParam) -> Unit {

    override fun invoke(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        XposedHelpers.findAndHookMethod(MainFragment::class.java, "onHiddenChanged", Boolean::class.java, mXCMethodOnHiddenChanged)

        // @formatter:off
        XposedBridge.hookAllMethods(MainFragment::class.java, "updateLeftListData"          , mXCMethodSkipMethodOnFragmentHidden)
        XposedBridge.hookAllMethods(MainFragment::class.java, "updateRightListData"         , mXCMethodSkipMethodOnFragmentHidden)
        XposedBridge.hookAllMethods(MainFragment::class.java, "updateLeftListHighPosition"  , mXCMethodSkipMethodOnFragmentHidden)
        XposedBridge.hookAllMethods(MainFragment::class.java, "updateRightListHighPosition" , mXCMethodSkipMethodOnFragmentHidden)
        XposedBridge.hookAllMethods(MainFragment::class.java, "showRightListView"           , mXCMethodSkipMethodOnFragmentHidden)
        XposedBridge.hookAllMethods(MainFragment::class.java, "showLeftListView"            , mXCMethodSkipMethodOnFragmentHidden)
        XposedBridge.hookAllMethods(MainFragment::class.java, "showLeftCardView"            , mXCMethodSkipMethodOnFragmentHidden)
        XposedBridge.hookAllMethods(MainFragment::class.java, "showRightCardView"           , mXCMethodSkipMethodOnFragmentHidden)
        XposedBridge.hookAllMethods(MainFragment::class.java, "showLeftSubCardView"         , mXCMethodSkipMethodOnFragmentHidden)
        XposedBridge.hookAllMethods(MainFragment::class.java, "showSubRightCardView"        , mXCMethodSkipMethodOnFragmentHidden)
        // @formatter:on
    }

    private val mXCMethodOnHiddenChanged: XC_MethodHook = object : XCMethodHookCatching() {
        override fun afterHookedMethodCatching(param: MethodHookParam) {
            val isHidden = param.args[0] as Boolean
            // XposedBridge.log("MainFragmentHook.onHiddenChanged isHidden=${isHidden}")
            if (isHidden.not()) {
                SurfaceViewManager.getInstance().resumeMainMap()
            }
        }
    }

    private val mXCMethodSkipMethodOnFragmentHidden: XC_MethodHook = object : XCMethodHookCatching() {
        override fun beforeHookedMethodCatching(param: MethodHookParam) {
            super.beforeHookedMethodCatching(param)
            val fragment = param.thisObject as MainFragment

            // XposedBridge.log("MainFragmentHook.${param.method.name}, isHidden=${fragment.isHidden}")
            if (fragment.isHidden) {
                param.result = null
            }
        }
    }
}
