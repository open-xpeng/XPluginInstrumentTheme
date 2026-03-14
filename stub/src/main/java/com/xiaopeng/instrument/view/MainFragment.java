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

package com.xiaopeng.instrument.view;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaopeng.MeterSD.MainSurfaceView;
import com.xiaopeng.instrument.widget.LeftInfoViewGroup;
import com.xiaopeng.instrument.widget.RightInfoViewGroup;
import com.xiaopeng.xui.widget.XImageView;

@SuppressWarnings("unused")
/* JADX INFO: loaded from: classes.dex */
public class MainFragment extends BaseFragment {
    private static final String TAG = "MainFragment";
    private LeftInfoViewGroup mLeftInfoViewGroup;
    OnMainFragmentListener mOnMainFragmentListener;
    private RightInfoViewGroup mRightInfoViewGroup;
    private MainSurfaceView mSurfaceView;
    private XImageView mTopMask;
    private int mCurrentGearType = 0;

    public interface OnMainFragmentListener {
        void onMainFragmentInit();
    }

    @Override // com.xiaopeng.instrument.view.BaseFragment, androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override // com.xiaopeng.instrument.view.BaseFragment, androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        throw new RuntimeException("Stub!");
    }

    @Override // com.xiaopeng.instrument.view.BaseFragment, androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        throw new RuntimeException("Stub!");
    }

    @Override // com.xiaopeng.instrument.view.BaseFragment, androidx.fragment.app.Fragment
    public void onStart() {
        super.onStart();
    }

    @Override // androidx.fragment.app.Fragment, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        throw new RuntimeException("Stub!");
    }

    protected void initView(View view) {
        throw new RuntimeException("Stub!");
    }

    private void initClusterInterface() {
        throw new RuntimeException("Stub!");
    }

    private void initViewModel() {
        throw new RuntimeException("Stub!");
    }

    private void initObservers() {
        throw new RuntimeException("Stub!");
    }

    protected void changeTheme() {
        throw new RuntimeException("Stub!");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void changeBackground(int i) {
        throw new RuntimeException("Stub!");
    }

    @Override // com.xiaopeng.instrument.view.BaseFragment, androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
        throw new RuntimeException("Stub!");
    }

    @Override // com.xiaopeng.instrument.view.BaseFragment, androidx.fragment.app.Fragment
    public void onPause() {
        super.onPause();
        throw new RuntimeException("Stub!");
    }

    @Override // com.xiaopeng.instrument.view.BaseFragment, androidx.fragment.app.Fragment
    public void onStop() {
        super.onStop();
    }

    @Override // androidx.fragment.app.Fragment
    public void onHiddenChanged(boolean z) {
        super.onHiddenChanged(z);
        throw new RuntimeException("Stub!");
    }

    @Override // com.xiaopeng.instrument.view.BaseFragment, androidx.fragment.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        throw new RuntimeException("Stub!");
    }

    public void setOnMainFragmentListener(OnMainFragmentListener onMainFragmentListener) {
        throw new RuntimeException("Stub!");
    }

    /* JADX INFO: Access modifiers changed from: private */
    private void updateLeftListHighPosition(int i) {
        throw new RuntimeException("Stub!");
    }

    /* JADX INFO: Access modifiers changed from: private */
    private void updateRightListHighPosition(int i) {
        throw new RuntimeException("Stub!");
    }

    /* JADX INFO: Access modifiers changed from: private */
    private void showRightListView(boolean z) {
        throw new RuntimeException("Stub!");
    }

    /* JADX INFO: Access modifiers changed from: private */
    private void showLeftListView(boolean z) {
        throw new RuntimeException("Stub!");
    }

    /* JADX INFO: Access modifiers changed from: private */
    private void showLeftCardView(boolean z) {
        throw new RuntimeException("Stub!");
    }

    /* JADX INFO: Access modifiers changed from: private */
    private void showRightCardView(boolean z) {
        throw new RuntimeException("Stub!");
    }

    /* JADX INFO: Access modifiers changed from: private */
    private void showLeftSubCardView(int i) {
        throw new RuntimeException("Stub!");
    }

    /* JADX INFO: Access modifiers changed from: private */
    private void showSubRightCardView(int i) {
        throw new RuntimeException("Stub!");
    }
}