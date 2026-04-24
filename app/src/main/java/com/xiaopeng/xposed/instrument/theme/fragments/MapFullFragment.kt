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
import android.content.res.Configuration
import android.content.res.XModuleResources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.core.view.postDelayed
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.xiaopeng.instrument.manager.SurfaceViewManager
import com.xiaopeng.instrument.view.BaseFragment
import com.xiaopeng.instrument.viewmodel.NaviViewModel
import com.xiaopeng.instrument.viewmodel.sr.SRInfoViewModel
import com.xiaopeng.instrument.viewmodel.sr.SRNaviViewModel
import com.xiaopeng.instrument.widget.CardMapSurfaceView
import com.xiaopeng.instrument.widget.NaviLaneInfoView
import com.xiaopeng.instrument.widget.sr.SRLeftInfoViewGroup
import com.xiaopeng.instrument.widget.sr.SRRightInfoViewGroup
import com.xiaopeng.libtheme.ThemeManager
import com.xiaopeng.xposed.instrument.theme.BuildConfig
import com.xiaopeng.xposed.instrument.theme.R
import com.xiaopeng.xposed.instrument.theme.XposedMan
import com.xiaopeng.xposed.instrument.theme.constants.ConstantSurfaceViewManager
import com.xiaopeng.xposed.instrument.theme.utils.LayoutInflaterXposed
import com.xiaopeng.xposed.instrument.theme.utils.LeftSubCardAutoSwitch
import com.xiaopeng.xui.widget.XImageView
import de.robv.android.xposed.XposedBridge

class MapFullFragment : BaseFragment() {

    private val mInfoViewModel: SRInfoViewModel by lazy { ViewModelProvider(requireActivity())[SRInfoViewModel::class.java] }
    private val mNaviViewModel: SRNaviViewModel by lazy { ViewModelProvider(requireActivity())[SRNaviViewModel::class.java] }
    private var mRawLeftSubCardIndex: Int? = null
    private var mIsNavigationActive: Boolean = false

    // @formatter:off
    private val mWidgetMapHeight: Int by lazy { ConstantSurfaceViewManager.SR_MAP_HEIGHT }
    private val mWidgetMapWidth : Int by lazy { ConstantSurfaceViewManager.SR_MAP_WIDTH  }

    private val mCardMapSurfaceView: CardMapSurfaceView   by lazy { requireView().findViewById(R.id.iv_map)          }
    private val mNaviLaneInfoView  : NaviLaneInfoView     by lazy { requireView().findViewById(R.id.navi_lane_info)  }
    private val mTopMaskImageView  : XImageView           by lazy { requireView().findViewById(R.id.iv_top_mask)     }
    private val mLeftInfoViewGroup : SRLeftInfoViewGroup  by lazy { requireView().findViewById(R.id.main_left_info)  }
    private val mRightInfoViewGroup: SRRightInfoViewGroup by lazy { requireView().findViewById(R.id.main_right_info) }
    // @formatter:on

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context: Context = inflater.context
        val moduleRes: XModuleResources = XModuleResources.createInstance(/* path = */ XposedMan.MODULE_PATH, /* origRes = */ null)

        val parser = moduleRes.getLayout(/* id = */ R.layout.fragment_map_full)
        return LayoutInflaterXposed.from(context).inflate( /* parser = */ parser, /* root = */ container, /* attachToRoot = */ false)
    }

    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)

        initNaviLaneInfoView()
        initLeftSubCardAutoSwitch()
        initObservers()

        this.mNaviLaneInfoView.setBackgroundResource(R.drawable.fragment_map_navi_bg_lane)

        view.postDelayed(delayInMillis = 500) {
            SurfaceViewManager.getInstance().srSurface = this.mCardMapSurfaceView.surface
            startChangeService(width = mWidgetMapWidth, height = mWidgetMapHeight, surface = mCardMapSurfaceView.surface)
        }
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

        if (viewModel == null) {
            return
        }
        if (mLifecycleOwner == null) {
            return
        }

        viewModel.naviLaneBgLiveData.observe(mLifecycleOwner) {
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
        // @formatter:off
        setLiveDataObserver(this.mInfoViewModel.leftSubCardLiveData   , observer { renderLeftSubCard(rawCardIndex = it)             })
        setLiveDataObserver(this.mInfoViewModel.rightSubCardLiveData  , observer { this.mRightInfoViewGroup.showSubCardView(it)      })
        setLiveDataObserver(this.mInfoViewModel.leftCardLiveData      , observer { this.mLeftInfoViewGroup .showCardView(it)         })
        setLiveDataObserver(this.mInfoViewModel.rightCardLiveData     , observer { this.mRightInfoViewGroup.showCardView(it)         })
        setLiveDataObserver(this.mInfoViewModel.leftListIndexLiveData , observer { this.mLeftInfoViewGroup .updateListHighIndex(it)  })
        setLiveDataObserver(this.mInfoViewModel.leftListLiveData      , observer { this.mLeftInfoViewGroup .updateListData(it)       })
        setLiveDataObserver(this.mInfoViewModel.leftListInfoLiveData  , observer { this.mLeftInfoViewGroup .showList(it)             })
        setLiveDataObserver(this.mInfoViewModel.rightListIndexLiveData, observer { this.mRightInfoViewGroup.updateListHighIndex(it)  })
        setLiveDataObserver(this.mInfoViewModel.rightListLiveData     , observer { this.mRightInfoViewGroup.updateListData(it)       })
        setLiveDataObserver(this.mInfoViewModel.rightListInfoLiveData , observer { this.mRightInfoViewGroup.showList(it)             })
        // @formatter:on
    }

    private fun initLeftSubCardAutoSwitch() {
        val naviTBtVisibility = mNaviViewModel.getNaviTBtVisibility()
        mIsNavigationActive = naviTBtVisibility.value == true
        setLiveDataObserver(naviTBtVisibility, observer<Boolean> { isNavigationActive ->
            mIsNavigationActive = isNavigationActive
            val rawCardIndex = mRawLeftSubCardIndex ?: return@observer
            renderLeftSubCard(rawCardIndex = rawCardIndex)
        })
    }

    private fun renderLeftSubCard(rawCardIndex: Int) {
        mRawLeftSubCardIndex = rawCardIndex
        val effectiveCardIndex = LeftSubCardAutoSwitch.resolve(rawCardIndex = rawCardIndex, isNavigationActive = mIsNavigationActive)
        mLeftInfoViewGroup.showSubCardView(effectiveCardIndex)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // val isisThemeChanged = ThemeManager.isThemeChanged(newConfig)
        // XposedBridge.log("onConfigurationChanged: $isisThemeChanged")
        // if (isisThemeChanged) {
        //     // val isNightMode: Boolean = ThemeManager.isNightMode(requireContext())
        //     mTopMaskImageView.setBackgroundResource(R.drawable.fragment_map_top_mask)
        // }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden.not()) {
            SurfaceViewManager.getInstance().srSurface = this.mCardMapSurfaceView.surface
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

    private fun <T> observer(block: (value: T) -> Unit): Observer<T> {
        return object : Observer<T> {
            override fun onChanged(value: T) {
                block(value)
            }
        }
    }

}
