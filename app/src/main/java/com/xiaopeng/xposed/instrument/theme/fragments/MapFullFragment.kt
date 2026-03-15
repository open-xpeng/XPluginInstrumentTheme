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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.xiaopeng.instrument.bean.InfoContainBean
import com.xiaopeng.instrument.manager.SurfaceViewManager
import com.xiaopeng.instrument.view.BaseFragment
import com.xiaopeng.instrument.viewmodel.NaviViewModel
import com.xiaopeng.instrument.viewmodel.sr.SRInfoViewModel
import com.xiaopeng.instrument.widget.CardMapSurfaceView
import com.xiaopeng.instrument.widget.NaviLaneInfoView
import com.xiaopeng.instrument.widget.sr.SRLeftInfoViewGroup
import com.xiaopeng.instrument.widget.sr.SRRightInfoViewGroup
import com.xiaopeng.xposed.instrument.theme.BuildConfig
import com.xiaopeng.xposed.instrument.theme.R
import com.xiaopeng.xposed.instrument.theme.XposedMan
import com.xiaopeng.xposed.instrument.theme.constants.ConstantSurfaceViewManager
import com.xiaopeng.xposed.instrument.theme.utils.LayoutInflaterXposed
import de.robv.android.xposed.XposedBridge
import org.joor.Reflect

class MapFullFragment : BaseFragment() {

    private val mInfoViewModel: SRInfoViewModel by lazy {
        ViewModelProvider(requireActivity())[SRInfoViewModel::class.java]
    }

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

    private val mLeftInfoViewGroup: SRLeftInfoViewGroup by lazy {
        requireView().findViewById(R.id.main_left_info)
    }

    private val mRightInfoViewGroup: SRRightInfoViewGroup by lazy {
        requireView().findViewById(R.id.main_right_info)
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
        initObservers()

        this.mNaviLaneInfoView.setBackgroundResource(R.drawable.fragment_map_navi_bg_lane)
        view.postDelayed(/* action = */ { onResume() }, /* delayMillis = */ 500)
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
            this.mNaviLaneInfoView.setBackgroundResource(R.drawable.fragment_map_navi_bg_lane)
        }
        viewModel.naviTollGateLaneData.observe(mLifecycleOwner) { list ->
            this.mNaviLaneInfoView.updateTollGateData(list)
        }
        viewModel.naviNormalLaneData.observe(mLifecycleOwner) { array ->
            this.mNaviLaneInfoView.updateNormalLaneData(array)
        }
    }

    private fun initObservers() {
        setLiveDataObserver(this.mInfoViewModel.leftSubCardLiveData) {
            this.showLeftSubCardView(it)
        }
        setLiveDataObserver(this.mInfoViewModel.rightSubCardLiveData) {
            this.showSubRightCardView(it);
        }
        setLiveDataObserver(this.mInfoViewModel.leftCardLiveData) {
            this.showLeftCardView(it);
        };
        setLiveDataObserver(this.mInfoViewModel.rightCardLiveData) {
            this.showRightCardView(it);
        }
        setLiveDataObserver(this.mInfoViewModel.leftListIndexLiveData) {
            this.updateLeftListHighPosition(it);
        }
        setLiveDataObserver(this.mInfoViewModel.leftListLiveData) {
            this.updateLeftListData(it);
        }
        setLiveDataObserver(this.mInfoViewModel.leftListInfoLiveData) {
            this.showLeftListView(it);
        }
        setLiveDataObserver(this.mInfoViewModel.rightListIndexLiveData) {
            this.updateRightListHighPosition(it);
        }
        setLiveDataObserver(this.mInfoViewModel.rightListLiveData) {
            this.updateRightListData(it);
        }
        setLiveDataObserver(this.mInfoViewModel.rightListInfoLiveData) {
            this.showRightListView(it);
        }


    }

    override fun onResume() {
        super.onResume()
        SurfaceViewManager.getInstance().srSurface = this.mCardMapSurfaceView.surface
        startChangeService(width = mWidgetMapWidth, height = mWidgetMapHeight, surface = mCardMapSurfaceView.surface)
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

    fun showLeftListView(z: Boolean) {
        this.mLeftInfoViewGroup.showList(z)
    }

    fun showRightListView(z: Boolean) {
        this.mRightInfoViewGroup.showList(z)
    }

    fun showLeftCardView(z: Boolean) {
        this.mLeftInfoViewGroup.showCardView(z)
    }

    fun showRightCardView(z: Boolean) {
        this.mRightInfoViewGroup.showCardView(z)
    }

    fun updateLeftListData(infoContainBean: InfoContainBean) {
        this.mLeftInfoViewGroup.updateListData(infoContainBean)
    }

    fun updateRightListData(infoContainBean: InfoContainBean) {
        this.mRightInfoViewGroup.updateListData(infoContainBean)
    }

    fun updateLeftListHighPosition(i: Int) {
        this.mLeftInfoViewGroup.updateListHighIndex(i)
    }

    fun updateRightListHighPosition(i: Int) {
        this.mRightInfoViewGroup.updateListHighIndex(i)
    }

    fun showLeftSubCardView(i: Int) {
        this.mLeftInfoViewGroup.showSubCardView(i)
        XposedBridge.log("showLeftSubCardView: $i")
        if (i == 0) {
            this.mLeftInfoViewGroup.postDelayed({ onResume() }, 2000)
        }

    }

    fun showSubRightCardView(i: Int) {
        this.mRightInfoViewGroup.showSubCardView(i)
        if (i == 0) {
            this.mRightInfoViewGroup.postDelayed({ onResume() }, 200)
        }
    }


}
