# 小地图方向切换坐标重置修复 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复全屏小地图在地图 app 切换“车头方向 / 北在上”后坐标被宿主重置的问题，同时不影响普通 SD 小地图和主地图。

**Architecture:** 新增单一职责的 `MiniMapViewCenterHook` 承载小地图坐标/中心点修复逻辑，现有 `MiniMapViewWrapperHook` 只保留动态缩放逻辑并调用该 Hook。坐标 Hook 不再按宿主返回值 `396` 判断全屏地图，改为按 `MiniMapViewWrapper.getMapHeight() == 720` 判断，并在 `setMapMode(int, boolean, boolean)` 后仅对全屏高度重新应用左上角坐标。

**Tech Stack:** Kotlin、Xposed `XposedHelpers.findAndHookMethod`、`XCMethodHookCatching`、Gradle Wrapper、JADX 宿主签名核对。

---

## 文件结构

- Create: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapViewCenterHook.kt`
  - 只负责小地图坐标/中心点修复。
  - Hook `MiniMapViewWrapper.getDefaultMapViewTop()`，按地图高度判断全屏场景。
  - Hook `MiniMapViewWrapper.setMapMode(int, boolean, boolean)`，方向切换后按全屏高度重新应用地图 top。
- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapViewWrapperHook.kt`
  - 移除坐标/中心点 Hook 逻辑。
  - 保留现有动态缩放 Hook。
  - 在 `invoke` 开头调用 `MiniMapViewCenterHook(loadPackageParam)`。

JADX 已核对宿主签名：

```text
MiniMapViewWrapper.getDefaultMapViewTop(): int
MiniMapViewWrapper.setMapMode(int, boolean, boolean): void
MapViewWrapper.getMapHeight(): int
MapViewWrapper.getDefaultMapViewLeft(): int
MapViewWrapper.setMapViewLeftTop(int, int): void
```

## 实现任务

### Task 1: 用地图高度识别全屏小地图

**Files:**
- Create: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapViewCenterHook.kt`
- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapViewWrapperHook.kt`

- [x] **Step 1: 新建 `MiniMapViewCenterHook.kt`**

创建文件并加入 license、package 和 imports：

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

- [x] **Step 2: 新增 Hook 对象和常量**

在文件中加入：

```kotlin
object MiniMapViewCenterHook : (XC_LoadPackage.LoadPackageParam) -> Unit {

    override fun invoke(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                /* className    = */ "com.xiaopeng.montecarlo.navcore.mapdisplay.MiniMapViewWrapper",
                /* classLoader  = */ loadPackageParam.classLoader,
                /* methodName   = */ "getDefaultMapViewTop",
                /* ...parameterTypesAndCallback = */ mXCMethodGetDefaultMapViewTop
            )
        } catch (t: Throwable) {
            XposedBridge.log(t)
        }

        try {
            XposedHelpers.findAndHookMethod(
                /* className    = */ "com.xiaopeng.montecarlo.navcore.mapdisplay.MiniMapViewWrapper",
                /* classLoader  = */ loadPackageParam.classLoader,
                /* methodName   = */ "setMapMode",
                /* ...parameterTypesAndCallback = */ Int::class.javaPrimitiveType!!,
                Boolean::class.javaPrimitiveType!!,
                Boolean::class.javaPrimitiveType!!,
                mXCMethodSetMapMode
            )
        } catch (t: Throwable) {
            XposedBridge.log(t)
        }
    }

    private const val FULL_MAP_HEIGHT = 720
    private const val FULL_MAP_VIEW_TOP = 560
}
```

- [x] **Step 3: 添加 `mXCMethodGetDefaultMapViewTop`**

在 `MiniMapViewCenterHook` 常量后加入：

```kotlin
private val mXCMethodGetDefaultMapViewTop: XC_MethodHook = object : XCMethodHookCatching() {
    override fun afterHookedMethodCatching(param: MethodHookParam) {
        super.afterHookedMethodCatching(param)

        val mapHeight = XposedHelpers.callMethod(param.thisObject, "getMapHeight") as? Int ?: return
        if (mapHeight == FULL_MAP_HEIGHT) {
            param.result = FULL_MAP_VIEW_TOP
        }
    }
}
```

- [x] **Step 4: 从 `MiniMapViewWrapperHook` 移除旧坐标 Hook**

删除 `MiniMapViewWrapperHook.invoke` 中注册 `getDefaultMapViewTop` 的第一个 `try/catch` 块，并删除 `mXCMethodGetDefaultMapViewTop` 对象。

- [x] **Step 5: 在 `MiniMapViewWrapperHook.invoke` 开头调用中心点 Hook**

在 `invoke(loadPackageParam)` 开头加入：

```kotlin
MiniMapViewCenterHook(loadPackageParam = loadPackageParam)
```

注意：

- 不使用 `runCatching` 包装。
- 坐标/中心点逻辑只在 `MiniMapViewCenterHook` 中。
- 保留 `XCMethodHookCatching` 兜底异常处理。

### Task 2: 在方向切换后重新应用全屏坐标

**Files:**
- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapViewCenterHook.kt`

- [x] **Step 1: 确认 `setMapMode` Hook 独立注册**

确认 `MiniMapViewCenterHook.invoke` 中 `setMapMode` Hook 使用独立 `try/catch` 注册：

```kotlin
XposedHelpers.findAndHookMethod(
    /* className    = */ "com.xiaopeng.montecarlo.navcore.mapdisplay.MiniMapViewWrapper",
    /* classLoader  = */ loadPackageParam.classLoader,
    /* methodName   = */ "setMapMode",
    /* ...parameterTypesAndCallback = */ Int::class.javaPrimitiveType!!,
    Boolean::class.javaPrimitiveType!!,
    Boolean::class.javaPrimitiveType!!,
    mXCMethodSetMapMode
)
```

- [x] **Step 2: 新增 `mXCMethodSetMapMode`**

在 `MiniMapViewCenterHook` 中新增：

```kotlin
private val mXCMethodSetMapMode: XC_MethodHook = object : XCMethodHookCatching() {
    override fun afterHookedMethodCatching(param: MethodHookParam) {
        super.afterHookedMethodCatching(param)

        val mapHeight = XposedHelpers.callMethod(param.thisObject, "getMapHeight") as? Int ?: return
        if (mapHeight != FULL_MAP_HEIGHT) {
            return
        }

        val mapLeft = XposedHelpers.callMethod(param.thisObject, "getDefaultMapViewLeft") as? Int ?: return
        XposedHelpers.callMethod(param.thisObject, "setMapViewLeftTop", mapLeft, FULL_MAP_VIEW_TOP)
    }
}
```

注意：

- 只在 `getMapHeight() == 720` 时生效。
- 不对普通 `660` 高度小地图生效。
- 不 Hook 主地图 `MapViewWrapper.setMapMode(...)`。

### Task 3: 验证

**Files:**
- Verify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapViewCenterHook.kt`
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
getDefaultMapViewTop 不再按 396 判断
setMapMode Hook 只注册 MiniMapViewWrapper
setMapMode Hook 只在 getMapHeight() == 720 时调用 setMapViewLeftTop
坐标/中心点逻辑集中在 MiniMapViewCenterHook
动态缩放 setMapLevel / enableDynamicLevel 逻辑未被改动
没有无关抽象
```

### Task 4: 实车验证

**Files:**
- No source change.

- [ ] **Step 1: 验证全屏地图初始位置**

操作：

```text
启动车辆仪表主题模块
进入 D 档显示 MapFullFragment
观察全屏地图车位位置
```

Expected:

```text
全屏地图保持当前期望偏移，未回到普通小地图位置
```

- [ ] **Step 2: 验证北在上切换**

操作：

```text
打开地图 app
切换小地图方向为北在上
回到仪表全屏地图观察
```

Expected:

```text
全屏地图坐标不重置
```

- [ ] **Step 3: 验证车头方向切换**

操作：

```text
打开地图 app
切换小地图方向为车头方向
回到仪表全屏地图观察
```

Expected:

```text
全屏地图坐标不重置
```

- [ ] **Step 4: 验证普通小地图不受影响**

操作：

```text
隐藏 MapFullFragment 或切回普通小地图卡片
观察普通小地图显示位置
```

Expected:

```text
普通 SD mini map 卡片没有被下移到 560
```

### Task 5: 提交

**Files:**
- Commit: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapViewWrapperHook.kt`
- Commit: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapViewCenterHook.kt`
- Commit: `docs/superpowers/specs/2026-04-28-minimap-map-view-top-reset-debug.md`
- Commit: `docs/superpowers/plans/2026-04-28-minimap-map-view-top-reset.md`

- [ ] **Step 1: 提交代码**

Run:

```powershell
git add -- app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapViewCenterHook.kt app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapViewWrapperHook.kt
git commit -m ":bug: 修复小地图方向切换后的坐标重置"
```

- [ ] **Step 2: 提交文档**

Run:

```powershell
git add -- docs/superpowers/specs/2026-04-28-minimap-map-view-top-reset-debug.md docs/superpowers/plans/2026-04-28-minimap-map-view-top-reset.md
git commit -m ":memo: 补充小地图坐标重置修复计划"
```
