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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.xiaopeng.instrument.manager.SurfaceViewManager
import com.xiaopeng.instrument.view.BaseFragment
import com.xiaopeng.instrument.viewmodel.NaviViewModel
import com.xiaopeng.instrument.widget.CardMapSurfaceView
import com.xiaopeng.instrument.widget.NaviLaneInfoView
import com.xiaopeng.xposed.instrument.theme.BuildConfig
import com.xiaopeng.xposed.instrument.theme.R
import com.xiaopeng.xposed.instrument.theme.XposedMan
import com.xiaopeng.xposed.instrument.theme.constants.ConstantSurfaceViewManager
import com.xiaopeng.xposed.instrument.theme.utils.LayoutInflaterXposed
import de.robv.android.xposed.XposedBridge
import org.joor.Reflect

class MapFullFragment : BaseFragment() {

    private val mWidgetMapHeight: Int by lazy {
        ConstantSurfaceViewManager.SR_MAP_HEIGHT
    }

    private val mWidgetMapWidth: Int by lazy {
        ConstantSurfaceViewManager.SR_MAP_WIDTH
    }

    private val mCardMapSurfaceView: CardMapSurfaceView by lazy {
        requireView().findViewById(R.id.iv_map)
    }

    private val mNaviLaneInfoView: NaviLaneInfoView by lazy {
        requireView().findViewById(R.id.navi_lane_info)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context: Context = inflater.context
        val moduleRes: XModuleResources = XModuleResources.createInstance(/* path = */ XposedMan.MODULE_PATH, /* origRes = */ null)

        val parser = moduleRes.getLayout(/* id = */ R.layout.fragment_map_full)
        return LayoutInflaterXposed.from(context).inflate( /* parser = */ parser, /* root = */ container, /* attachToRoot = */ false)
    }

    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)

        initNaviLaneInfoView()

        view.postDelayed(/* action = */ {
            startChangeService(width = mWidgetMapWidth, height = mWidgetMapHeight, surface = mCardMapSurfaceView.surface)
        }, /* delayMillis = */ 500)
    }

    private fun initNaviLaneInfoView() {
        val context: Context = requireContext()
        val mLifecycleOwner: LifecycleOwner? = when (context) {
            is LifecycleOwner -> context as LifecycleOwner
            else              -> null
        }

        val viewModel: NaviViewModel? = when (context) {
            is ViewModelStoreOwner -> ViewModelProvider(owner = context)[NaviViewModel::class.java]
            else                   -> null
        }

        // val surfaceViewManager = SurfaceViewManager.getInstance()
        // val viewModel: NaviViewModel? = Reflect.on(surfaceViewManager).field("mLeftMapCardView").get("mViewModel")
        XposedBridge.log("initNaviLaneInfoView: $viewModel")
        XposedBridge.log("initNaviLaneInfoView: $mLifecycleOwner")

        if (viewModel == null) {
            return
        }
        if (mLifecycleOwner == null) {
            return
        }

        viewModel.naviLaneBgLiveData.observe(mLifecycleOwner) {
            this.mNaviLaneInfoView.updateLaneBg(it)
        }
        viewModel.naviTollGateLaneData.observe(mLifecycleOwner) { list ->
            this.mNaviLaneInfoView.updateTollGateData(list)
        }
        viewModel.naviNormalLaneData.observe(mLifecycleOwner) { array ->
            this.mNaviLaneInfoView.updateNormalLaneData(array)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden.not()) {
            startChangeService(width = mWidgetMapWidth, height = mWidgetMapHeight, surface = mCardMapSurfaceView.surface)
        }
    }

    private fun startChangeService(width: Int, height: Int, surface: Surface) {
        val intent = Intent()
        intent.setAction(ConstantSurfaceViewManager.ACTION_MAP_SURFACE_CHANGED)
        intent.putExtra(/* name = */ ConstantSurfaceViewManager.MAP_WIDTH, /* value = */ width)
        intent.putExtra(/* name = */ ConstantSurfaceViewManager.MAP_HEIGHT, /* value = */ height)
        intent.putExtra(/* name = */ ConstantSurfaceViewManager.MAP_SURFACE, /* value = */ surface)
        intent.setClassName(/* packageName = */ ConstantSurfaceViewManager.PACKAGE_NAME, /* className = */ ConstantSurfaceViewManager.CLASS_NAME)

        if (BuildConfig.IS_RUNNING_TEST_PLATFORM) {
            return
        }

        try {
            requireContext().startService(intent)
        } catch (t: Throwable) {
            XposedBridge.log(t)
        }
    }

}
