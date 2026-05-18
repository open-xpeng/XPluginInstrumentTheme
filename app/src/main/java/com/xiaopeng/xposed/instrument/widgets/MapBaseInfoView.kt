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

package com.xiaopeng.xposed.instrument.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.xiaopeng.instrument.animator.AnimatorHelper
import com.xiaopeng.instrument.animator.AnimatorType
import com.xiaopeng.instrument.animator.BaseViewAnimator
import com.xiaopeng.instrument.bean.InfoContainBean
import com.xiaopeng.instrument.widget.CardListAdapter
import com.xiaopeng.instrument.widget.CardPickerLayoutManager
import com.xiaopeng.xposed.instrument.theme.R
import com.xiaopeng.xui.widget.XRecyclerView
import com.xiaopeng.xui.widget.XRelativeLayout

abstract class MapBaseInfoView : XRelativeLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    abstract val layout: Int
    abstract val position: Int

    protected val mLogger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(this.javaClass.simpleName)

    private val mRecyclerView: XRecyclerView
    private val mAnimatorHelper: AnimatorHelper
    private val mListHideAnimator: BaseViewAnimator
    private val mListShowAnimator: BaseViewAnimator

    protected var mInfoContainBean: InfoContainBean? = null;
    protected val cardListAdapter: CardListAdapter
    protected val cardPickerLayoutManager: CardPickerLayoutManager

    init {
        val context = context
        inflate(/* context = */ context, /* resource = */ layout, /* root = */ this)

        this.mAnimatorHelper = AnimatorHelper();
        this.mListShowAnimator = AnimatorType.SlideInNormal.initAnimator();
        this.mListHideAnimator = AnimatorType.SlideOutScale.initAnimator();


        mRecyclerView = findViewById(R.id.info_list)
        cardListAdapter = CardListAdapter(/* context = */ context, /* xRecyclerView = */ mRecyclerView, /* i = */ position)
        mRecyclerView.adapter = cardListAdapter

        cardPickerLayoutManager = CardPickerLayoutManager(/* context = */ context, /* xRecyclerView = */ this.mRecyclerView, /* i = */ 1, /* z = */ false, /* i2 = */ 3, /* i3 = */ position)
    }


    fun showList(z: Boolean) {
        this.mRecyclerView.visibility = if (z) VISIBLE else GONE;
        this.mAnimatorHelper.showAnimator(z, this.mListShowAnimator, this.mListHideAnimator, this.mRecyclerView);
        if (mLogger.isDebugEnabled) {
            mLogger.debug("event=hook_value_applied targetMethod={} result={}", "showList", z)
        }
    }

    fun updateListData(infoContainBean: InfoContainBean?) {
        if (infoContainBean == null) {
            return
        }
        this.mInfoContainBean = infoContainBean;
        this.cardListAdapter.updateAllItem(/* list = */ infoContainBean.getInfoBeanList());
        this.cardPickerLayoutManager.scrollToTargetPosition(infoContainBean.infoBeanList.size, infoContainBean.selectIndex);
        if (mLogger.isDebugEnabled) {
            mLogger.debug(
                "event=hook_value_applied targetMethod={} result={} selectIndex={}",
                "updateListData",
                infoContainBean.infoBeanList.size,
                infoContainBean.selectIndex
            )
        }
    }

}
