package com.xiaopeng.instrument.animator;

import android.animation.ObjectAnimator;
import android.view.View;

/* JADX INFO: loaded from: classes.dex */
public class FrgChangeLeftAnimator extends BaseViewAnimator {
    private static final float ALPHA_MAX = 1.0f;
    private static final float ALPHA_MIN = 0.0f;
    private static final float FROM_X = 0.0f;
    private static final float TO_X = -960.0f;
    private ObjectAnimator mAlphaOutObjectAnimator;
    private ObjectAnimator mObjectAnimator;

    @Override // com.xiaopeng.instrument.animator.BaseViewAnimator
    public void prepare(View... viewArr) {
        throw new RuntimeException("Stub!");
    }
}