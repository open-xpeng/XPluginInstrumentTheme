# 小地图动态缩放 Hook 拆分 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 `MiniMapViewWrapperHook` 中的小地图动态缩放偏移逻辑拆到独立类，保持 Hook 功能单一，同时不改变现有行为。

**Architecture:** 新增 `MiniMapDynamicZoomHook`，只负责 `setMapLevel(float)` 偏移和 `DynamicLevelHelper.enableDynamicLevel()` 基准修正。`MiniMapViewWrapperHook` 保留为 MonteCarlo 小地图 Hook 聚合入口，只依次调用 `MiniMapViewCenterHook` 和 `MiniMapDynamicZoomHook`，避免把具体功能细节扩散到 `XposedMan`。

**Tech Stack:** Kotlin、Xposed `XposedHelpers.findAndHookMethod`、`XCMethodHookCatching`、Gradle Wrapper。

---

## 文件结构

- Create: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapDynamicZoomHook.kt`
  - 承载小地图动态缩放偏移的全部逻辑。
  - 注册 `DynamicLevelHelper.enableDynamicLevel()`。
  - 注册 `MapViewWrapper.setMapLevel(float)`。
  - 保存宿主 raw level，应用 `+1.5f`，并修正动态 helper 的 `mPreLevel`。
- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapViewWrapperHook.kt`
  - 删除动态缩放字段、常量和 Hook callback。
  - 保留聚合入口，调用 `MiniMapViewCenterHook` 与 `MiniMapDynamicZoomHook`。
  - 不再直接引用 `XCMethodHookCatching`、`XC_MethodHook`、`XposedBridge`、`XposedHelpers`。

## 实现任务

### Task 1: 新增动态缩放 Hook 类

**Files:**
- Create: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapDynamicZoomHook.kt`

- [x] **Step 1: 创建文件头和 imports**

创建 `MiniMapDynamicZoomHook.kt`，内容从 license 和包声明开始：

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

import com.xiaopeng.xposed.instrument.theme.utils.XCMethodHookCatching
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
```

- [x] **Step 2: 添加对象、注册逻辑和常量**

在 imports 后加入：

```kotlin
object MiniMapDynamicZoomHook : (XC_LoadPackage.LoadPackageParam) -> Unit {

    override fun invoke(loadPackageParam: XC_LoadPackage.LoadPackageParam) {

        try {
            mMiniMapViewWrapperClass = XposedHelpers.findClass(
                "com.xiaopeng.montecarlo.navcore.mapdisplay.MiniMapViewWrapper",
                loadPackageParam.classLoader
            )
            XposedHelpers.findAndHookMethod(
                /* className    = */ "com.xiaopeng.montecarlo.dynamiclevel.base.DynamicLevelHelper",
                /* classLoader  = */ loadPackageParam.classLoader,
                /* methodName   = */ "enableDynamicLevel",
                /* callback     = */ mXCMethodEnableDynamicLevel
            )
            XposedHelpers.findAndHookMethod(
                /* className    = */ "com.xiaopeng.montecarlo.navcore.mapdisplay.MapViewWrapper",
                /* classLoader  = */ loadPackageParam.classLoader,
                /* methodName   = */ "setMapLevel",
                /* parameter    = */ Float::class.javaPrimitiveType!!,
                mXCMethodSetMapLevel
            )
        } catch (t: Throwable) {
            XposedBridge.log(t)
        }
    }

    private const val MAP_LEVEL_OFFSET = 1.5f
    private const val MAX_MAP_LEVEL = 19.0f
    private const val KEY_RAW_MAP_LEVEL = "xpit_raw_map_level"

    private var mMiniMapViewWrapperClass: Class<*>? = null
}
```

注意：保持 `enableDynamicLevel` 先于 `setMapLevel` 注册，避免动态基准修正不可用时单独启用 zoom 偏移。

- [x] **Step 3: 添加 `mXCMethodSetMapLevel`**

在 `mMiniMapViewWrapperClass` 后加入：

```kotlin
private val mXCMethodSetMapLevel: XC_MethodHook = object : XCMethodHookCatching() {
    override fun beforeHookedMethodCatching(param: MethodHookParam) {
        super.beforeHookedMethodCatching(param)

        val miniMapViewWrapperClass = mMiniMapViewWrapperClass ?: return
        val thisObject = param.thisObject ?: return
        if (!miniMapViewWrapperClass.isInstance(thisObject)) {
            return
        }

        val level = param.args[0] as? Float ?: return
        XposedHelpers.setAdditionalInstanceField(thisObject, KEY_RAW_MAP_LEVEL, level)
        val adjustedLevel = level + MAP_LEVEL_OFFSET
        param.args[0] = if (adjustedLevel > MAX_MAP_LEVEL) {
            MAX_MAP_LEVEL
        } else {
            adjustedLevel
        }
    }
}
```

- [x] **Step 4: 添加 `mXCMethodEnableDynamicLevel`**

在 `mXCMethodSetMapLevel` 后加入：

```kotlin
private val mXCMethodEnableDynamicLevel: XC_MethodHook = object : XCMethodHookCatching() {
    override fun afterHookedMethodCatching(param: MethodHookParam) {
        super.afterHookedMethodCatching(param)

        val miniMapViewWrapperClass = mMiniMapViewWrapperClass ?: return
        val mapViewWrapper = XposedHelpers.getObjectField(param.thisObject, "mMapViewWrapper") ?: return
        if (!miniMapViewWrapperClass.isInstance(mapViewWrapper)) {
            return
        }

        val level = XposedHelpers.getAdditionalInstanceField(mapViewWrapper, KEY_RAW_MAP_LEVEL) as? Float ?: return
        XposedHelpers.setFloatField(param.thisObject, "mPreLevel", level)
    }
}
```

### Task 2: 收敛 MiniMapViewWrapperHook 为聚合入口

**Files:**
- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapViewWrapperHook.kt`

- [x] **Step 1: 替换 `invoke` 内容**

把当前 `invoke(loadPackageParam)` 中动态缩放注册逻辑删除，只保留两个功能 Hook 调用：

```kotlin
override fun invoke(loadPackageParam: XC_LoadPackage.LoadPackageParam) {

    MiniMapViewCenterHook(loadPackageParam = loadPackageParam)
    MiniMapDynamicZoomHook(loadPackageParam = loadPackageParam)
}
```

- [x] **Step 2: 删除动态缩放私有成员**

从 `MiniMapViewWrapperHook.kt` 删除以下成员：

```kotlin
private const val MAP_LEVEL_OFFSET = 1.5f
private const val MAX_MAP_LEVEL = 19.0f
private const val KEY_RAW_MAP_LEVEL = "xpit_raw_map_level"

private var mMiniMapViewWrapperClass: Class<*>? = null

private val mXCMethodSetMapLevel: XC_MethodHook
private val mXCMethodEnableDynamicLevel: XC_MethodHook
```

- [x] **Step 3: 删除不再使用的 imports**

`MiniMapViewWrapperHook.kt` 只保留：

```kotlin
import de.robv.android.xposed.callbacks.XC_LoadPackage
```

### Task 3: 验证

**Files:**
- Verify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapDynamicZoomHook.kt`
- Verify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapViewWrapperHook.kt`

- [x] **Step 1: 编译 Kotlin**

Run:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Expected:

```text
BUILD SUCCESSFUL
```

- [x] **Step 2: 运行 JVM 单元测试**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Expected:

```text
BUILD SUCCESSFUL
```

- [x] **Step 3: 人工复核 diff**

确认 diff 满足：

```text
MiniMapViewWrapperHook 只调用 MiniMapViewCenterHook 和 MiniMapDynamicZoomHook
MiniMapDynamicZoomHook 包含 setMapLevel 与 enableDynamicLevel 两个 Hook
MiniMapViewCenterHook 未被修改
没有改变 MAP_LEVEL_OFFSET、MAX_MAP_LEVEL、KEY_RAW_MAP_LEVEL 的值
没有新增包装方法、工具类或无关抽象
```

### Task 4: 提交

**Files:**
- Commit: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapDynamicZoomHook.kt`
- Commit: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapViewWrapperHook.kt`
- Commit: `docs/superpowers/specs/2026-04-28-minimap-dynamic-zoom-hook-split-design.md`
- Commit: `docs/superpowers/plans/2026-04-28-minimap-dynamic-zoom-hook-split.md`

- [ ] **Step 1: 提交代码**

Run:

```powershell
git add -- app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapDynamicZoomHook.kt app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapViewWrapperHook.kt
git commit -m ":recycle: 拆分小地图动态缩放 Hook"
```

- [ ] **Step 2: 提交计划文档**

Run:

```powershell
git add -- docs/superpowers/specs/2026-04-28-minimap-dynamic-zoom-hook-split-design.md docs/superpowers/plans/2026-04-28-minimap-dynamic-zoom-hook-split.md
git commit -m ":memo: 补充小地图动态缩放 Hook 拆分计划"
```
