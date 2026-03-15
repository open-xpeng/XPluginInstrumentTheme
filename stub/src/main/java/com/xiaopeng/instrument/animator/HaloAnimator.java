package com.xiaopeng.instrument.animator;

import android.animation.ObjectAnimator;
import android.view.View;

/* JADX INFO: loaded from: classes.dex */
public class HaloAnimator extends BaseViewAnimator {
    private static final float ALPHA_END = 0.1f;
    private static final float ALPHA_START = 1.0f;
    private ObjectAnimator mObjectAnimator;

    @Override // com.xiaopeng.instrument.animator.BaseViewAnimator
    public void prepare(View... viewArr) {
        throw new RuntimeException("Stub!");
    }
}