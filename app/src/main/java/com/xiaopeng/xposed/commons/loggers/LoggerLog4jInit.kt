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

import android.content.Context
import android.os.Process
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.android.LogcatAppender
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.LevelFilter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy
import ch.qos.logback.core.spi.FilterReply
import ch.qos.logback.core.util.FileSize
import ch.qos.logback.core.util.OptionHelper
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File

object LoggerLog4jInit : (Context) -> Unit {

    private val mProcessName: String by lazy { Process.myPid().toString() }

    override fun invoke(context: Context) {
        val loggerContext: LoggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        loggerContext.reset()
        loggerContext.putProperty("CACHE_DIR" , File(context.externalCacheDir, "HyperHDH").absolutePath)
        loggerContext.putProperty("PROCESS_ID", mProcessName)

        val logger: Logger = loggerContext.getLogger("ROOT")

        // Logcat
        val logcatAppender = initSlf4JLogCat()
        val logcatFilter = LevelFilter()
        logcatFilter.setLevel(Level.DEBUG)
        logcatFilter.onMatch = FilterReply.ACCEPT
        logcatFilter.onMismatch = FilterReply.DENY
        logcatAppender.addFilter(logcatFilter)
        logger.addAppender(logcatAppender)

        // Xposed
        if (false) {
            val xposedAppender = initSlf4JXposed()
            val xposedFilter = LevelFilter()
            xposedFilter.setLevel(Level.DEBUG)
            xposedFilter.onMatch = FilterReply.ACCEPT
            xposedFilter.onMismatch = FilterReply.DENY
            xposedAppender.addFilter(xposedFilter)
            logger.addAppender(xposedAppender)
        }

        // File
        val fileAppender = initSlf4JFile(context = context)
        val fileFilter = LevelFilter()
        fileFilter.setLevel(Level.DEBUG)
        fileFilter.onMatch = FilterReply.ACCEPT
        fileFilter.onMismatch = FilterReply.DENY
        fileAppender.addFilter(fileFilter)
        logger.addAppender(fileAppender)

        logger.level = Level.DEBUG
        logger.isAdditive = false
    }

    private fun initSlf4JLogCat(): LogcatAppender {
        val loggerContext: LoggerContext = LoggerFactory.getILoggerFactory() as LoggerContext

        val patternLayout = PatternLayoutEncoder()
        patternLayout.context = loggerContext
        patternLayout.pattern = OptionHelper.substVars("[XTheme] %-50(%F:%L) [%-10thread] %m%n", loggerContext)
        patternLayout.charset = Charsets.UTF_8
        patternLayout.start()

        val logcatAppender = LogcatAppender()
        logcatAppender.context = loggerContext
        logcatAppender.encoder = patternLayout
        logcatAppender.start()
        return logcatAppender
    }

    private fun initSlf4JXposed(): LoggerLog4jAppender {
        val loggerContext: LoggerContext = LoggerFactory.getILoggerFactory() as LoggerContext

        val patternLayout = PatternLayoutEncoder()
        patternLayout.context = loggerContext
        patternLayout.pattern = OptionHelper.substVars($$"[XTheme] [%-5level] [${PROCESS_ID}:%-26thread] %m%n", loggerContext)
        patternLayout.charset = Charsets.UTF_8
        patternLayout.start()

        val logcatAppender = LoggerLog4jAppender()
        logcatAppender.context = loggerContext
        logcatAppender.encoder = patternLayout
        logcatAppender.start()
        return logcatAppender
    }


    private fun initSlf4JFile(context: Context): RollingFileAppender<ILoggingEvent> {
        val loggerContext: LoggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        val logDir = File(loggerContext.getProperty("CACHE_DIR"))
        if (logDir.exists().not()) {
            FileUtils.forceMkdir(logDir)
        }

        val patternLayout = PatternLayoutEncoder()
        patternLayout.context = loggerContext
        patternLayout.pattern = OptionHelper.substVars($$"%d{yyyy-MM-dd HH:mm:ss.SSS} [${PROCESS_ID}] [%5p] - %m%n", loggerContext)
        patternLayout.charset = Charsets.UTF_8
        patternLayout.start()

        val rollingPolicy = SizeAndTimeBasedRollingPolicy<ILoggingEvent>()
        rollingPolicy.context = loggerContext
        rollingPolicy.fileNamePattern = OptionHelper.substVars($$"${CACHE_DIR}/%d{yyyy-MM-dd_HH}.%i.log", loggerContext)
        rollingPolicy.maxHistory = 24
        rollingPolicy.isCleanHistoryOnStart = true
        rollingPolicy.setTotalSizeCap(FileSize.valueOf("300MB"))
        rollingPolicy.setMaxFileSize(FileSize.valueOf("10MB"))

        val fileAppender = RollingFileAppender<ILoggingEvent>()
        fileAppender.context = loggerContext
        fileAppender.encoder = patternLayout
        fileAppender.rollingPolicy = rollingPolicy
        rollingPolicy.setParent(fileAppender)
        rollingPolicy.start()
        fileAppender.start()
        return fileAppender
    }
}
