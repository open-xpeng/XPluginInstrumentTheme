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

package com.xiaopeng.speech.protocol.event;

/* JADX INFO: loaded from: classes23.dex */
public class ComboEvent {
    public static final String DATA_MODE_BIO_TTS        = "data://combo.mode.bio.tts";
    public static final String DATA_MODE_FRIDGE_TTS     = "data://combo.mode.fridge.tts";
    public static final String DATA_MODE_INVISIBLE_TTS  = "data://combo.mode.invisible.tts";
    public static final String DATA_MODE_MEDITATION_TTS = "data://combo.mode.meditation.tts";
    public static final String DATA_MODE_VENTILATE_TTS  = "data://combo.mode.ventilate.tts";
    public static final String DATA_MODE_WAIT_TTS       = "data://combo.mode.wait.tts";

    public static final String ENTER_USER_MODE = "command://combo.enter.user.scenario";
    public static final String EXIT_USER_MODE  = "command://combo.exit.user.scenario";

    public static final String FAST_CLOSE_MODE_INVISIBLE = "command://combo.mode.invisible.fast.close";

    public static final String MODE_BIO           = "command://combo.mode.bio";
    public static final String MODE_BIO_OFF       = "command://combo.mode.bio.off";
    public static final String MODE_FRIDGE        = "command://combo.mode.fridge";
    public static final String MODE_FRIDGE_OFF    = "command://combo.mode.fridge.off";
    public static final String MODE_INVISIBLE     = "command://combo.mode.invisible";
    public static final String MODE_INVISIBLE_OFF = "command://combo.mode.invisible.off";
    public static final String MODE_VENTILATE     = "command://combo.mode.ventilate";
    public static final String MODE_VENTILATE_OFF = "command://combo.mode.ventilate.off";
    public static final String MODE_WAIT          = "command://combo.mode.wait";
    public static final String MODE_WAIT_OFF      = "command://combo.mode.wait.off";
}