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