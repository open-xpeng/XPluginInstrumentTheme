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

    private val mReflectSurfaceViewManager: Reflect by lazy {
        Reflect.onClass(/* clazz = */ SurfaceViewManager::class.java)
    }

    val PACKAGE_NAME: String by lazy { mReflectSurfaceViewManager.get(/* name = */ "PACKAGE_NAME"   ) }
    val CLASS_NAME  : String by lazy { mReflectSurfaceViewManager.get(/* name = */ "CLASS_NAME"     ) }

    val ACTION_MAP_SURFACE_CHANGED  : String by lazy { mReflectSurfaceViewManager.get(/* name = */ "ACTION_MAP_SURFACE_CHANGED" ) }
    val ACTION_MAP_SURFACE_CREATE   : String by lazy { mReflectSurfaceViewManager.get(/* name = */ "ACTION_MAP_SURFACE_CREATE"  ) }
    val ACTION_MAP_SURFACE_DESTROY  : String by lazy { mReflectSurfaceViewManager.get(/* name = */ "ACTION_MAP_SURFACE_DESTROY" ) }

    val MAP_HEIGHT  : String by lazy { mReflectSurfaceViewManager.get(/* name = */ "MAP_HEIGHT" ) }
    val MAP_SURFACE : String by lazy { mReflectSurfaceViewManager.get(/* name = */ "MAP_SURFACE") }
    val MAP_WIDTH   : String by lazy { mReflectSurfaceViewManager.get(/* name = */ "MAP_WIDTH"  ) }

    val SD_MAP_HEIGHT   : Int by lazy { mReflectSurfaceViewManager.get(/* name = */ "SD_MAP_HEIGHT" ) }
    val SD_MAP_WIDTH    : Int by lazy { mReflectSurfaceViewManager.get(/* name = */ "SD_MAP_WIDTH"  ) }
    val SR_MAP_HEIGHT   : Int by lazy { mReflectSurfaceViewManager.get(/* name = */ "SR_MAP_HEIGHT" ) }
    val SR_MAP_WIDTH    : Int by lazy { mReflectSurfaceViewManager.get(/* name = */ "SR_MAP_WIDTH"  ) }

}
