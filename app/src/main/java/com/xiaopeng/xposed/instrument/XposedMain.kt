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

package com.xiaopeng.xposed.instrument

import android.app.Application
import com.xiaopeng.xposed.commons.callbacks.XposedCallbackCommonsApplicationOnCreate
import com.xiaopeng.xposed.commons.loader.HostClassLoader
import com.xiaopeng.xposed.commons.wrappers.IXposedHookLoadPackageCatching
import com.xiaopeng.xposed.instrument.hooks.MainActivityHook
import com.xiaopeng.xposed.instrument.hooks.MainFragmentHook
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.sollyu.xposed.hyper.hdh.commons.wrappers.XposedHelpersWrapper

class XposedMain : IXposedHookLoadPackageCatching, IXposedHookZygoteInit {

    companion object {
        lateinit var MODULE_PATH: String
            private set

        lateinit var MODULE_CLASS_LOADER: ClassLoader
            private set
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        MODULE_PATH = startupParam.modulePath
    }

    override fun handleLoadPackageCatching(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        if (loadPackageParam.packageName != "com.xiaopeng.instrument") {
            return
        }

        MODULE_CLASS_LOADER = loadPackageParam.classLoader
        HostClassLoader.injectClassLoader(hostClassLoader = loadPackageParam.classLoader)

        XposedHelpersWrapper.findAndHookMethod(Application::class.java, "onCreate", XposedCallbackCommonsApplicationOnCreate(mLoadPackageParam = loadPackageParam))

        MainActivityHook(loadPackageParam = loadPackageParam)
        MainFragmentHook(loadPackageParam = loadPackageParam)
    }

}
