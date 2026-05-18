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

package com.xiaopeng.xposed.instrument.constants

import com.xiaopeng.instrument.manager.SurfaceViewManager
import org.joor.Reflect

object ConstantSurfaceViewManager {

    private val mLogger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(this.javaClass.simpleName)

    private val mReflectSurfaceViewManager: Reflect by lazy {
        Reflect.onClass(/* clazz = */ SurfaceViewManager::class.java)
    }

    val PACKAGE_NAME: String by lazy {
        val value: String = mReflectSurfaceViewManager.get(/* name = */ "PACKAGE_NAME")
        if (mLogger.isInfoEnabled) {
            mLogger.info("event=reflect_value_initialized key={} value={}", "PACKAGE_NAME", value)
        }
        value
    }
    val CLASS_NAME: String by lazy {
        val value: String = mReflectSurfaceViewManager.get(/* name = */ "CLASS_NAME")
        if (mLogger.isInfoEnabled) {
            mLogger.info("event=reflect_value_initialized key={} value={}", "CLASS_NAME", value)
        }
        value
    }

    val ACTION_MAP_SURFACE_CHANGED: String by lazy {
        val value: String = mReflectSurfaceViewManager.get(/* name = */ "ACTION_MAP_SURFACE_CHANGED")
        if (mLogger.isInfoEnabled) {
            mLogger.info("event=reflect_value_initialized key={} value={}", "ACTION_MAP_SURFACE_CHANGED", value)
        }
        value
    }
    val ACTION_MAP_SURFACE_CREATE: String by lazy {
        val value: String = mReflectSurfaceViewManager.get(/* name = */ "ACTION_MAP_SURFACE_CREATE")
        if (mLogger.isInfoEnabled) {
            mLogger.info("event=reflect_value_initialized key={} value={}", "ACTION_MAP_SURFACE_CREATE", value)
        }
        value
    }
    val ACTION_MAP_SURFACE_DESTROY: String by lazy {
        val value: String = mReflectSurfaceViewManager.get(/* name = */ "ACTION_MAP_SURFACE_DESTROY")
        if (mLogger.isInfoEnabled) {
            mLogger.info("event=reflect_value_initialized key={} value={}", "ACTION_MAP_SURFACE_DESTROY", value)
        }
        value
    }

    val MAP_HEIGHT: String by lazy {
        val value: String = mReflectSurfaceViewManager.get(/* name = */ "MAP_HEIGHT")
        if (mLogger.isInfoEnabled) {
            mLogger.info("event=reflect_value_initialized key={} value={}", "MAP_HEIGHT", value)
        }
        value
    }
    val MAP_SURFACE: String by lazy {
        val value: String = mReflectSurfaceViewManager.get(/* name = */ "MAP_SURFACE")
        if (mLogger.isInfoEnabled) {
            mLogger.info("event=reflect_value_initialized key={} value={}", "MAP_SURFACE", value)
        }
        value
    }
    val MAP_WIDTH: String by lazy {
        val value: String = mReflectSurfaceViewManager.get(/* name = */ "MAP_WIDTH")
        if (mLogger.isInfoEnabled) {
            mLogger.info("event=reflect_value_initialized key={} value={}", "MAP_WIDTH", value)
        }
        value
    }

    val SD_MAP_HEIGHT: Int by lazy {
        val value: Int = mReflectSurfaceViewManager.get(/* name = */ "SD_MAP_HEIGHT")
        if (mLogger.isInfoEnabled) {
            mLogger.info("event=reflect_value_initialized key={} value={}", "SD_MAP_HEIGHT", value)
        }
        value
    }
    val SD_MAP_WIDTH: Int by lazy {
        val value: Int = mReflectSurfaceViewManager.get(/* name = */ "SD_MAP_WIDTH")
        if (mLogger.isInfoEnabled) {
            mLogger.info("event=reflect_value_initialized key={} value={}", "SD_MAP_WIDTH", value)
        }
        value
    }
    val SR_MAP_HEIGHT: Int by lazy {
        val value: Int = mReflectSurfaceViewManager.get(/* name = */ "SR_MAP_HEIGHT")
        if (mLogger.isInfoEnabled) {
            mLogger.info("event=reflect_value_initialized key={} value={}", "SR_MAP_HEIGHT", value)
        }
        value
    }
    val SR_MAP_WIDTH: Int by lazy {
        val value: Int = mReflectSurfaceViewManager.get(/* name = */ "SR_MAP_WIDTH")
        if (mLogger.isInfoEnabled) {
            mLogger.info("event=reflect_value_initialized key={} value={}", "SR_MAP_WIDTH", value)
        }
        value
    }

}
