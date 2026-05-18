package com.xiaopeng.xposed.commons.wrappers

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

interface IXposedHookLoadPackageCatching: IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            handleLoadPackageCatching(lpparam)
        } catch (throwable: Throwable) {
            XposedBridge.log(throwable)
        }
    }

    fun handleLoadPackageCatching(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
    }

}
