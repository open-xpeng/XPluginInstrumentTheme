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

package com.xiaopeng.xposed.instrument.theme

import com.xiaopeng.xposed.instrument.theme.hook.MainActivityHook
import com.xiaopeng.xposed.instrument.theme.hook.MainFragmentHook
import com.xiaopeng.xposed.instrument.theme.hook.MiniMapViewWrapperHook
import com.xiaopeng.xposed.instrument.theme.utils.HostClassLoader
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class XposedMan : IXposedHookLoadPackage, IXposedHookZygoteInit {

    companion object {
        lateinit var MODULE_PATH: String
            private set

        lateinit var MODULE_CLASS_LOADER: ClassLoader
            private set
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        MODULE_PATH = startupParam.modulePath
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        runCatching { handleLoadPackageCatching(loadPackageParam = lpparam) }
            .onFailure { XposedBridge.log(/* t = */ it) }
    }

    private fun handleLoadPackageCatching(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        MODULE_CLASS_LOADER = loadPackageParam.classLoader
        HostClassLoader.injectClassLoader(hostClassLoader = loadPackageParam.classLoader)

        when (loadPackageParam.packageName) {
            "com.xiaopeng.instrument" -> {
                MainActivityHook(loadPackageParam = loadPackageParam)
                MainFragmentHook(loadPackageParam = loadPackageParam)
            }
            "com.xiaopeng.montecarlo" -> MiniMapViewWrapperHook(loadPackageParam = loadPackageParam)
        }
    }


}
