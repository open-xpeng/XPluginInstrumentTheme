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

import org.joor.Reflect
import java.net.URL
import kotlin.collections.any
import kotlin.jvm.Throws
import kotlin.jvm.java
import kotlin.jvm.javaClass
import kotlin.text.startsWith

class HostClassLoader(
    private val mHostClassLoader: ClassLoader,
    private val mParentClassLoader: ClassLoader
) : ClassLoader() {

    companion object {
        fun injectClassLoader(hostClassLoader: ClassLoader) {
            val selfClassLoader: ClassLoader = requireNotNull(HostClassLoader::class.java.classLoader)
            val selfParentClassLoader: ClassLoader = Reflect.on(/* object = */ selfClassLoader).get("parent")

            if (selfParentClassLoader.javaClass != HostClassLoader::class.java) {
                val loader = HostClassLoader(mHostClassLoader = hostClassLoader, mParentClassLoader = selfParentClassLoader)
                Reflect.on(/* object = */ selfClassLoader).set(/* name = */ "parent", /* value = */ loader)
            }
        }
    }

    @Throws(exceptionClasses = [ClassNotFoundException::class])
    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        return runCatching { mHostClassLoader.loadClass(name) }.getOrNull()
               ?: mParentClassLoader.loadClass(name)
    }

    override fun getResource(name: String): URL? {
        return mParentClassLoader.getResource(name) ?: mHostClassLoader.getResource(name)
    }

}
