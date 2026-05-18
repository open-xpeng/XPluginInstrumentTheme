package com.xiaopeng.xposed.instrument.theme.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class BoundedFileLoggerTest {

    @Test
    fun `rotates file before append when next write would exceed limit`() {
        val dir = Files.createTempDirectory("bounded-file-logger-test").toFile()
        val file = File(dir, "cloud-recovery.log")
        file.writeText("123456789")

        BoundedFileLogger.append(
            file = file,
            content = "abc",
            maxBytes = 10L,
        )

        assertEquals("abc", file.readText())
    }

    @Test
    fun `appends normally when still under limit`() {
        val dir = Files.createTempDirectory("bounded-file-logger-test").toFile()
        val file = File(dir, "cloud-recovery.log")
        file.writeText("1234")

        BoundedFileLogger.append(
            file = file,
            content = "56",
            maxBytes = 10L,
        )

        assertEquals("123456", file.readText())
        assertTrue(file.length() <= 10L)
    }
}
