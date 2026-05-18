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

package com.xiaopeng.xposed.montecarlo

import android.app.Application
import com.xiaopeng.xposed.commons.callbacks.XposedCallbackCommonsApplicationOnCreate
import com.xiaopeng.xposed.montecarlo.hooks.MiniMapDynamicZoomHook
import com.xiaopeng.xposed.montecarlo.hooks.MiniMapViewCenterHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import com.xiaopeng.xposed.commons.wrappers.IXposedHookLoadPackageCatching
import io.github.sollyu.xposed.hyper.hdh.commons.wrappers.XposedHelpersWrapper

class XposedMain : IXposedHookLoadPackageCatching {

    override fun handleLoadPackageCatching(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        super.handleLoadPackageCatching(loadPackageParam)

        if (loadPackageParam.packageName != "com.xiaopeng.montecarlo") {
            return
        }

        XposedHelpersWrapper.findAndHookMethod(
            clazz = Application::class.java,
            methodName = "onCreate",
            XposedCallbackCommonsApplicationOnCreate(mLoadPackageParam = loadPackageParam)
        )

        MiniMapViewCenterHook(loadPackageParam = loadPackageParam)
        MiniMapDynamicZoomHook(loadPackageParam = loadPackageParam)
    }
}
