package com.xiaopeng.instrument.widget.sr;

import android.content.Context;
import android.util.AttributeSet;

import com.xiaopeng.instrument.bean.InfoContainBean;
import com.xiaopeng.xui.widget.XRelativeLayout;

/* JADX INFO: loaded from: classes.dex */
public abstract class SRBaseInfoView extends XRelativeLayout {

    public Context mSRContext;

    abstract int getLayout();

    abstract int getPosition();

    public SRBaseInfoView(Context context) {
        this(context, null);
    }

    public SRBaseInfoView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        throw new RuntimeException("Stub!");
    }

    public SRBaseInfoView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        throw new RuntimeException("Stub!");
    }

    @Override // com.xiaopeng.xui.widget.XRelativeLayout, android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        throw new RuntimeException("Stub!");
    }

    public void init(Context context) {
        throw new RuntimeException("Stub!");
    }

    private void initAnimator() {
        throw new RuntimeException("Stub!");
    }

    protected void showMapCarView() {
        throw new RuntimeException("Stub!");
    }

    protected void showMediaCarView() {
        throw new RuntimeException("Stub!");
    }

    protected void showCarConditionCarView() {
        throw new RuntimeException("Stub!");
    }

    protected void showPowerConsumptionCarView() {
        throw new RuntimeException("Stub!");
    }

    protected void showOdoMeterCarView() {
        throw new RuntimeException("Stub!");
    }

    protected void hideFixedCardView() {
        throw new RuntimeException("Stub!");
    }

    public void showSubCardView(int i) {
        throw new RuntimeException("Stub!");
    }

    public void showCardView(boolean z) {
        throw new RuntimeException("Stub!");
    }

    public void showList(boolean z) {
        throw new RuntimeException("Stub!");
    }

    public void updateListHighIndex(int i) {
        throw new RuntimeException("Stub!");
    }

    public void updateListData(InfoContainBean infoContainBean) {
        throw new RuntimeException("Stub!");
    }
}