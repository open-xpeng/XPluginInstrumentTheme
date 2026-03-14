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

package com.xiaopeng.speech;

import android.os.Handler;


/* JADX INFO: loaded from: classes23.dex */
public abstract class SpeechNode<T> {
    protected Handler mWorkerHandler;
    private   boolean mSubscribed = false;

    public void setSubscribed(boolean subscribed) {
        throw new RuntimeException("Stub!");
    }

    public boolean isSubscribed() {
        throw new RuntimeException("Stub!");
    }

    protected void onSubscribe() {
        throw new RuntimeException("Stub!");
    }

    protected void onUnsubscribe() {
        throw new RuntimeException("Stub!");
    }

    public Handler getWorkerHandler() {
        throw new RuntimeException("Stub!");
    }

    public void setWorkerHandler(Handler workerHandler) {
        throw new RuntimeException("Stub!");
    }

    public void addListener(T listener) {
        throw new RuntimeException("Stub!");
    }

    public void removeListener(T listener) {
        throw new RuntimeException("Stub!");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void performCommand(String command, String data) {
        throw new RuntimeException("Stub!");
    }

    public String[] getSubscribeEvents() {
        throw new RuntimeException("Stub!");
    }

}