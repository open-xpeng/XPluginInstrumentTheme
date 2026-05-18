package com.xiaopeng.xposed.instrument.theme.utils

import android.content.Context
import android.os.Process
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TemporaryDebugLogger {
    private const val MAX_LOG_BYTES: Long = 50L * 1024L * 1024L
    private const val LOG_DIR_NAME: String = "xplugin-temp-logs"
    private const val LOG_FILE_NAME: String = "cloud-recovery.log"
    private val timeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    fun log(context: Context, tag: String, message: String) {
        val line = buildString {
            append(timeFormatter.format(Date()))
            append(" pid=")
            append(Process.myPid())
            append(" [")
            append(tag)
            append("] ")
            append(message)
            append('\n')
        }
        BoundedFileLogger.append(
            file = logFile(context = context),
            content = line,
            maxBytes = MAX_LOG_BYTES,
        )
    }

    fun logError(context: Context, tag: String, throwable: Throwable, message: String) {
        val throwableMessage = throwable.message ?: "<no message>"
        log(
            context = context,
            tag = tag,
            message = "$message | ${throwable.javaClass.simpleName}: $throwableMessage",
        )
    }

    fun logFile(context: Context): File {
        return File(File(context.filesDir, LOG_DIR_NAME), LOG_FILE_NAME)
    }
}
