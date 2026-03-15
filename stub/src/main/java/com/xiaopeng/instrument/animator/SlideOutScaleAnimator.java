package com.xiaopeng.instrument.animator;

import android.animation.ObjectAnimator;
import android.view.View;

/* JADX INFO: loaded from: classes.dex */
public class SlideOutScaleAnimator extends BaseViewAnimator {
    private static final String TAG = "SlideOutScaleAnimator";
    private ObjectAnimator mAlphaObjectAnimator;
    private ObjectAnimator mScaleXObjectAnimator;
    private ObjectAnimator mScaleYObjectAnimator;

    @Override // com.xiaopeng.instrument.animator.BaseViewAnimator
    public void prepare(View... viewArr) {
        throw new RuntimeException("Stub!");
    }
}