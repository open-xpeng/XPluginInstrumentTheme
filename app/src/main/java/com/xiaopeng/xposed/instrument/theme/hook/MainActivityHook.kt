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
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.xiaopeng.instrument.bean.GearType
import com.xiaopeng.instrument.view.MainActivity
import com.xiaopeng.instrument.viewmodel.InfoViewModel
import com.xiaopeng.xposed.instrument.theme.extensions.getResourceId
import com.xiaopeng.xposed.instrument.theme.fragments.CenterMapFragment
import com.xiaopeng.xposed.instrument.theme.utils.XCMethodHookCatching
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

object MainActivityHook : (XC_LoadPackage.LoadPackageParam) -> Unit {

    override fun invoke(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        XposedHelpers.findAndHookMethod(
            /* clazz        = */ MainActivity::class.java,
            /* methodName   = */ "onCreate",
            /* ...parameterTypesAndCallback = */Bundle::class.java, mXCMethodOnViewCreated
        )
    }

    private val mXCMethodOnViewCreated: XC_MethodHook = object : XCMethodHookCatching() {
        override fun afterHookedMethodCatching(param: MethodHookParam) {
            super.afterHookedMethodCatching(param)

            val thiz: MainActivity = param.thisObject as MainActivity
            val infoViewModel = ViewModelProvider(owner = thiz)[InfoViewModel::class.java]

            infoViewModel.gearLiveData.observe(/* owner = */ thiz, /* observer = */ OnGearLiveDataChanged(mainActivity = thiz))
        }

        inner class OnGearLiveDataChanged(private val mainActivity: MainActivity): Observer<Int> {
            override fun onChanged(value: Int) {
                XposedBridge.log("MainActivityHook:mXCMethodOnViewCreated:mOnGearLiveDataChanged:onChanged value=$value")
                if (value != GearType.GEAR_D) {
                    return
                }

                val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
                val b = fragmentManager.beginTransaction()
                b.replace(mainActivity.getResourceId(/* type = */ "id", /* name = */ "fragment_container"), CenterMapFragment())
                b.commit()

                // Reflect.on(/* object = */ mainActivity).call(/* name = */ "showFragmentByClass", /* ...args = */ CenterMapFragment::class.java)
            }
        }

    }

}
