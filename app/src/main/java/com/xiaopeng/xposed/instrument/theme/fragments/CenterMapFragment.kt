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

package com.xiaopeng.xposed.instrument.theme.fragments

import android.content.Context
import android.content.Intent
import android.content.res.XModuleResources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import com.xiaopeng.instrument.view.BaseFragment
import com.xiaopeng.instrument.widget.CardMapSurfaceView
import com.xiaopeng.xposed.instrument.theme.R
import com.xiaopeng.xposed.instrument.theme.XposedMan
import com.xiaopeng.xposed.instrument.theme.constants.ConstantSurfaceViewManager
import com.xiaopeng.xposed.instrument.theme.utils.LayoutInflaterXposed
import de.robv.android.xposed.XposedBridge

class CenterMapFragment : BaseFragment() {

    private val mMapCardView: CardMapSurfaceView by lazy {
        requireView().findViewById(R.id.iv_map)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context: Context = inflater.context
        val moduleRes: XModuleResources = XModuleResources.createInstance(/* path = */ XposedMan.MODULE_PATH, /* origRes = */ null)

        val parser = moduleRes.getLayout(/* id = */ R.layout.fragment_center_map)
        return LayoutInflaterXposed.from(context).inflate( /* parser = */ parser, /* root = */ container, /* attachToRoot = */ false)
    }

    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)

        view.postDelayed({
            startChangeService(width = ConstantSurfaceViewManager.SR_MAP_WIDTH, height = ConstantSurfaceViewManager.SR_MAP_HEIGHT, surface = mMapCardView.surface)
        }, 1000)
    }

    private fun startChangeService(width: Int, height: Int, surface: Surface) {
        val intent = Intent()
        intent.setAction(ConstantSurfaceViewManager.ACTION_MAP_SURFACE_CHANGED)
        intent.putExtra(/* name = */ ConstantSurfaceViewManager.MAP_WIDTH, /* value = */ width)
        intent.putExtra(/* name = */ ConstantSurfaceViewManager.MAP_HEIGHT, /* value = */ height)
        intent.putExtra(/* name = */ ConstantSurfaceViewManager.MAP_SURFACE, /* value = */ surface)
        intent.setClassName(/* packageName = */ ConstantSurfaceViewManager.PACKAGE_NAME, /* className = */ ConstantSurfaceViewManager.CLASS_NAME)

        try {
            requireContext().startService(intent)
        } catch (t: Throwable) {
            XposedBridge.log(t)
        }
    }

}
