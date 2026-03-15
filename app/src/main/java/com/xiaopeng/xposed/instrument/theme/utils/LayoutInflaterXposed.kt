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

package com.xiaopeng.xposed.instrument.theme.utils

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.xiaopeng.xposed.instrument.theme.XposedMan
import de.robv.android.xposed.XposedBridge
import org.joor.Reflect

object LayoutInflaterXposed {

    fun from(context: Context): LayoutInflater {
        val mixedInflater: LayoutInflater = LayoutInflater.from(context)
        val factory = Factory(classLoader = LayoutInflaterXposed::class.java.classLoader)
        Reflect.on(/* object = */ mixedInflater).set("mPrivateFactory", factory)
        return mixedInflater
    }

    private class Factory(private val classLoader: ClassLoader?) : LayoutInflater.Factory2 {
        override fun onCreateView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? {
            val view1: View? = try {
                Reflect.onClass(/* name = */ name, /* classLoader = */ classLoader).create(context, attrs).get<View>()
            } catch (e: Exception) {
                null
            }
            if (view1 != null) {
                return view1
            }

            val view2: View? = try {
                Reflect.onClass(/* name = */ name, /* classLoader = */ XposedMan.MODULE_CLASS_LOADER).create(context, attrs).get<View>()
            } catch (e: Exception) {
                null
            }
            if (view2 != null) {
                return view2
            }

            return null
        }

        override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
            return onCreateView(parent = null, name = name, context = context, attrs = attrs)
        }
    }

}
