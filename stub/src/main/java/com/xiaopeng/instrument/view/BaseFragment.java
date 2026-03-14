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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

@SuppressWarnings("unused")
/* JADX INFO: loaded from: classes.dex */
public class BaseFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        throw new RuntimeException("Stub!");
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        throw new RuntimeException("Stub!");
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        throw new RuntimeException("Stub!");
    }

    @Override // androidx.fragment.app.Fragment
    public void onPause() {
        super.onPause();
        throw new RuntimeException("Stub!");
    }

    @Override // androidx.fragment.app.Fragment
    public void onStop() {
        super.onStop();
        throw new RuntimeException("Stub!");
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        throw new RuntimeException("Stub!");
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        throw new RuntimeException("Stub!");
    }

}