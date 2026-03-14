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

package com.xiaopeng.instrument.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceView;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.xiaopeng.xui.widget.XImageView;
import com.xiaopeng.xui.widget.XLinearLayout;
import com.xiaopeng.xui.widget.XRelativeLayout;
import com.xiaopeng.xui.widget.XTextView;

import org.jspecify.annotations.NonNull;

import java.util.List;

@SuppressWarnings("unused")
/* JADX INFO: loaded from: classes.dex */
public class MapCardView extends XRelativeLayout implements IBaseCustomView {
    public static final String MANEUVER_RESOURCE_PREFIX = "navi_maneuver_ic_";
    private static int mCrossBgType;
    private static int mGearType;
    private final String TAG;

    private XImageView mIvCrossDirection;
    private XImageView mIvMap;
    private LifecycleOwner mLifecycleOwner;
    private XRelativeLayout mNavCrossBg;
    private XLinearLayout mNavTbtLayout;
    private XImageView mNaviMask;
    private XTextView mTvCrossDistance;
    private XTextView mTvCrossDistanceUnit;
    private XTextView mTvCrossRoadName;

    public MapCardView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        throw new RuntimeException("Stub!");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showNavCrossBg(int i) {
        throw new RuntimeException("Stub!");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showNavManeuver(Bitmap bitmap) {
        throw new RuntimeException("Stub!");
    }

    protected int getLayout() {
        throw new RuntimeException("Stub!");
    }

    private void initNaviSRMode() {
        throw new RuntimeException("Stub!");
    }

    private void initNormalMode() {
        throw new RuntimeException("Stub!");
    }

    public Surface getSurface() {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    public SurfaceView getSurfaceView() {
        throw new RuntimeException("Stub!");
    }

    private void initContentView() {
        throw new RuntimeException("Stub!");
    }

    private void initViewModel() {
        throw new RuntimeException("Stub!");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showNavMap(Bitmap bitmap) {
        throw new RuntimeException("Stub!");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showNaviTbt(boolean z) {
        throw new RuntimeException("Stub!");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showNaviCrossGuidence(boolean z) {
        throw new RuntimeException("Stub!");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showNaviLaneBg(boolean z) {
        throw new RuntimeException("Stub!");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showCrossDistanceUnit(Integer num) {
        throw new RuntimeException("Stub!");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showCrossDistance(String str) {
        throw new RuntimeException("Stub!");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showCrossRoadName(String str) {
        throw new RuntimeException("Stub!");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showTollGateLane(List<Integer> list) {
        throw new RuntimeException("Stub!");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showNavNormal(int[][] iArr) {
        throw new RuntimeException("Stub!");
    }

    @Override // com.xiaopeng.xui.widget.XRelativeLayout, android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        throw new RuntimeException("Stub!");
    }

    private void changeTheme(int i) {
        throw new RuntimeException("Stub!");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setGearType(int i) {
        throw new RuntimeException("Stub!");
    }

    @Override // com.xiaopeng.xui.widget.XRelativeLayout, android.view.View
    public void setVisibility(int i) {
        throw new RuntimeException("Stub!");
    }

    private void changeBackground() {
        throw new RuntimeException("Stub!");
    }

    protected void setMapPos(int i) {
        throw new RuntimeException("Stub!");
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.xiaopeng.instrument.widget.IBaseCustomView
    public <T> void setLiveDataObserver(LiveData<T> liveData, Observer<T> observer) {
        throw new RuntimeException("Stub!");
    }
}