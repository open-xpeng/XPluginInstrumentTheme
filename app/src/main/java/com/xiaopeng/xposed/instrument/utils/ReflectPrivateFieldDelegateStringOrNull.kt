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

package com.xiaopeng.xposed.instrument.utils

import org.joor.Reflect
import kotlin.reflect.KProperty

class ReflectPrivateFieldDelegateStringOrNull(private val name: String) {

    private val mLogger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(this.javaClass.simpleName)

    private var mReflect: Reflect? = null
    private var mHasLoggedReadSuccess: Boolean = false
    private var mHasLoggedReadFailure: Boolean = false

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String? {
        if (mReflect == null) {
            mReflect = Reflect.on(/* object = */ thisRef)
        }
        if (mReflect?.get<Any?>() !== thisRef) {
            mReflect = Reflect.on(/* object = */ thisRef)
        }
        val reflect: Reflect = mReflect!!
        try {
            val value: String? = reflect.get(/* name = */ name)
            if (!mHasLoggedReadSuccess && mLogger.isInfoEnabled) {
                mLogger.info("event=reflect_field_read key={} result={}", name, value)
                mHasLoggedReadSuccess = true
            }
            return value
        } catch (throwable: Throwable) {
            if (!mHasLoggedReadFailure) {
                mLogger.error("event=reflect_field_read key={} result={}", name, "failed", throwable)
                mHasLoggedReadFailure = true
            }
            throw throwable
        }
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
        if (mReflect == null) {
            mReflect = Reflect.on(/* object = */ thisRef)
        }
        if (mReflect?.get<Any?>() !== thisRef) {
            mReflect = Reflect.on(/* object = */ thisRef)
        }
        val reflect: Reflect = mReflect!!
        reflect.set(/* name = */ name, /* value = */ value)
    }
}
