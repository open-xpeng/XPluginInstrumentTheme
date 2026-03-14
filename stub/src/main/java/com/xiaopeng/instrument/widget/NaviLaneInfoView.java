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
import android.util.AttributeSet;

import com.xiaopeng.xui.widget.XLinearLayout;

import java.util.List;

public class NaviLaneInfoView extends XLinearLayout {
    public NaviLaneInfoView(Context context) {
        super(context);
    }

    public NaviLaneInfoView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public NaviLaneInfoView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public NaviLaneInfoView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    public void updateLaneBg(boolean z) {
        throw new RuntimeException("Stub!");
    }

    public void updateTollGateData(List<Integer> list) {
        throw new RuntimeException("Stub!");
    }

    public void updateNormalLaneData(int[][] iArr) {
        throw new RuntimeException("Stub!");
    }



}
