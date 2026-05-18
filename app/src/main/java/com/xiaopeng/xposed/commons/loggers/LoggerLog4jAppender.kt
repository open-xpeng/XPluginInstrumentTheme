/*
 * Copyright 2026 Sollyu
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

package com.xiaopeng.xposed.commons.loggers

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.UnsynchronizedAppenderBase
import de.robv.android.xposed.XposedBridge

class LoggerLog4jAppender : UnsynchronizedAppenderBase<ILoggingEvent>() {

    var encoder: PatternLayoutEncoder? = null

    override fun start() {
        if (this.encoder == null || this.encoder!!.layout == null) {
            addError("No layout set for the appender named [${this.name}].")
            return
        }
        super.start()
    }

    override fun append(event: ILoggingEvent) {
        if (!isStarted) {
            return
        }
        val encoder: PatternLayoutEncoder = this.encoder ?: return

        when (event.level.levelInt) {
            Level.ALL_INT   -> XposedBridge.log(encoder.layout.doLayout(event))
            Level.TRACE_INT -> XposedBridge.log(encoder.layout.doLayout(event))
            Level.DEBUG_INT -> XposedBridge.log(encoder.layout.doLayout(event))
            Level.INFO_INT  -> XposedBridge.log(encoder.layout.doLayout(event))
            Level.WARN_INT  -> XposedBridge.log(encoder.layout.doLayout(event))
            Level.ERROR_INT -> XposedBridge.log(encoder.layout.doLayout(event))
            Level.OFF_INT   -> Unit
            else            -> Unit
        }
    }
}

