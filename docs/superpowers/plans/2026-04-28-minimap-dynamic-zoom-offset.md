# 小地图动态缩放偏移 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 保留宿主小地图动态缩放逻辑，并在最终应用 zoom level 时对 `MiniMapViewWrapper` 追加 `+1.5f` 偏移，上限限制为 `19.0f`。

**Architecture:** 只在现有 MonteCarlo Hook 文件中修改，不新增工具类、测试类、stub 或转发方法。`setMapLevel(float)` Hook 保存宿主原始等级并写入偏移后的实际等级；`DynamicLevelHelper.enableDynamicLevel()` Hook 在动态缩放启用后把 `mPreLevel` 恢复为原始等级，避免重复偏移，同时不污染 `getMapLevelF()` 的全局语义。注册时先挂 `enableDynamicLevel()`，再挂 `setMapLevel(float)`，确保动态基准修正不可用时不会启用偏移 Hook。

**Tech Stack:** Kotlin、Xposed `XposedHelpers.findAndHookMethod`、Gradle Wrapper、JADX 核对宿主签名。

---

## 文件结构

- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapViewWrapperHook.kt`
  - 保留现有 `getDefaultMapViewTop()` Hook。
  - 新增/保留 `MapViewWrapper.setMapLevel(float)` Hook，保存 raw level 并应用 `+1.5f`。
  - 新增 `DynamicLevelHelper.enableDynamicLevel()` Hook，只修正小地图动态 helper 的 `mPreLevel`。
  - 不 Hook `getMapLevelF()`，避免破坏宿主 `setMapLevel(float)` 内部的幂等检查。

JADX 已核对：

```text
MapViewWrapper.setMapLevel(float): void
DynamicLevelHelper.enableDynamicLevel(): void
DynamicLevelHelper.mMapViewWrapper: MapViewWrapper
DynamicLevelHelper.mPreLevel: float
```

## 实现任务

### Task 1: 调整 MiniMapViewWrapperHook

**Files:**
- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapViewWrapperHook.kt`

- [x] **Step 1: 注册目标 Hook**

在 `invoke(loadPackageParam)` 中保持直接注册，不新增包装方法。先注册 `DynamicLevelHelper.enableDynamicLevel()`，成功后再注册 `MapViewWrapper.setMapLevel(float)`：

```kotlin
XposedHelpers.findAndHookMethod(
    /* className    = */ "com.xiaopeng.montecarlo.dynamiclevel.base.DynamicLevelHelper",
    /* classLoader  = */ loadPackageParam.classLoader,
    /* methodName   = */ "enableDynamicLevel",
    /* ...parameterTypesAndCallback = */ mXCMethodEnableDynamicLevel
)
XposedHelpers.findAndHookMethod(
    /* className    = */ "com.xiaopeng.montecarlo.navcore.mapdisplay.MapViewWrapper",
    /* classLoader  = */ loadPackageParam.classLoader,
    /* methodName   = */ "setMapLevel",
    /* ...parameterTypesAndCallback = */ Float::class.javaPrimitiveType!!,
    mXCMethodSetMapLevel
)
```

注册块继续包在现有 `try/catch` 中，失败时 `XposedBridge.log(t)`。

- [x] **Step 2: 保存原始等级并应用偏移**

`mXCMethodSetMapLevel.beforeHookedMethodCatching` 只处理 `MiniMapViewWrapper` 实例：

```kotlin
val miniMapViewWrapperClass = mMiniMapViewWrapperClass ?: return
val thisObject = param.thisObject ?: return
if (!miniMapViewWrapperClass.isInstance(thisObject)) {
    return
}

val level = param.args.firstOrNull() as? Float ?: return
XposedHelpers.setAdditionalInstanceField(thisObject, KEY_RAW_MAP_LEVEL, level)
val adjustedLevel = level + MAP_LEVEL_OFFSET
param.args[0] = if (adjustedLevel > MAX_MAP_LEVEL) {
    MAX_MAP_LEVEL
} else {
    adjustedLevel
}
```

新增常量：

```kotlin
private const val KEY_RAW_MAP_LEVEL = "xpit_raw_map_level"
```

- [x] **Step 3: 修正动态缩放内部基准**

移除 `mXCMethodGetMapLevelF`。新增：

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

### Task 2: 验证

**Files:**
- Verify: `app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MiniMapViewWrapperHook.kt`

- [x] **Step 1: 编译 Kotlin**

Run:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Expected: PASS，没有 Kotlin 编译错误。

- [x] **Step 2: 运行 JVM 单元测试**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Expected: PASS，现有单元测试全部通过。

- [x] **Step 3: 人工检查 diff**

确认：

```text
没有 getMapLevelF Hook
setMapLevel Hook 只处理 MiniMapViewWrapper
enableDynamicLevel Hook 只处理 mMapViewWrapper 为 MiniMapViewWrapper 的动态 helper
没有新增工具类、测试类、stub 或布局改动
```

## 手动验证清单

- 全屏地图比修改前更接近，路口细节更清楚。
- 巡航/导航动态缩放不会越用越放大。
- 重复设置相同等级时不破坏宿主 `setMapLevel(float)` 幂等检查。
- 中控主地图没有被放大。
- 若宿主版本缺少目标类、字段或方法，宿主进程不应因 Hook 崩溃。
