package com.xiaopeng.xposed.commons.wrappers

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage

interface IXposedHookLoadPackageCatching: IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val mLogger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(this.javaClass.simpleName)
        try {
            handleLoadPackageCatching(lpparam)
        } catch (throwable: Throwable) {
            mLogger.error("IXposedHookLoadPackageCatching:handleLoadPackage", throwable)
        }
    }

    fun handleLoadPackageCatching(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
    }

}
