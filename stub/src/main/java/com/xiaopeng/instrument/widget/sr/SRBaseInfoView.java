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

package com.xiaopeng.instrument.widget.sr;

import android.content.Context;
import android.util.AttributeSet;

import com.xiaopeng.instrument.bean.InfoContainBean;
import com.xiaopeng.xui.widget.XRelativeLayout;

/* JADX INFO: loaded from: classes.dex */
public abstract class SRBaseInfoView extends XRelativeLayout {

    public Context mSRContext;

    abstract int getLayout();

    abstract int getPosition();

    public SRBaseInfoView(Context context) {
        this(context, null);
    }

    public SRBaseInfoView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        throw new RuntimeException("Stub!");
    }

    public SRBaseInfoView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        throw new RuntimeException("Stub!");
    }

    @Override // com.xiaopeng.xui.widget.XRelativeLayout, android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        throw new RuntimeException("Stub!");
    }

    public void init(Context context) {
        throw new RuntimeException("Stub!");
    }

    private void initAnimator() {
        throw new RuntimeException("Stub!");
    }

    protected void showMapCarView() {
        throw new RuntimeException("Stub!");
    }

    protected void showMediaCarView() {
        throw new RuntimeException("Stub!");
    }

    protected void showCarConditionCarView() {
        throw new RuntimeException("Stub!");
    }

    protected void showPowerConsumptionCarView() {
        throw new RuntimeException("Stub!");
    }

    protected void showOdoMeterCarView() {
        throw new RuntimeException("Stub!");
    }

    protected void hideFixedCardView() {
        throw new RuntimeException("Stub!");
    }

    public void showSubCardView(int i) {
        throw new RuntimeException("Stub!");
    }

    public void showCardView(boolean z) {
        throw new RuntimeException("Stub!");
    }

    public void showList(boolean z) {
        throw new RuntimeException("Stub!");
    }

    public void updateListHighIndex(int i) {
        throw new RuntimeException("Stub!");
    }

    public void updateListData(InfoContainBean infoContainBean) {
        throw new RuntimeException("Stub!");
    }
}