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

package com.xiaopeng.instrument.animator;

import android.animation.ObjectAnimator;
import android.view.View;

/* JADX INFO: loaded from: classes.dex */
public class TransitionGearAnimator extends BaseViewAnimator {
    public static final float ALPHA_MAX = 1.0f;
    public static final float ALPHA_MIN = 0.0f;
    private static final long DELAY_TIME = 1000;
    private static final long DURATION = 300;
    private static final long DURATION_SPEED = 150;
    private static final String TAG = "TransitionGearAnimator";
    private static final int VIEW_FIRST_INDEX = 0;
    private static final int VIEW_SECOND_INDEX = 1;
    private static final int VIEW_SIZE = 2;
    private ObjectAnimator mAlphaFadeInObjectAnimator;
    private ObjectAnimator mAlphaInObjectAnimator;
    private ObjectAnimator mAlphaOutObjectAnimator;

    @Override // com.xiaopeng.instrument.animator.BaseViewAnimator
    protected void prepare(View... viewArr) {
        throw new RuntimeException("Stub!");
    }
}