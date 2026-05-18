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
import android.view.Surface
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.xiaopeng.instrument.bean.GearType
import com.xiaopeng.instrument.manager.SurfaceViewManager
import com.xiaopeng.instrument.view.MainActivity
import com.xiaopeng.instrument.view.MainFragment
import com.xiaopeng.instrument.viewmodel.InfoViewModel
import com.xiaopeng.instrument.viewmodel.sr.SRInfoViewModel
import com.xiaopeng.instrument.viewmodel.sr.SRNaviViewModel
import com.xiaopeng.xposed.commons.extensions.getResourceId
import com.xiaopeng.xposed.commons.wrappers.XCMethodHookWrapper
import com.xiaopeng.xposed.instrument.XposedMain
import com.xiaopeng.xposed.instrument.fragments.MapFullFragment
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.sollyu.xposed.hyper.hdh.commons.wrappers.XposedHelpersWrapper
import org.joor.Reflect

object MainActivityHook : (XC_LoadPackage.LoadPackageParam) -> Unit {

    private val mFragmentTag: String = MapFullFragment::class.java.name
    private const val NAVI_SR_FRAGMENT_NAME: String = "com.xiaopeng.instrument.view.NaviSRFragment"

    override fun invoke(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        XposedHelpersWrapper.findAndHookMethod(
            MainActivity::class.java,
            "onCreate",
            Bundle::class.java,
            mXCMethodOnViewCreated
        )
        XposedHelpersWrapper.findAndHookMethod(
            MainActivity::class.java,
            "showFragmentByClass",
            Class::class.java,
            mXCMethodShowFragmentByClass
        )
    }

    private val mXCMethodShowFragmentByClass: XC_MethodHook = object : XCMethodHookWrapper() {

        override fun beforeHookedMethodCatching(methodHookParam: MethodHookParam) {
            super.beforeHookedMethodCatching(methodHookParam)
            val activity: MainActivity = methodHookParam.thisObject as MainActivity
            val targetFragmentClass = methodHookParam.args[0] as Class<*>

            if (targetFragmentClass != MainFragment::class.java) {
                return
            }

            val infoViewModel = ViewModelProvider(owner = activity)[InfoViewModel::class.java]
            if (infoViewModel.gearLiveData.value != GearType.GEAR_D) {
                return
            }

            showMapFullFragment(activity = activity)
            methodHookParam.result = null
        }

        override fun afterHookedMethodCatching(methodHookParam: MethodHookParam) {
            super.afterHookedMethodCatching(methodHookParam)

            val targetFragmentClass = methodHookParam.args[0] as Class<*>
            if (targetFragmentClass.name != NAVI_SR_FRAGMENT_NAME) {
                return
            }

            val activity = methodHookParam.thisObject as MainActivity
            val fragment = activity.supportFragmentManager.findFragmentByTag(NAVI_SR_FRAGMENT_NAME) ?: return
            val srSurfaceView = XposedHelpers.getObjectField(fragment, "mSrSurfaceView") ?: return
            val srSurface = XposedHelpers.callMethod(srSurfaceView, "getSurface") as? Surface ?: return

            if (fragment.isHidden) {
                return
            }

            SurfaceViewManager.getInstance().srSurface = srSurface
            SurfaceViewManager.getInstance().startSRChangeService()
        }
    }

    private val mXCMethodOnViewCreated: XC_MethodHook = object : XCMethodHookWrapper() {
        override fun afterHookedMethodCatching(methodHookParam: MethodHookParam) {
            super.afterHookedMethodCatching(methodHookParam)

            val activity: MainActivity = methodHookParam.thisObject as MainActivity
            val infoViewModel = ViewModelProvider(owner = activity)[InfoViewModel::class.java]
            ViewModelProvider(owner = activity)[SRInfoViewModel::class.java]
            ViewModelProvider(owner = activity)[SRNaviViewModel::class.java]

            // 添加模块资源
            XposedHelpers.callMethod(/* obj = */ activity.getResources().assets, /* methodName = */ "addAssetPath", /* ...args = */ XposedMain.MODULE_PATH);

            infoViewModel.gearLiveData.observe(/* owner = */ activity, /* observer = */ OnGearLiveDataChanged(mainActivity = activity))
        }

        inner class OnGearLiveDataChanged(private val mainActivity: MainActivity) : Observer<Int> {

            override fun onChanged(value: Int) {
                when (value) {
                    GearType.GEAR_D -> showMapFullFragment(mainActivity)
                    else            -> hideMapFullFragment(mainActivity)
                }
            }
        }

    }

    private fun showMapFullFragment(activity: MainActivity) {
        val fragmentManager: FragmentManager = activity.supportFragmentManager

        // 已经在显示，无需重复操作
        val currentFragmentName: String? = activity.mCurrentFragmentName
        if (currentFragmentName == mFragmentTag) {
            return
        }

        // 启动 Fragment 切换动画
        Reflect
            .on(/* object = */ activity)
            .field(/* name = */ "mFragChangeAnimView")
            .call(/* name = */ "startFragmentChangeAnim", /* ...args = */ 300L)

        val transaction = fragmentManager.beginTransaction()

        // 隐藏当前 Fragment，并记录以便退出 D 挡时恢复
        if (!currentFragmentName.isNullOrBlank()) {
            val currentFragment = fragmentManager.findFragmentByTag(currentFragmentName)
            if (currentFragment != null) {
                transaction.hide(currentFragment)
            }
        }

        // 添加或显示 MapFullFragment
        val mapFragment = fragmentManager.findFragmentByTag(mFragmentTag) as? MapFullFragment
        if (mapFragment == null) {
            val fragment = MapFullFragment()
            val containerId = activity.getResourceId(/* type = */ "id", /* name = */ "fragment_container")
            transaction.add(/* containerViewId = */ containerId, /* fragment = */ fragment, /* tag = */ mFragmentTag)
        } else {
            transaction.show(mapFragment)
        }

        transaction.commitNow()
        activity.mCurrentFragmentName = mFragmentTag
    }

    private fun hideMapFullFragment(activity: MainActivity) {
        // MapFullFragment 未在显示，无需操作
        if (activity.mCurrentFragmentName != mFragmentTag) {
            return
        }

        val fragmentManager: FragmentManager = activity.supportFragmentManager
        val transaction = fragmentManager.beginTransaction()

        // 隐藏 MapFullFragment
        val mapFragment = fragmentManager.findFragmentByTag(mFragmentTag)
        if (mapFragment != null) {
            transaction.hide(mapFragment)
        }

        val mainFragmentTag: String = MainFragment::class.java.name
        activity.mCurrentFragmentName = MainFragment::class.java.name

        val prevFragment = fragmentManager.findFragmentByTag(mainFragmentTag)
        if (prevFragment == null) {
            transaction.commitNow()
            return
        }

        transaction.show(prevFragment)
        transaction.commitNow()
    }

}
