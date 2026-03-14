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

package com.xiaopeng.speech.protocol.node.combo;

import com.xiaopeng.speech.INodeListener;

/* JADX INFO: loaded from: classes23.dex */
public interface ComboListener extends INodeListener {
    void onDataModeBioTts();

    void onDataModeFridgeTts();

    void onDataModeInvisibleTts();

    void onDataModeMeditationTts();

    void onDataModeVentilateTts();

    void onDataModeWaitTts();

    void onFastCloseModeInvisible();

    void onModeBio();

    void onModeBioOff();

    void onModeFridge();

    void onModeFridgeOff();

    void onModeInvisible();

    void onModeInvisibleOff();

    void onModeVentilate();

    void onModeVentilateOff();

    void onModeWait();

    void onModeWaitOff();

    default void enterUserMode(String mode) {
    }

    default void enterUserModeWithExtra(String mode, String extra) {
    }

    default void exitUserModel(String mode) {
    }
}