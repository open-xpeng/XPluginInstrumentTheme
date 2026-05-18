package io.github.sollyu.xposed.hyper.hdh.commons.wrappers

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Member

internal object XposedHelpersWrapper {

    internal fun findClass(className: String, classLoader: ClassLoader?): Class<*> {
        return XposedHelpers.findClass(className, classLoader)
    }

    internal fun findMethodExact(clazz: Class<*>, methodName: String, vararg parameterTypes: Class<*>): java.lang.reflect.Method {
        return XposedHelpers.findMethodExact(clazz, methodName, *parameterTypes)
    }

    internal fun findMethodBestMatch(clazz: Class<*>, methodName: String, vararg parameterTypes: Class<*>): java.lang.reflect.Method {
        return XposedHelpers.findMethodBestMatch(clazz, methodName, *parameterTypes)
    }

    internal fun findAndHookMethod(className: String, classLoader: ClassLoader?, methodName: String, vararg parameterTypesAndCallback: Any): XC_MethodHook.Unhook {
        return XposedHelpers.findAndHookMethod(className, classLoader, methodName, *parameterTypesAndCallback)
    }

    internal fun findAndHookMethod(clazz: Class<*>, methodName: String, vararg parameterTypesAndCallback: Any): XC_MethodHook.Unhook {
        return XposedHelpers.findAndHookMethod(clazz, methodName, *parameterTypesAndCallback)
    }

    internal fun hookAllConstructors(clazz: Class<*>, callback: XC_MethodHook): Set<XC_MethodHook.Unhook> {
        return XposedBridge.hookAllConstructors(clazz, callback)
    }

    internal fun hookMethod(member: Member, callback: XC_MethodHook): XC_MethodHook.Unhook {
        return XposedBridge.hookMethod(member, callback)
    }

    internal fun setAdditionalInstanceField(instance: Any, key: String, value: Any?) {
        XposedHelpers.setAdditionalInstanceField(instance, key, value)
    }

    internal fun getAdditionalInstanceField(instance: Any, key: String): Any? {
        return XposedHelpers.getAdditionalInstanceField(instance, key)
    }

    internal fun removeAdditionalInstanceField(instance: Any, key: String): Any? {
        return XposedHelpers.removeAdditionalInstanceField(instance, key)
    }
}
