package com.xiaopeng.instrument.animator;

import android.animation.ObjectAnimator;
import android.view.View;

/* JADX INFO: loaded from: classes.dex */
public class SlideInDownAnimator extends BaseViewAnimator {
    private static final int DEFAULT_DISTANCE = 600;
    private static final String TAG = "SlideInDownAnimator";
    private ObjectAnimator mAlphaObjectAnimator;
    private ObjectAnimator mTransObjectAnimator;

    @Override // com.xiaopeng.instrument.animator.BaseViewAnimator
    public void prepare(View... viewArr) {
        throw new RuntimeException("Stub!");
    }
}