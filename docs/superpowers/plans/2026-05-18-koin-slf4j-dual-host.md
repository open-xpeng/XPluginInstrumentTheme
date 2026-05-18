# Koin 与 SLF4J 双宿主接入 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 `com.xiaopeng.instrument` 与 `com.xiaopeng.montecarlo` 两个宿主补齐 `Application#onCreate` 级别的 Koin 与 SLF4J 初始化能力，同时保持现有 Hook 结构不变。

**Architecture:** 在 `XposedMan` 之上新增一层宿主启动基础设施。`XposedMan` 继续按包名注册现有 Hook，并额外注册一个通用的 `Application#onCreate` Hook；该 Hook 根据宿主包名触发一次性的日志初始化与 Koin 启动。基础设施拆成宿主标识、幂等守卫、Koin modules、Koin logger、Logback 初始化器五块，业务 Hook 只做最小接入。

**Tech Stack:** Kotlin、Xposed `XC_MethodHook` / `XposedHelpers`、Koin 4.2、SLF4J 2.0、`logback-android`、JUnit 4、Gradle Wrapper。

---

## 文件结构

- Create: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/bootstrap/XposedHostApp.kt`
  - 定义支持的宿主枚举、包名、日志目录名和包名解析逻辑。
- Create: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/bootstrap/XposedHostRuntime.kt`
  - 定义宿主运行时信息，封装 `Application`、`LoadPackageParam`、进程名、日志目录名。
- Create: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/bootstrap/HostBootstrapRegistry.kt`
  - 负责按“宿主包名 + 进程名”判重，避免重复初始化。
- Create: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/bootstrap/HostKoinModules.kt`
  - 生成 `common`、`instrument`、`montecarlo` 三类 module。
- Create: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/bootstrap/HostKoinLogger.kt`
  - 把 Koin 日志桥接到 SLF4J。
- Create: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/bootstrap/HostLogbackInitializer.kt`
  - 配置 Logcat 与滚动文件日志。
- Create: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/bootstrap/HostBootstrapper.kt`
  - 串起幂等判断、日志初始化、Koin 启动。
- Create: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/HostApplicationOnCreateHook.kt`
  - 用 `Application.onCreate` 作为统一启动挂点，按宿主包名过滤后调用 `HostBootstrapper`。
- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/XposedMan.kt`
  - 在两个宿主分支中都注册 `HostApplicationOnCreateHook`，保留现有业务 Hook。
- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MainActivityHook.kt`
  - 示范性替换为 `LoggerFactory` 日志，保留现有行为。
- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapViewWrapperHook.kt`
  - 示范性替换为 `LoggerFactory` 日志，保留现有行为。
- Create: `app/src/test/java/com/xiaopeng/xposed/instrument/theme/bootstrap/XposedHostAppTest.kt`
  - 测试宿主包名解析、日志目录名和进程 key。
- Create: `app/src/test/java/com/xiaopeng/xposed/instrument/theme/bootstrap/HostBootstrapRegistryTest.kt`
  - 测试幂等注册表的首次进入、重复进入、失败回滚。

## 实现任务

### Task 1: 建立宿主标识与幂等守卫

**Files:**
- Create: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/bootstrap/XposedHostApp.kt`
- Create: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/bootstrap/XposedHostRuntime.kt`
- Create: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/bootstrap/HostBootstrapRegistry.kt`
- Create: `app/src/test/java/com/xiaopeng/xposed/instrument/theme/bootstrap/XposedHostAppTest.kt`
- Create: `app/src/test/java/com/xiaopeng/xposed/instrument/theme/bootstrap/HostBootstrapRegistryTest.kt`

- [ ] **Step 1: 写宿主解析失败测试**

创建 `XposedHostAppTest.kt`：

```kotlin
package com.xiaopeng.xposed.instrument.theme.bootstrap

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class XposedHostAppTest {

    @Test
    fun `resolves instrument package`() {
        assertEquals(
            XposedHostApp.INSTRUMENT,
            XposedHostApp.fromPackageName("com.xiaopeng.instrument")
        )
    }

    @Test
    fun `resolves montecarlo package`() {
        assertEquals(
            XposedHostApp.MONTE_CARLO,
            XposedHostApp.fromPackageName("com.xiaopeng.montecarlo")
        )
    }

    @Test
    fun `returns null for unsupported package`() {
        assertNull(XposedHostApp.fromPackageName("com.xiaopeng.unknown"))
    }

    @Test
    fun `exposes stable log directory names`() {
        assertEquals("instrument", XposedHostApp.INSTRUMENT.logDirName)
        assertEquals("montecarlo", XposedHostApp.MONTE_CARLO.logDirName)
    }
}
```

- [ ] **Step 2: 写幂等守卫失败测试**

创建 `HostBootstrapRegistryTest.kt`：

```kotlin
package com.xiaopeng.xposed.instrument.theme.bootstrap

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HostBootstrapRegistryTest {

    @Before
    fun setUp() {
        HostBootstrapRegistry.clearForTest()
    }

    @Test
    fun `first enter succeeds and second enter is blocked`() {
        val key = HostBootstrapRegistry.createKey(
            packageName = "com.xiaopeng.instrument",
            processName = "com.xiaopeng.instrument"
        )

        assertTrue(HostBootstrapRegistry.tryMarkInitializing(key))
        assertFalse(HostBootstrapRegistry.tryMarkInitializing(key))
    }

    @Test
    fun `mark success keeps key blocked`() {
        val key = HostBootstrapRegistry.createKey(
            packageName = "com.xiaopeng.montecarlo",
            processName = "com.xiaopeng.montecarlo:ui"
        )

        assertTrue(HostBootstrapRegistry.tryMarkInitializing(key))
        HostBootstrapRegistry.markInitialized(key)

        assertFalse(HostBootstrapRegistry.tryMarkInitializing(key))
    }

    @Test
    fun `mark failure reopens key`() {
        val key = HostBootstrapRegistry.createKey(
            packageName = "com.xiaopeng.instrument",
            processName = "com.xiaopeng.instrument:remote"
        )

        assertTrue(HostBootstrapRegistry.tryMarkInitializing(key))
        HostBootstrapRegistry.markFailed(key)

        assertTrue(HostBootstrapRegistry.tryMarkInitializing(key))
    }

    @Test
    fun `create key combines package and process`() {
        assertEquals(
            "com.xiaopeng.instrument|com.xiaopeng.instrument:remote",
            HostBootstrapRegistry.createKey(
                packageName = "com.xiaopeng.instrument",
                processName = "com.xiaopeng.instrument:remote"
            )
        )
    }
}
```

- [ ] **Step 3: 运行失败测试**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.xiaopeng.xposed.instrument.theme.bootstrap.XposedHostAppTest" --tests "com.xiaopeng.xposed.instrument.theme.bootstrap.HostBootstrapRegistryTest"
```

Expected:

```text
FAILURE: Build failed with an exception.
... unresolved reference: XposedHostApp
... unresolved reference: HostBootstrapRegistry
```

- [ ] **Step 4: 实现宿主枚举与运行时信息**

创建 `XposedHostApp.kt`：

```kotlin
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

package com.xiaopeng.xposed.instrument.theme.bootstrap

enum class XposedHostApp(
    val packageName: String,
    val logDirName: String,
) {
    INSTRUMENT(
        packageName = "com.xiaopeng.instrument",
        logDirName = "instrument",
    ),
    MONTE_CARLO(
        packageName = "com.xiaopeng.montecarlo",
        logDirName = "montecarlo",
    );

    companion object {
        fun fromPackageName(packageName: String): XposedHostApp? {
            return entries.firstOrNull { it.packageName == packageName }
        }
    }
}
```

创建 `XposedHostRuntime.kt`：

```kotlin
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

package com.xiaopeng.xposed.instrument.theme.bootstrap

import android.app.Application
import de.robv.android.xposed.callbacks.XC_LoadPackage

data class XposedHostRuntime(
    val hostApp: XposedHostApp,
    val application: Application,
    val loadPackageParam: XC_LoadPackage.LoadPackageParam,
) {
    val packageName: String
        get() = hostApp.packageName

    val processName: String
        get() = loadPackageParam.processName

    val bootstrapKey: String
        get() = HostBootstrapRegistry.createKey(
            packageName = packageName,
            processName = processName,
        )
}
```

- [ ] **Step 5: 实现幂等守卫**

创建 `HostBootstrapRegistry.kt`：

```kotlin
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

package com.xiaopeng.xposed.instrument.theme.bootstrap

import java.util.concurrent.ConcurrentHashMap

object HostBootstrapRegistry {

    private val mStateMap = ConcurrentHashMap<String, State>()

    fun createKey(packageName: String, processName: String): String {
        return "$packageName|$processName"
    }

    fun tryMarkInitializing(key: String): Boolean {
        return mStateMap.putIfAbsent(key, State.INITIALIZING) == null
    }

    fun markInitialized(key: String) {
        mStateMap[key] = State.INITIALIZED
    }

    fun markFailed(key: String) {
        mStateMap.remove(key)
    }

    fun clearForTest() {
        mStateMap.clear()
    }

    private enum class State {
        INITIALIZING,
        INITIALIZED,
    }
}
```

- [ ] **Step 6: 运行测试确认通过**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.xiaopeng.xposed.instrument.theme.bootstrap.XposedHostAppTest" --tests "com.xiaopeng.xposed.instrument.theme.bootstrap.HostBootstrapRegistryTest"
```

Expected:

```text
BUILD SUCCESSFUL
```

### Task 2: 搭建 Koin modules 与 Koin logger

**Files:**
- Create: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/bootstrap/HostKoinModules.kt`
- Create: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/bootstrap/HostKoinLogger.kt`
- Modify: `app/build.gradle.kts`
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: 写 module 选择失败测试**

在 `XposedHostAppTest.kt` 末尾追加：

```kotlin
    @Test
    fun `host module names are stable`() {
        assertEquals("instrument", XposedHostApp.INSTRUMENT.logDirName)
        assertEquals("montecarlo", XposedHostApp.MONTE_CARLO.logDirName)
    }
```

在 `HostBootstrapRegistryTest.kt` 末尾追加：

```kotlin
    @Test
    fun `create key is deterministic`() {
        val key1 = HostBootstrapRegistry.createKey(
            packageName = "com.xiaopeng.instrument",
            processName = "com.xiaopeng.instrument"
        )
        val key2 = HostBootstrapRegistry.createKey(
            packageName = "com.xiaopeng.instrument",
            processName = "com.xiaopeng.instrument"
        )

        assertEquals(key1, key2)
    }
```

目的：本任务不新增新的测试类，沿用已有纯逻辑入口，保证后续 module/logger 相关辅助属性有稳定来源。

- [ ] **Step 2: 确认依赖目录无需再扩张**

保持 `gradle/libs.versions.toml` 现有 Koin / SLF4J 依赖，不新增 `commons-io` 或 `gson`。如果文件还未是以下片段，先对齐：

```toml
koin-bom         = { module = "io.insert-koin:koin-bom", version = "4.2.1" }
koin-core        = { module = "io.insert-koin:koin-core" }
koin-android     = { module = "io.insert-koin:koin-android" }
slf4j-api        = "org.slf4j:slf4j-api:2.0.18"
slf4j-logback    = "com.github.tony19:logback-android:3.0.0"
```

并确保 `app/build.gradle.kts` 仍包含：

```kotlin
implementation(platform(libs.koin.bom))
implementation(libs.koin.core)
implementation(libs.koin.android)

implementation(libs.slf4j.api)
implementation(libs.slf4j.logback)
```

- [ ] **Step 3: 实现 Koin logger**

创建 `HostKoinLogger.kt`：

```kotlin
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

package com.xiaopeng.xposed.instrument.theme.bootstrap

import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import org.slf4j.LoggerFactory

object HostKoinLogger : Logger(Level.INFO) {

    private val mLogger = LoggerFactory.getLogger(HostKoinLogger::class.java)

    override fun display(level: Level, msg: MESSAGE) {
        when (level) {
            Level.DEBUG -> mLogger.debug("koin: {}", msg)
            Level.INFO -> mLogger.info("koin: {}", msg)
            Level.WARNING -> mLogger.warn("koin: {}", msg)
            Level.ERROR -> mLogger.error("koin: {}", msg)
            Level.NONE -> Unit
        }
    }
}
```

- [ ] **Step 4: 实现三层 modules**

创建 `HostKoinModules.kt`：

```kotlin
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

package com.xiaopeng.xposed.instrument.theme.bootstrap

import android.app.Application
import android.os.Handler
import android.os.Looper
import org.koin.core.module.Module
import org.koin.dsl.module

object HostKoinModules {

    fun createModules(runtime: XposedHostRuntime): List<Module> {
        return listOf(
            createCommonModule(runtime = runtime),
        ) + when (runtime.hostApp) {
            XposedHostApp.INSTRUMENT -> listOf(createInstrumentModule(runtime = runtime))
            XposedHostApp.MONTE_CARLO -> listOf(createMonteCarloModule(runtime = runtime))
        }
    }

    private fun createCommonModule(runtime: XposedHostRuntime): Module {
        return module {
            single<Application> { runtime.application }
            single { runtime.loadPackageParam }
            single { runtime }
            single<Handler> { Handler(Looper.getMainLooper()) }
        }
    }

    private fun createInstrumentModule(runtime: XposedHostRuntime): Module {
        return module {
            single<XposedHostApp> { runtime.hostApp }
            single<String>(qualifier = org.koin.core.qualifier.named("hostPackageName")) { runtime.packageName }
            single<String>(qualifier = org.koin.core.qualifier.named("hostProcessName")) { runtime.processName }
        }
    }

    private fun createMonteCarloModule(runtime: XposedHostRuntime): Module {
        return module {
            single<XposedHostApp> { runtime.hostApp }
            single<String>(qualifier = org.koin.core.qualifier.named("hostPackageName")) { runtime.packageName }
            single<String>(qualifier = org.koin.core.qualifier.named("hostProcessName")) { runtime.processName }
        }
    }
}
```

- [ ] **Step 5: 运行单测确保纯逻辑仍通过**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.xiaopeng.xposed.instrument.theme.bootstrap.XposedHostAppTest" --tests "com.xiaopeng.xposed.instrument.theme.bootstrap.HostBootstrapRegistryTest"
```

Expected:

```text
BUILD SUCCESSFUL
```

### Task 3: 搭建 Logback 初始化器

**Files:**
- Create: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/bootstrap/HostLogbackInitializer.kt`

- [ ] **Step 1: 写文件日志路径辅助失败测试**

在 `XposedHostAppTest.kt` 末尾追加：

```kotlin
    @Test
    fun `log dir names remain package specific`() {
        assertEquals("instrument", XposedHostApp.INSTRUMENT.logDirName)
        assertEquals("montecarlo", XposedHostApp.MONTE_CARLO.logDirName)
    }
```

这个测试看起来重复，但它保护的是日志目录命名约束，防止后续把两个宿主写进同一目录。

- [ ] **Step 2: 创建共享 Logback 初始化器**

创建 `HostLogbackInitializer.kt`：

```kotlin
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

package com.xiaopeng.xposed.instrument.theme.bootstrap

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
import org.slf4j.LoggerFactory
import java.io.File

object HostLogbackInitializer {

    fun initialize(runtime: XposedHostRuntime) {
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        loggerContext.reset()

        val cacheRoot = runtime.application.externalCacheDir ?: runtime.application.cacheDir
        val logDir = File(cacheRoot, "XPluginInstrumentTheme/${runtime.hostApp.logDirName}/${runtime.processName.replace(':', '_')}")
        if (!logDir.exists() && !logDir.mkdirs()) {
            throw IllegalStateException("Failed to create log directory: ${logDir.absolutePath}")
        }

        loggerContext.putProperty("LOG_DIR", logDir.absolutePath)
        loggerContext.putProperty("HOST_PACKAGE", runtime.packageName)
        loggerContext.putProperty("HOST_PROCESS", runtime.processName)

        val rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME)
        rootLogger.level = Level.DEBUG
        rootLogger.isAdditive = false
        rootLogger.addAppender(createLogcatAppender(loggerContext))

        runCatching {
            rootLogger.addAppender(createFileAppender(loggerContext))
        }
    }

    private fun createLogcatAppender(loggerContext: LoggerContext): LogcatAppender {
        val encoder = PatternLayoutEncoder()
        encoder.context = loggerContext
        encoder.pattern = OptionHelper.substVars($$"[XPIT][${HOST_PACKAGE}] %-40(%F:%L) [%thread] %m%n", loggerContext)
        encoder.charset = Charsets.UTF_8
        encoder.start()

        val appender = LogcatAppender()
        appender.context = loggerContext
        appender.encoder = encoder
        appender.addFilter(createDebugLevelFilter())
        appender.start()
        return appender
    }

    private fun createFileAppender(loggerContext: LoggerContext): RollingFileAppender<ILoggingEvent> {
        val encoder = PatternLayoutEncoder()
        encoder.context = loggerContext
        encoder.pattern = OptionHelper.substVars($$"%d{yyyy-MM-dd HH:mm:ss.SSS} [${HOST_PROCESS}] [%5p] %m%n", loggerContext)
        encoder.charset = Charsets.UTF_8
        encoder.start()

        val policy = SizeAndTimeBasedRollingPolicy<ILoggingEvent>()
        policy.context = loggerContext
        policy.fileNamePattern = OptionHelper.substVars($$"${LOG_DIR}/%d{yyyy-MM-dd_HH}.%i.log", loggerContext)
        policy.maxHistory = 24
        policy.isCleanHistoryOnStart = true
        policy.setTotalSizeCap(FileSize.valueOf("300MB"))
        policy.setMaxFileSize(FileSize.valueOf("10MB"))

        val appender = RollingFileAppender<ILoggingEvent>()
        appender.context = loggerContext
        appender.encoder = encoder
        appender.rollingPolicy = policy
        policy.setParent(appender)
        policy.start()
        appender.addFilter(createDebugLevelFilter())
        appender.start()
        return appender
    }

    private fun createDebugLevelFilter(): LevelFilter {
        val filter = LevelFilter()
        filter.setLevel(Level.DEBUG)
        filter.onMatch = FilterReply.ACCEPT
        filter.onMismatch = FilterReply.DENY
        filter.start()
        return filter
    }
}
```

- [ ] **Step 3: 编译确认 Logback API 可用**

Run:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Expected:

```text
BUILD SUCCESSFUL
```

### Task 4: 搭建 Bootstrapper 与 `Application#onCreate` Hook

**Files:**
- Create: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/bootstrap/HostBootstrapper.kt`
- Create: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/HostApplicationOnCreateHook.kt`
- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/XposedMan.kt`

- [ ] **Step 1: 写 `Application` 启动入口骨架**

创建 `HostBootstrapper.kt`：

```kotlin
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

package com.xiaopeng.xposed.instrument.theme.bootstrap

import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.slf4j.LoggerFactory

object HostBootstrapper {

    private val mLogger = LoggerFactory.getLogger(HostBootstrapper::class.java)

    fun bootstrap(runtime: XposedHostRuntime) {
        val key = runtime.bootstrapKey
        if (!HostBootstrapRegistry.tryMarkInitializing(key)) {
            mLogger.debug("skip duplicated bootstrap: {}", key)
            return
        }

        runCatching {
            HostLogbackInitializer.initialize(runtime)
            if (GlobalContext.getOrNull() == null) {
                startKoin {
                    logger(HostKoinLogger)
                    androidContext(runtime.application)
                    modules(HostKoinModules.createModules(runtime))
                }
            }
            mLogger.info("bootstrap complete: package={} process={}", runtime.packageName, runtime.processName)
            HostBootstrapRegistry.markInitialized(key)
        }.onFailure {
            HostBootstrapRegistry.markFailed(key)
            throw it
        }
    }
}
```

- [ ] **Step 2: 写 `Application#onCreate` Hook**

创建 `HostApplicationOnCreateHook.kt`：

```kotlin
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

package com.xiaopeng.xposed.instrument.theme.hook

import android.app.Application
import com.xiaopeng.xposed.instrument.theme.bootstrap.HostBootstrapper
import com.xiaopeng.xposed.instrument.theme.bootstrap.XposedHostApp
import com.xiaopeng.xposed.instrument.theme.bootstrap.XposedHostRuntime
import com.xiaopeng.xposed.instrument.theme.utils.XCMethodHookCatching
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HostApplicationOnCreateHook(
    private val mHostApp: XposedHostApp,
    private val mLoadPackageParam: XC_LoadPackage.LoadPackageParam,
) : XCMethodHookCatching() {

    override fun afterHookedMethodCatching(param: MethodHookParam) {
        val application = param.thisObject as? Application ?: return
        if (application.packageName != mHostApp.packageName) {
            return
        }

        HostBootstrapper.bootstrap(
            XposedHostRuntime(
                hostApp = mHostApp,
                application = application,
                loadPackageParam = mLoadPackageParam,
            )
        )
    }

    companion object {
        fun register(
            hostApp: XposedHostApp,
            loadPackageParam: XC_LoadPackage.LoadPackageParam,
        ) {
            XposedHelpers.findAndHookMethod(
                Application::class.java,
                "onCreate",
                HostApplicationOnCreateHook(
                    mHostApp = hostApp,
                    mLoadPackageParam = loadPackageParam,
                )
            )
        }
    }
}
```

- [ ] **Step 3: 在 `XposedMan` 注册宿主初始化 Hook**

把 `XposedMan.kt` 改成：

```kotlin
package com.xiaopeng.xposed.instrument.theme

import com.xiaopeng.xposed.instrument.theme.bootstrap.XposedHostApp
import com.xiaopeng.xposed.instrument.theme.hook.HostApplicationOnCreateHook
import com.xiaopeng.xposed.instrument.theme.hook.MainActivityHook
import com.xiaopeng.xposed.instrument.theme.hook.MainFragmentHook
import com.xiaopeng.xposed.instrument.theme.hook.MiniMapViewWrapperHook
import com.xiaopeng.xposed.instrument.theme.utils.HostClassLoader
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class XposedMan : IXposedHookLoadPackage, IXposedHookZygoteInit {

    companion object {
        lateinit var MODULE_PATH: String
            private set

        lateinit var MODULE_CLASS_LOADER: ClassLoader
            private set
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        MODULE_PATH = startupParam.modulePath
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        runCatching { handleLoadPackageCatching(loadPackageParam = lpparam) }
            .onFailure { XposedBridge.log(it) }
    }

    private fun handleLoadPackageCatching(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        MODULE_CLASS_LOADER = loadPackageParam.classLoader
        HostClassLoader.injectClassLoader(hostClassLoader = loadPackageParam.classLoader)

        when (loadPackageParam.packageName) {
            XposedHostApp.INSTRUMENT.packageName -> {
                HostApplicationOnCreateHook.register(
                    hostApp = XposedHostApp.INSTRUMENT,
                    loadPackageParam = loadPackageParam,
                )
                MainActivityHook(loadPackageParam = loadPackageParam)
                MainFragmentHook(loadPackageParam = loadPackageParam)
            }

            XposedHostApp.MONTE_CARLO.packageName -> {
                HostApplicationOnCreateHook.register(
                    hostApp = XposedHostApp.MONTE_CARLO,
                    loadPackageParam = loadPackageParam,
                )
                MiniMapViewWrapperHook(loadPackageParam = loadPackageParam)
            }
        }
    }
}
```

- [ ] **Step 4: 编译确认 Hook 能通过**

Run:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Expected:

```text
BUILD SUCCESSFUL
```

### Task 5: 示范性接入 SLF4J logger

**Files:**
- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MainActivityHook.kt`
- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapViewWrapperHook.kt`

- [ ] **Step 1: 在 `MainActivityHook` 增加 logger**

在 `MainActivityHook.kt` imports 中加入：

```kotlin
import org.slf4j.LoggerFactory
```

在 `object MainActivityHook` 内加入：

```kotlin
    private val mLogger = LoggerFactory.getLogger(MainActivityHook::class.java)
```

在 `showMapFullFragment(activity: MainActivity)` 开头加入：

```kotlin
        mLogger.debug("showMapFullFragment: currentFragment={}", activity.mCurrentFragmentName)
```

在 `hideMapFullFragment(activity: MainActivity)` 开头加入：

```kotlin
        mLogger.debug("hideMapFullFragment: currentFragment={}", activity.mCurrentFragmentName)
```

- [ ] **Step 2: 在 `MiniMapViewWrapperHook` 增加 logger**

在 `MiniMapViewWrapperHook.kt` imports 中加入：

```kotlin
import org.slf4j.LoggerFactory
```

在 `object MiniMapViewWrapperHook` 内加入：

```kotlin
    private val mLogger = LoggerFactory.getLogger(MiniMapViewWrapperHook::class.java)
```

把 `invoke(loadPackageParam)` 调整为：

```kotlin
    override fun invoke(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        mLogger.info("register minimap hooks: process={}", loadPackageParam.processName)
        MiniMapViewCenterHook(loadPackageParam = loadPackageParam)
        MiniMapDynamicZoomHook(loadPackageParam = loadPackageParam)
    }
```

- [ ] **Step 3: 编译确认示范接入无回归**

Run:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Expected:

```text
BUILD SUCCESSFUL
```

### Task 6: 全量验证与人工复核

**Files:**
- Verify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/bootstrap/XposedHostApp.kt`
- Verify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/bootstrap/XposedHostRuntime.kt`
- Verify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/bootstrap/HostBootstrapRegistry.kt`
- Verify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/bootstrap/HostKoinModules.kt`
- Verify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/bootstrap/HostKoinLogger.kt`
- Verify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/bootstrap/HostLogbackInitializer.kt`
- Verify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/bootstrap/HostBootstrapper.kt`
- Verify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/HostApplicationOnCreateHook.kt`
- Verify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/XposedMan.kt`
- Verify: `app/src/test/java/com/xiaopeng/xposed/instrument/theme/bootstrap/XposedHostAppTest.kt`
- Verify: `app/src/test/java/com/xiaopeng/xposed/instrument/theme/bootstrap/HostBootstrapRegistryTest.kt`

- [ ] **Step 1: 跑全部 JVM 单元测试**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 2: 跑 Kotlin 编译**

Run:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 3: 做一次人工结构复核**

确认以下条件全部满足：

```text
XposedMan 在 instrument 和 montecarlo 两个分支里都注册了 HostApplicationOnCreateHook
HostApplicationOnCreateHook 只 Hook Application.onCreate，并且按 application.packageName 过滤
HostBootstrapRegistry 的 key 同时包含 packageName 与 processName
HostLogbackInitializer 同时配置了 Logcat 和文件滚动日志
文件日志目录包含宿主目录名和 processName
HostBootstrapper 在失败时调用 HostBootstrapRegistry.markFailed(key)
Koin 启动通过 HostKoinLogger 输出日志
当前实现没有把现有业务 Hook 改造成注入类
```

- [ ] **Step 4: 用 JADX 复核宿主入口**

核对以下事实仍成立：

```text
instrument 宿主包名仍为 com.xiaopeng.instrument
montecarlo 宿主包名仍为 com.xiaopeng.montecarlo
两个宿主都存在正常的 Application.onCreate 启动链
```

- [ ] **Step 5: 停下来给用户 review，不自动提交**

执行到这里后不要提交，直接把以下状态反馈给用户：

```text
Koin / SLF4J 双宿主初始化代码已落地
compileDebugKotlin 与 testDebugUnitTest 已验证
当前工作区包含基础设施接入改动，等待用户确认后再决定是否整理提交
```

## 计划自检

- Spec coverage:
  - 双宿主 `Application#onCreate` 初始化：Task 4
  - `common + host-specific` Koin modules：Task 2
  - Koin logger：Task 2
  - Logcat + 文件滚动日志：Task 3
  - 宿主级幂等守卫：Task 1、Task 4
  - 保持现有 Hook 结构：Task 4、Task 5
  - 少量示范性日志接入：Task 5
- Placeholder scan:
  - 未使用 `TBD` / `TODO` / “稍后实现”
  - 每个代码步骤都给了明确代码块或命令
- Type consistency:
  - 宿主枚举统一使用 `XposedHostApp`
  - 运行时载体统一使用 `XposedHostRuntime`
  - 启动注册表统一使用 `HostBootstrapRegistry`
  - 引导入口统一使用 `HostBootstrapper.bootstrap(runtime)`
