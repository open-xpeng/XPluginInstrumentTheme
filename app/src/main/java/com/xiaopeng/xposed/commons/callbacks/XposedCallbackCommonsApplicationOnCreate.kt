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

package com.xiaopeng.xposed.commons.callbacks

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.xiaopeng.xposed.commons.loggers.LoggerKoin
import com.xiaopeng.xposed.commons.loggers.LoggerLog4jInit
import com.xiaopeng.xposed.commons.wrappers.XCMethodHookWrapper
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

class XposedCallbackCommonsApplicationOnCreate(
    private val mLoadPackageParam: XC_LoadPackage.LoadPackageParam,
) : XCMethodHookWrapper(), KoinComponent {

    override fun beforeHookedMethodCatching(methodHookParam: MethodHookParam) {
        super.beforeHookedMethodCatching(methodHookParam)
        val application = methodHookParam.thisObject as Application
        onApplicationOnCreate(application)
    }

    private fun onApplicationOnCreate(application: Application) {
        initKoin(application)
        initSlf4j(application)
        printLogger()
    }

    private fun initKoin(application: Application) {
        val mKoinCommonModule: Module = module {
            // @formatter:off
            single           { mLoadPackageParam }
            single<Gson    > { GsonBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().create() }
            single<Handler > { Handler(Looper.getMainLooper()) }
            // @formatter:on
        }

        val mKoinModuleManager: Module = module {
        }

        startKoin {
            logger(logger = LoggerKoin)
            // androidContext(androidContext = application)
            modules(listOf(mKoinCommonModule, mKoinModuleManager))
        }
    }

    private fun initSlf4j(application: Application) {
        LoggerLog4jInit(application)
    }

    private fun printLogger() {
        mLogger.info("=========================[START]=========================")
        mLogger.info("process={}", mLoadPackageParam.processName)
    }

}
