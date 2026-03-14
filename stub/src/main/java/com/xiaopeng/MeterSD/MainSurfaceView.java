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

package com.xiaopeng.MeterSD;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.widget.CheckBox;

import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

@SuppressWarnings("unused")
/* JADX INFO: loaded from: classes.dex */
public class MainSurfaceView extends GLSurfaceView {
    private static final int ONEGG_TOUCH_COUNT = 8;
    private static final String TAG = "metersd";
    private boolean mIsDrawing;
    private NaviSRCallback mNaviSRCallback;
    private MainRenderer mRenderer;
    private boolean mStopDraw;
    public int mSurfaceHeight;
    public int mSurfaceWidth;
    private List<Long> mTouchTime;

    public interface NaviSRCallback {
        void onInitedOpenGL();
    }

    public MainSurfaceView(Context context) {
        super(context);
        throw new RuntimeException("Stub!");
    }

    public MainSurfaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        throw new RuntimeException("Stub!");
    }

    public void init(boolean z, NaviSRCallback naviSRCallback) {
        throw new RuntimeException("Stub!");
    }

    public void unInit() {
        throw new RuntimeException("Stub!");
    }

    @Override // android.opengl.GLSurfaceView, android.view.SurfaceHolder.Callback
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        super.surfaceCreated(surfaceHolder);
        throw new RuntimeException("Stub!");
    }

    @Override // android.opengl.GLSurfaceView, android.view.SurfaceHolder.Callback
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        super.surfaceDestroyed(surfaceHolder);
        throw new RuntimeException("Stub!");
    }

    public class MainRenderer implements GLSurfaceView.Renderer {
        public MainRenderer() {
        }

        @Override // android.opengl.GLSurfaceView.Renderer
        public void onSurfaceCreated(GL10 gl10, EGLConfig eGLConfig) {
            throw new RuntimeException("Stub!");
        }

        @Override // android.opengl.GLSurfaceView.Renderer
        public void onSurfaceChanged(GL10 gl10, int i, int i2) {
            throw new RuntimeException("Stub!");
        }

        @Override // android.opengl.GLSurfaceView.Renderer
        public void onDrawFrame(GL10 gl10) {
            throw new RuntimeException("Stub!");
        }
    }

    private Bitmap getBitmap(Context context, int i) {
        throw new RuntimeException("Stub!");
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        throw new RuntimeException("Stub!");
    }

    private void showSettings() {
        throw new RuntimeException("Stub!");
    }

    public void setFillFrameFPS(int i, CheckBox checkBox) {
        throw new RuntimeException("Stub!");
    }

    public void showDebugString(boolean z) {
        throw new RuntimeException("Stub!");
    }

}