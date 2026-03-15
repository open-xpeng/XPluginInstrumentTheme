package com.xiaopeng.instrument.bean;

import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class InfoContainBean {
    private List<InfoBean> mInfoBeanList;
    private int mSelectIndex;

    public int getSelectIndex() {
        return this.mSelectIndex;
    }

    public void setSelectIndex(int i) {
        this.mSelectIndex = i;
    }

    public List<InfoBean> getInfoBeanList() {
        return this.mInfoBeanList;
    }

    public void setInfoBeanList(List<InfoBean> list) {
        this.mInfoBeanList = list;
    }
}