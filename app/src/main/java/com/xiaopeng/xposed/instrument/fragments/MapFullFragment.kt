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

package com.xiaopeng.xposed.instrument.fragments

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
import com.xiaopeng.xposed.instrument.theme.BuildConfig
import com.xiaopeng.xposed.instrument.theme.R
import com.xiaopeng.xposed.instrument.XposedMain
import com.xiaopeng.xposed.instrument.constants.ConstantSurfaceViewManager
import com.xiaopeng.xposed.instrument.utils.LayoutInflaterXposed
import com.xiaopeng.xposed.instrument.utils.LeftSubCardAutoSwitch
import com.xiaopeng.xui.widget.XImageView

class MapFullFragment : BaseFragment() {

    private val mLogger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(this.javaClass.simpleName)
    private val mInfoViewModel: SRInfoViewModel by lazy { ViewModelProvider(requireActivity())[SRInfoViewModel::class.java] }
    private val mNaviViewModel: SRNaviViewModel by lazy { ViewModelProvider(requireActivity())[SRNaviViewModel::class.java] }
    private var mRawLeftSubCardIndex: Int? = null
    private var mIsTurnGuidanceVisible: Boolean = false
    private var mIsTbtVisible: Boolean = false
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
        val moduleRes: XModuleResources = XModuleResources.createInstance(/* path = */ XposedMain.MODULE_PATH, /* origRes = */ null)

        val parser = moduleRes.getLayout(/* id = */ R.layout.fragment_map_full)
        return LayoutInflaterXposed.from(context).inflate( /* parser = */ parser, /* root = */ container, /* attachToRoot = */ false)
    }

    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)

        initNaviLaneInfoView()
        initLeftSubCardAutoSwitch()
        initObservers()

        if (mLogger.isInfoEnabled) {
            mLogger.info(
                "event=fragment_view_created fragment={} width={} height={}",
                this.javaClass.simpleName,
                mWidgetMapWidth,
                mWidgetMapHeight
            )
        }

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
        val naviGuidenceVisibility = mNaviViewModel.getNaviGuidenceVisibility()
        val naviTBtVisibility = mNaviViewModel.getNaviTBtVisibility()
        mIsTurnGuidanceVisible = naviGuidenceVisibility.value == true
        mIsTbtVisible = naviTBtVisibility.value == true
        mIsNavigationActive = resolveNavigationCardActive()

        if (mLogger.isInfoEnabled) {
            mLogger.info(
                "event=hook_state_initialized fragment={} turnGuidanceVisible={} tbtVisible={} navActive={}",
                this.javaClass.simpleName,
                mIsTurnGuidanceVisible,
                mIsTbtVisible,
                mIsNavigationActive
            )
        }

        setLiveDataObserver(naviGuidenceVisibility, navigationObserver { isTurnGuidanceVisible ->
            mIsTurnGuidanceVisible = isTurnGuidanceVisible
        })
        setLiveDataObserver(naviTBtVisibility, navigationObserver { isTbtVisible ->
            mIsTbtVisible = isTbtVisible
        })
    }

    private fun resolveNavigationCardActive(): Boolean {
        return LeftSubCardAutoSwitch.isNavigationActive(
            isTurnGuidanceVisible = mIsTurnGuidanceVisible,
            isTbtVisible = mIsTbtVisible,
        )
    }

    private fun updateNavigationCardState(): Boolean {
        val previous = mIsNavigationActive
        mIsNavigationActive = resolveNavigationCardActive()
        return previous != mIsNavigationActive
    }

    private fun renderLeftSubCard(rawCardIndex: Int) {
        mRawLeftSubCardIndex = rawCardIndex
        val effectiveCardIndex = LeftSubCardAutoSwitch.resolve(rawCardIndex = rawCardIndex, isNavigationActive = mIsNavigationActive)
        if (mLogger.isDebugEnabled) {
            mLogger.debug(
                "event=card_render_resolved fragment={} rawValue={} effectiveValue={} navActive={}",
                this.javaClass.simpleName,
                rawCardIndex,
                effectiveCardIndex,
                mIsNavigationActive
            )
        }
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
        if (mLogger.isInfoEnabled) {
            mLogger.info(
                "event=fragment_hidden_changed fragment={} hidden={} rawValue={}",
                this.javaClass.simpleName,
                hidden,
                mRawLeftSubCardIndex
            )
        }
        if (hidden) {
            return
        }

        val rawCardIndex = mRawLeftSubCardIndex
        if (rawCardIndex != null) {
            renderLeftSubCard(rawCardIndex = rawCardIndex)
        }
        SurfaceViewManager.getInstance().srSurface = this.mCardMapSurfaceView.surface
        startChangeService(width = mWidgetMapWidth, height = mWidgetMapHeight, surface = mCardMapSurfaceView.surface)
    }

    private fun startChangeService(width: Int, height: Int, surface: Surface) {
        if (BuildConfig.IS_RUNNING_TEST_PLATFORM) {
            if (mLogger.isInfoEnabled) {
                mLogger.info(
                    "event=surface_change_service_skipped fragment={} reason={} width={} height={}",
                    this.javaClass.simpleName,
                    "running_test_platform",
                    width,
                    height
                )
            }
            return
        }

        val intent = Intent()
        intent.setAction(ConstantSurfaceViewManager.ACTION_MAP_SURFACE_CHANGED)
        intent.putExtra(/* name = */ ConstantSurfaceViewManager.MAP_WIDTH, /* value = */ width)
        intent.putExtra(/* name = */ ConstantSurfaceViewManager.MAP_HEIGHT, /* value = */ height)
        intent.putExtra(/* name = */ ConstantSurfaceViewManager.MAP_SURFACE, /* value = */ surface)
        intent.setClassName(/* packageName = */ ConstantSurfaceViewManager.PACKAGE_NAME, /* className = */ ConstantSurfaceViewManager.CLASS_NAME)

        if (mLogger.isInfoEnabled) {
            mLogger.info(
                "event=surface_change_service_start fragment={} width={} height={} action={}",
                this.javaClass.simpleName,
                width,
                height,
                ConstantSurfaceViewManager.ACTION_MAP_SURFACE_CHANGED
            )
        }

        try {
            requireContext().startService(intent)
            if (mLogger.isInfoEnabled) {
                mLogger.info(
                    "event=surface_change_service_completed fragment={} width={} height={}",
                    this.javaClass.simpleName,
                    width,
                    height
                )
            }
        } catch (t: Throwable) {
            mLogger.error("MapFullFragment:startChangeService", t)
        }
    }

    private fun <T> observer(block: (value: T) -> Unit): Observer<T> {
        return object : Observer<T> {
            override fun onChanged(value: T) {
                if (isHidden) {
                    if (mLogger.isDebugEnabled) {
                        mLogger.debug(
                            "event=observer_skipped fragment={} reason={} valueClass={}",
                            this@MapFullFragment.javaClass.simpleName,
                            "fragment_hidden",
                            value?.javaClass?.simpleName
                        )
                    }
                    return
                }
                block(value)
            }
        }
    }

    private fun navigationObserver(block: (value: Boolean) -> Unit): Observer<Boolean> {
        return object : Observer<Boolean> {
            override fun onChanged(value: Boolean) {
                block(value)
                val previousNavigationActive = mIsNavigationActive
                if (!updateNavigationCardState()) {
                    if (mLogger.isDebugEnabled) {
                        mLogger.debug(
                            "event=observer_skipped fragment={} reason={} newValue={} navActive={}",
                            this@MapFullFragment.javaClass.simpleName,
                            "navigation_state_unchanged",
                            value,
                            mIsNavigationActive
                        )
                    }
                    return
                }
                if (mLogger.isInfoEnabled) {
                    mLogger.info(
                        "event=hook_state_changed fragment={} previousValue={} newValue={} turnGuidanceVisible={} tbtVisible={}",
                        this@MapFullFragment.javaClass.simpleName,
                        previousNavigationActive,
                        mIsNavigationActive,
                        mIsTurnGuidanceVisible,
                        mIsTbtVisible
                    )
                }
                if (isHidden) {
                    if (mLogger.isDebugEnabled) {
                        mLogger.debug(
                            "event=observer_skipped fragment={} reason={} navActive={}",
                            this@MapFullFragment.javaClass.simpleName,
                            "fragment_hidden",
                            mIsNavigationActive
                        )
                    }
                    return
                }

                val rawCardIndex = mRawLeftSubCardIndex ?: run {
                    if (mLogger.isDebugEnabled) {
                        mLogger.debug(
                            "event=observer_skipped fragment={} reason={} navActive={}",
                            this@MapFullFragment.javaClass.simpleName,
                            "left_sub_card_uninitialized",
                            mIsNavigationActive
                        )
                    }
                    return
                }
                renderLeftSubCard(rawCardIndex = rawCardIndex)
            }
        }
    }

}
