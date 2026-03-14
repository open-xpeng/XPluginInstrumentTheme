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

package com.xiaopeng.instrument.manager;

import android.content.Context;
import android.content.Intent;
import android.view.Surface;

import com.xiaopeng.instrument.widget.MapCardView;

@SuppressWarnings("unused")
/* JADX INFO: loaded from: classes.dex */
public class SurfaceViewManager {
    private static final String ACTION_MAP_SURFACE_CHANGED = "com.xiaopeng.montecarlo.minimap.ACTION_MAP_SURFACE_CHANGED";
    private static final String ACTION_MAP_SURFACE_CREATE = "com.xiaopeng.montecarlo.minimap.ACTION_MAP_SURFACE_CREATE";
    private static final String ACTION_MAP_SURFACE_DESTROY = "com.xiaopeng.montecarlo.minimap.ACTION_MAP_SURFACE_DESTROY";
    private static final String CLASS_NAME = "com.xiaopeng.montecarlo.service.minimap.MiniMapService";
    private static final String MAP_HEIGHT = "map_height";
    private static final String MAP_SURFACE = "map_surface";
    private static final String MAP_WIDTH = "map_width";
    private static final int MAX_RETRY_TIME = 10;
    private static final int MSG_WHAT_SD_LEFT = 1;
    private static final int MSG_WHAT_SD_RIGHT = 2;
    private static final int MSG_WHAT_SR = 3;
    private static final String PACKAGE_NAME = "com.xiaopeng.montecarlo";
    private static final int RETRY_TIME_INTERVAL = 200;
    private static final int SD_MAP_HEIGHT = 660;
    private static final int SD_MAP_WIDTH = 660;
    private static final int SR_MAP_HEIGHT = 720;
    private static final int SR_MAP_WIDTH = 1920;
    private static final String TAG = "SurfaceViewManager";
    private static Intent mCreateIntent;
    private static Intent mDestroyIntent;
    private Context mContext;
    private int mCurrentMapType;
    private boolean mIsSRMode;
    private MapCardView mLeftMapCardView;
    private Surface mLeftSDSurface;
    private int mLeftViewType;
    private MapCardView mRightMapCardView;
    private Surface mRightSDSurface;
    private int mRightViewType;
    private int mSDLeftRetryTime;
    private int mSDRightRetryTime;
    private int mSRRetryTime;
    private Surface mSRSurface;

    public int getCurrentMapType() {
        throw new RuntimeException("Stub!");
    }

    private SurfaceViewManager() {
        this.mLeftViewType = 0;
        this.mRightViewType = 0;
        this.mCurrentMapType = 0;
        init();
    }

    public static SurfaceViewManager getInstance() {
        throw new RuntimeException("Stub!");
    }

    public int getLeftViewType() {
        throw new RuntimeException("Stub!");
    }

    public void setLeftViewType(int i) {
        throw new RuntimeException("Stub!");
    }

    public int getRightViewType() {
        throw new RuntimeException("Stub!");
    }

    public void setRightViewType(int i) {
        throw new RuntimeException("Stub!");
    }

    public Surface getSRSurface() {
        throw new RuntimeException("Stub!");
    }

    public void setIsSRMode(boolean z) {
        throw new RuntimeException("Stub!");
    }

    public void setSRSurface(Surface surface) {
        throw new RuntimeException("Stub!");
    }

    public void setLeftSDSurface(MapCardView mapCardView) {
        throw new RuntimeException("Stub!");
    }

    public void setRightSDSurface(MapCardView mapCardView) {
        throw new RuntimeException("Stub!");
    }

    private void init() {
        throw new RuntimeException("Stub!");
    }

    public void startCreateService() {
        throw new RuntimeException("Stub!");
    }

    public void startDestroyService() {
        throw new RuntimeException("Stub!");
    }

    private void startChangeService(int i, int i2, Surface surface) {
        throw new RuntimeException("Stub!");
    }

    public void startSRChangeService() {
        throw new RuntimeException("Stub!");
    }

    public void startLeftSDChangeService() {
        throw new RuntimeException("Stub!");
    }

    public void startRightSDChangeService() {
        throw new RuntimeException("Stub!");
    }

    private void retryAndEndStartService(int i, int i2, MapCardView mapCardView) {
        throw new RuntimeException("Stub!");
    }

    private void refreshMapView(MapCardView mapCardView) {
        throw new RuntimeException("Stub!");
    }

    public void removeOtherMsg(int i) {
        throw new RuntimeException("Stub!");
    }

    public void resumeMainMap() {
        throw new RuntimeException("Stub!");
    }
}