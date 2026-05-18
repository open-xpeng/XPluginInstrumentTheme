package com.xiaopeng.xposed.instrument.theme.utils

import java.io.File
import java.io.FileOutputStream

object BoundedFileLogger {
    @Synchronized
    fun append(file: File, content: String, maxBytes: Long) {
        val bytes = content.toByteArray(Charsets.UTF_8)
        file.parentFile?.mkdirs()

        if (file.exists() && file.length() + bytes.size > maxBytes) {
            file.delete()
        }

        if (!file.exists()) {
            file.createNewFile()
        }

        FileOutputStream(file, true).use { output ->
            output.write(bytes)
            output.flush()
        }
    }
}
