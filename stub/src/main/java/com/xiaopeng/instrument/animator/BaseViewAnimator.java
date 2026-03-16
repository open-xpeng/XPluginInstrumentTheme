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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.view.View;
import android.view.animation.Interpolator;

/* JADX INFO: loaded from: classes.dex */
public abstract class BaseViewAnimator {
    public static final long DURATION = 300;
    private static final String TAG = "com.xiaopeng.instrument.animator.BaseViewAnimator";
    private static final int VIEW_SIZE = 0;
    private long mDuration = 300;
    private int mRepeatCount = 0;
    private int mRepeatMode = 1;
    private AnimatorSet mAnimatorSet = new AnimatorSet();

    protected abstract void prepare(View... viewArr);

    private BaseViewAnimator setTarget(View... viewArr) {
        throw new RuntimeException("Stub!");
    }

    private boolean isEmptyView(View... viewArr) {
        throw new RuntimeException("Stub!");
    }

    public void animate(View... viewArr) {
        throw new RuntimeException("Stub!");
    }

    public void restart() {
        throw new RuntimeException("Stub!");
    }

    public void reset(View... viewArr) {
        throw new RuntimeException("Stub!");
    }

    private void start() {
        throw new RuntimeException("Stub!");
    }

    private void startInternal() {
        throw new RuntimeException("Stub!");
    }

    public BaseViewAnimator addAnimatorListener(Animator.AnimatorListener animatorListener) {
        throw new RuntimeException("Stub!");
    }

    public boolean hasListener() {
        throw new RuntimeException("Stub!");
    }

    public void destroy() {
        throw new RuntimeException("Stub!");
    }

    public void cancel() {
        throw new RuntimeException("Stub!");
    }

    public boolean isRunning() {
        throw new RuntimeException("Stub!");
    }

    public boolean isStarted() {
        throw new RuntimeException("Stub!");
    }

    public void removeAnimatorListener(Animator.AnimatorListener animatorListener) {
        throw new RuntimeException("Stub!");
    }

    public void removeAllListener() {
        throw new RuntimeException("Stub!");
    }

    public long getStartDelay() {
        throw new RuntimeException("Stub!");
    }

    public BaseViewAnimator setStartDelay(long j) {
        throw new RuntimeException("Stub!");
    }

    public BaseViewAnimator setInterpolator(Interpolator interpolator) {
        throw new RuntimeException("Stub!");
    }

    public long getDuration() {
        throw new RuntimeException("Stub!");
    }

    public BaseViewAnimator setDuration(long j) {
        throw new RuntimeException("Stub!");
    }

    public AnimatorSet getAnimatorSet() {
        throw new RuntimeException("Stub!");
    }

    public BaseViewAnimator setRepeatCount(int i) {
        throw new RuntimeException("Stub!");
    }

    public BaseViewAnimator setRepeatMode(int i) {
        throw new RuntimeException("Stub!");
    }
}