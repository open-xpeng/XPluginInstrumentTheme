# Instrument / Montecarlo 关键链路日志补点 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 `instrument` 与 `montecarlo` 运行时代码补齐关键链路 SLF4J 摘要日志，便于后续从日志复盘模块入口、Hook 决策、页面切换、Surface 同步与地图修正过程。

**Architecture:** 直接在现有业务调用点补 `SLF4J` 日志，不新增 helper，不改 `commons`，不改 `stub`。按入口层、`instrument` 主链路、工具/控件支撑层、`montecarlo` Hook 层逐步补点，统一采用 `event=... key=value` 风格，并在需要额外取值或高频路径时使用 `isInfoEnabled` / `isDebugEnabled`。

**Tech Stack:** Kotlin, Android, Xposed, SLF4J, logback-android, Gradle

---

## File Structure

**Modify:**

- `app/src/main/java/com/xiaopeng/xposed/instrument/XposedMain.kt`
- `app/src/main/java/com/xiaopeng/xposed/montecarlo/XposedMain.kt`
- `app/src/main/java/com/xiaopeng/xposed/instrument/hooks/MainActivityHook.kt`
- `app/src/main/java/com/xiaopeng/xposed/instrument/hooks/MainFragmentHook.kt`
- `app/src/main/java/com/xiaopeng/xposed/instrument/fragments/MapFullFragment.kt`
- `app/src/main/java/com/xiaopeng/xposed/instrument/widgets/MapBaseInfoView.kt`
- `app/src/main/java/com/xiaopeng/xposed/instrument/widgets/MapLeftInfoViewGroup.kt`
- `app/src/main/java/com/xiaopeng/xposed/instrument/widgets/MapRightInfoViewGroup.kt`
- `app/src/main/java/com/xiaopeng/xposed/instrument/utils/LayoutInflaterXposed.kt`
- `app/src/main/java/com/xiaopeng/xposed/instrument/utils/LeftSubCardAutoSwitch.kt`
- `app/src/main/java/com/xiaopeng/xposed/instrument/utils/ReflectPrivateFieldDelegateStringOrNull.kt`
- `app/src/main/java/com/xiaopeng/xposed/instrument/utils/ReflectPrivateFieldDelegateLong.kt`
- `app/src/main/java/com/xiaopeng/xposed/instrument/constants/ConstantSurfaceViewManager.kt`
- `app/src/main/java/com/xiaopeng/xposed/montecarlo/hooks/MiniMapDynamicZoomHook.kt`
- `app/src/main/java/com/xiaopeng/xposed/montecarlo/hooks/MiniMapViewCenterHook.kt`

**Do not modify:**

- `app/src/main/java/com/xiaopeng/xposed/commons/**`
- `stub/src/main/java/**`

**Verification only:**

- `.\gradlew.bat :app:compileDebugKotlin`
- `.\gradlew.bat :app:testDebugUnitTest`

## Task 1: 模块入口层日志

**Files:**
- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/XposedMain.kt`
- Modify: `app/src/main/java/com/xiaopeng/xposed/montecarlo/XposedMain.kt`

- [ ] **Step 1: 在两个 `XposedMain` 增加 logger 字段**

```kotlin
private val mLogger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(this.javaClass.simpleName)
```

放置位置：

- `class XposedMain : IXposedHookLoadPackageCatching, IXposedHookZygoteInit { ... }`
- `class XposedMain : IXposedHookLoadPackageCatching { ... }`

- [ ] **Step 2: 在 `instrument/XposedMain.kt` 的 `initZygote` 中补初始化日志**

```kotlin
override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
    MODULE_PATH = startupParam.modulePath
    if (mLogger.isInfoEnabled) {
        mLogger.info(
            "event=module_init_zygote packageName={} modulePath={}",
            "com.xiaopeng.instrument",
            MODULE_PATH
        )
    }
}
```

- [ ] **Step 3: 在两个 `handleLoadPackageCatching(...)` 中补包命中与跳过日志**

```kotlin
if (loadPackageParam.packageName != "com.xiaopeng.instrument") {
    return
}

if (mLogger.isInfoEnabled) {
    mLogger.info(
        "event=module_load_package packageName={} process={}",
        loadPackageParam.packageName,
        loadPackageParam.processName
    )
}
```

`montecarlo` 同步改成：

```kotlin
if (loadPackageParam.packageName != "com.xiaopeng.montecarlo") {
    return
}
```

- [ ] **Step 4: 在 Hook 注册点补 `hook_registered` 与 `hook_register_completed`**

```kotlin
XposedHelpersWrapper.findAndHookMethod(
    Application::class.java,
    "onCreate",
    XposedCallbackCommonsApplicationOnCreate(mLoadPackageParam = loadPackageParam)
)
if (mLogger.isInfoEnabled) {
    mLogger.info("event=hook_registered targetClass={} targetMethod={}", "Application", "onCreate")
}

MainActivityHook(loadPackageParam = loadPackageParam)
if (mLogger.isInfoEnabled) {
    mLogger.info("event=hook_registered targetClass={} targetMethod={}", "MainActivity", "onCreate/showFragmentByClass")
}

MainFragmentHook(loadPackageParam = loadPackageParam)
if (mLogger.isInfoEnabled) {
    mLogger.info("event=hook_registered targetClass={} targetMethod={}", "MainFragment", "onViewCreated/onResume/onHiddenChanged/showLeftSubCardView")
    mLogger.info("event=hook_register_completed packageName={}", loadPackageParam.packageName)
}
```

`montecarlo` 对应补：

```kotlin
MiniMapViewCenterHook(loadPackageParam = loadPackageParam)
MiniMapDynamicZoomHook(loadPackageParam = loadPackageParam)
if (mLogger.isInfoEnabled) {
    mLogger.info("event=hook_register_completed packageName={}", loadPackageParam.packageName)
}
```

- [ ] **Step 5: 运行单文件编译检查**

Run:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Expected:

- `BUILD SUCCESSFUL`
- 没有 `XposedMain.kt` 相关语法错误

## Task 2: `instrument` Hook 主链路日志

**Files:**
- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/hooks/MainActivityHook.kt`
- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/hooks/MainFragmentHook.kt`

- [ ] **Step 1: 在 `MainActivityHook.kt` 增加 logger 并记录 Hook 注册**

```kotlin
private val mLogger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(this.javaClass.simpleName)

override fun invoke(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
    XposedHelpersWrapper.findAndHookMethod(
        MainActivity::class.java,
        "onCreate",
        Bundle::class.java,
        mXCMethodOnViewCreated
    )
    XposedHelpersWrapper.findAndHookMethod(
        MainActivity::class.java,
        "showFragmentByClass",
        Class::class.java,
        mXCMethodShowFragmentByClass
    )
    if (mLogger.isInfoEnabled) {
        mLogger.info("event=hook_register_completed targetClass={}", MainActivity::class.java.name)
    }
}
```

- [ ] **Step 2: 为 `showFragmentByClass` 前后 Hook 增加入口、跳过与结果日志**

```kotlin
if (mLogger.isInfoEnabled) {
    mLogger.info(
        "event=hook_enter targetMethod={} targetClass={} currentFragment={} gear={}",
        "showFragmentByClass",
        targetFragmentClass.name,
        activity.mCurrentFragmentName,
        infoViewModel.gearLiveData.value
    )
}

if (targetFragmentClass != MainFragment::class.java) {
    if (mLogger.isInfoEnabled) {
        mLogger.info("event=hook_skipped targetMethod={} reason={}", "showFragmentByClass", "target_fragment_not_main_fragment")
    }
    return
}
```

`afterHookedMethodCatching(...)` 中补：

```kotlin
if (fragment.isHidden) {
    if (mLogger.isInfoEnabled) {
        mLogger.info("event=hook_skipped targetMethod={} reason={}", "showFragmentByClass.after", "sr_fragment_hidden")
    }
    return
}

if (mLogger.isInfoEnabled) {
    mLogger.info("event=hook_value_applied targetMethod={} result={} surface={}", "showFragmentByClass.after", "start_sr_change_service", srSurface)
}
```

- [ ] **Step 3: 为 `onCreate` 后置 Hook 与挡位 observer 增加初始化和切换日志**

```kotlin
if (mLogger.isInfoEnabled) {
    mLogger.info(
        "event=hook_state_initialized targetMethod={} fragmentTag={} modulePath={}",
        "onCreate",
        mFragmentTag,
        XposedMain.MODULE_PATH
    )
}
```

`OnGearLiveDataChanged.onChanged(...)` 中补：

```kotlin
if (mLogger.isInfoEnabled) {
    mLogger.info("event=hook_state_changed targetMethod={} newValue={}", "gearLiveData", value)
}
```

- [ ] **Step 4: 为 `showMapFullFragment(...)` 和 `hideMapFullFragment(...)` 增加 Fragment 切换日志**

```kotlin
if (currentFragmentName == mFragmentTag) {
    if (mLogger.isInfoEnabled) {
        mLogger.info("event=hook_skipped targetMethod={} reason={}", "showMapFullFragment", "already_showing")
    }
    return
}

if (mLogger.isInfoEnabled) {
    mLogger.info(
        "event=fragment_switch_started fragment={} previousValue={}",
        mFragmentTag,
        currentFragmentName
    )
}
```

提交事务后补：

```kotlin
if (mLogger.isInfoEnabled) {
    mLogger.info(
        "event=fragment_switch_completed fragment={} newValue={}",
        mFragmentTag,
        activity.mCurrentFragmentName
    )
}
```

- [ ] **Step 5: 为 `MainFragmentHook.kt` 增加入口、决策、状态变化日志**

补充的核心片段应覆盖：

```kotlin
if (mLogger.isInfoEnabled) {
    mLogger.info(
        "event=hook_state_initialized fragment={} turnGuidanceVisible={} tbtVisible={} navActive={}",
        fragment.javaClass.simpleName,
        naviGuidenceVisibility.value == true,
        naviTBtVisibility.value == true,
        XposedHelpers.getAdditionalInstanceField(fragment, KEY_NAV_ACTIVE)
    )
}
```

```kotlin
if (fragment.isHidden) {
    if (mLogger.isInfoEnabled) {
        mLogger.info(
            "event=hook_skipped targetMethod={} reason={} rawValue={}",
            "showLeftSubCardView",
            "fragment_hidden",
            rawCardIndex
        )
    }
    methodHookParam.result = null
    return
}
```

```kotlin
if (mLogger.isInfoEnabled) {
    mLogger.info(
        "event=hook_value_resolved targetMethod={} rawValue={} effectiveValue={} navActive={}",
        "showLeftSubCardView",
        rawCardIndex,
        effectiveCardIndex,
        isNavigationActive
    )
}
```

```kotlin
if (previous != isNavigationActive && mLogger.isInfoEnabled) {
    mLogger.info(
        "event=hook_state_changed fragment={} previousValue={} newValue={} turnGuidanceVisible={} tbtVisible={}",
        fragment.javaClass.simpleName,
        previous,
        isNavigationActive,
        isTurnGuidanceVisible,
        isTbtVisible
    )
}
```

```kotlin
if (mLogger.isInfoEnabled) {
    mLogger.info(
        "event=hook_value_applied targetMethod={} rawValue={} navActive={} surfaceType={}",
        "syncLeftMapSurfaceType",
        rawCardIndex,
        isNavigationActive,
        leftMapSurfaceType
    )
}
```

- [ ] **Step 6: 运行 Kotlin 编译验证 `instrument` 主链路**

Run:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Expected:

- `BUILD SUCCESSFUL`
- `MainActivityHook.kt` 和 `MainFragmentHook.kt` 无编译错误

## Task 3: `instrument` Fragment、控件与工具层日志

**Files:**
- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/fragments/MapFullFragment.kt`
- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/widgets/MapBaseInfoView.kt`
- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/widgets/MapLeftInfoViewGroup.kt`
- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/widgets/MapRightInfoViewGroup.kt`
- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/utils/LayoutInflaterXposed.kt`
- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/utils/LeftSubCardAutoSwitch.kt`
- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/utils/ReflectPrivateFieldDelegateStringOrNull.kt`
- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/utils/ReflectPrivateFieldDelegateLong.kt`
- Modify: `app/src/main/java/com/xiaopeng/xposed/instrument/constants/ConstantSurfaceViewManager.kt`

- [ ] **Step 1: 为 `MapFullFragment.kt` 的创建、导航初始化和卡片渲染补日志**

在 `onViewCreated(...)`、`initLeftSubCardAutoSwitch()`、`renderLeftSubCard(...)` 中加入：

```kotlin
if (mLogger.isInfoEnabled) {
    mLogger.info(
        "event=fragment_view_created fragment={} width={} height={}",
        this.javaClass.simpleName,
        mWidgetMapWidth,
        mWidgetMapHeight
    )
}
```

```kotlin
if (mLogger.isInfoEnabled) {
    mLogger.info(
        "event=hook_state_initialized fragment={} turnGuidanceVisible={} tbtVisible={} navActive={}",
        this.javaClass.simpleName,
        mIsTurnGuidanceVisible,
        mIsTbtVisible,
        mIsNavigationActive
    )
}
```

```kotlin
if (mLogger.isInfoEnabled) {
    mLogger.info(
        "event=card_render_resolved fragment={} rawValue={} effectiveValue={} navActive={}",
        this.javaClass.simpleName,
        rawCardIndex,
        effectiveCardIndex,
        mIsNavigationActive
    )
}
```

- [ ] **Step 2: 为 `MapFullFragment.kt` 的 hidden 恢复和 service 启动补日志**

```kotlin
if (mLogger.isInfoEnabled) {
    mLogger.info(
        "event=fragment_hidden_changed fragment={} hidden={} rawValue={}",
        this.javaClass.simpleName,
        hidden,
        mRawLeftSubCardIndex
    )
}
```

```kotlin
if (BuildConfig.IS_RUNNING_TEST_PLATFORM) {
    if (mLogger.isInfoEnabled) {
        mLogger.info(
            "event=surface_change_service_skipped fragment={} reason={} width={} height={}",
            this.javaClass.simpleName,
            "running_test_platform",
            width,
            height
        )
    }
    return
}
```

```kotlin
if (mLogger.isInfoEnabled) {
    mLogger.info(
        "event=surface_change_service_start fragment={} width={} height={} action={}",
        this.javaClass.simpleName,
        width,
        height,
        ConstantSurfaceViewManager.ACTION_MAP_SURFACE_CHANGED
    )
}
```

成功后补：

```kotlin
mLogger.info(
    "event=surface_change_service_completed fragment={} width={} height={}",
    this.javaClass.simpleName,
    width,
    height
)
```

- [ ] **Step 3: 为 `MapBaseInfoView.kt` 增加列表显隐和数据更新日志**

先新增 logger：

```kotlin
protected val mLogger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(this.javaClass.simpleName)
```

再补：

```kotlin
if (mLogger.isInfoEnabled) {
    mLogger.info("event=hook_value_applied targetMethod={} result={}", "showList", z)
}
```

```kotlin
if (mLogger.isInfoEnabled) {
    mLogger.info(
        "event=hook_value_applied targetMethod={} result={} selectIndex={}",
        "updateListData",
        infoContainBean.infoBeanList.size,
        infoContainBean.selectIndex
    )
}
```

`MapLeftInfoViewGroup.kt` / `MapRightInfoViewGroup.kt` 本次只补 logger 字段和必要的构造完成日志，不改它们的 `position` 行为。

- [ ] **Step 4: 为 `LayoutInflaterXposed.kt`、`LeftSubCardAutoSwitch.kt`、`ConstantSurfaceViewManager.kt` 增加关键工具日志**

`LayoutInflaterXposed.kt`：

```kotlin
private val mLogger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(this.javaClass.simpleName)
```

```kotlin
if (mLogger.isDebugEnabled) {
    mLogger.debug("event=layout_inflate_intercepted targetClass={} result={}", name, view1 != null || view2 != null)
}
```

`LeftSubCardAutoSwitch.kt`：

```kotlin
private val mLogger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(this.javaClass.simpleName)
```

```kotlin
if (mLogger.isDebugEnabled) {
    mLogger.debug(
        "event=auto_switch_resolved rawValue={} effectiveValue={} navActive={}",
        rawCardIndex,
        result,
        isNavigationActive
    )
}
```

`ConstantSurfaceViewManager.kt`：

```kotlin
private val mLogger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(this.javaClass.simpleName)
```

按每个 `by lazy` 在首次求值时补：

```kotlin
val ACTION_MAP_SURFACE_CHANGED: String by lazy {
    val value: String = mReflectSurfaceViewManager.get("ACTION_MAP_SURFACE_CHANGED")
    if (mLogger.isInfoEnabled) {
        mLogger.info("event=reflect_value_initialized key={} value={}", "ACTION_MAP_SURFACE_CHANGED", value)
    }
    value
}
```

- [ ] **Step 5: 为反射代理类增加字段读取与失败日志**

在 `ReflectPrivateFieldDelegateStringOrNull.kt` 与 `ReflectPrivateFieldDelegateLong.kt` 中增加：

```kotlin
private val mLogger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(this.javaClass.simpleName)
```

首次成功读取时：

```kotlin
if (mLogger.isInfoEnabled) {
    mLogger.info("event=reflect_field_read key={} result={}", name, value)
}
```

失败时：

```kotlin
mLogger.error("event=reflect_field_read key={} result={}", name, "failed", throwable)
```

- [ ] **Step 6: 运行 Kotlin 编译验证 `instrument` 支撑层**

Run:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Expected:

- `BUILD SUCCESSFUL`
- 支撑层日志补点没有引入类型错误或构造时机问题

## Task 4: `montecarlo` Hook 日志

**Files:**
- Modify: `app/src/main/java/com/xiaopeng/xposed/montecarlo/hooks/MiniMapDynamicZoomHook.kt`
- Modify: `app/src/main/java/com/xiaopeng/xposed/montecarlo/hooks/MiniMapViewCenterHook.kt`

- [ ] **Step 1: 在 `MiniMapDynamicZoomHook.kt` 中补类解析和 Hook 注册日志**

```kotlin
mMiniMapViewWrapperClass = XposedHelpers.findClass(
    "com.xiaopeng.montecarlo.navcore.mapdisplay.MiniMapViewWrapper",
    loadPackageParam.classLoader
)
if (mLogger.isInfoEnabled) {
    mLogger.info(
        "event=hook_state_initialized targetClass={} result={}",
        "com.xiaopeng.montecarlo.navcore.mapdisplay.MiniMapViewWrapper",
        mMiniMapViewWrapperClass != null
    )
}
```

- [ ] **Step 2: 为 `setMapLevel` 增加原始值、跳过原因和应用值日志**

```kotlin
if (!miniMapViewWrapperClass.isInstance(thisObject)) {
    if (mLogger.isDebugEnabled) {
        mLogger.debug("event=hook_skipped targetMethod={} reason={}", "setMapLevel", "not_mini_map_instance")
    }
    return
}
```

```kotlin
if (mLogger.isInfoEnabled) {
    mLogger.info(
        "event=hook_value_resolved targetMethod={} rawValue={} effectiveValue={}",
        "setMapLevel",
        level,
        methodHookParam.args[0]
    )
}
```

- [ ] **Step 3: 为 `enableDynamicLevel` 增加读取结果与写回日志**

```kotlin
if (!miniMapViewWrapperClass.isInstance(mapViewWrapper)) {
    if (mLogger.isDebugEnabled) {
        mLogger.debug("event=hook_skipped targetMethod={} reason={}", "enableDynamicLevel", "map_view_wrapper_not_mini_map")
    }
    return
}
```

```kotlin
if (mLogger.isInfoEnabled) {
    mLogger.info(
        "event=hook_value_applied targetMethod={} rawValue={} newValue={}",
        "enableDynamicLevel",
        level,
        level
    )
}
```

- [ ] **Step 4: 在 `MiniMapViewCenterHook.kt` 增加 logger、Hook 注册和地图中心修正日志**

先新增：

```kotlin
private val mLogger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(this.javaClass.simpleName)
```

注册后补：

```kotlin
if (mLogger.isInfoEnabled) {
    mLogger.info("event=hook_register_completed targetClass={}", "com.xiaopeng.montecarlo.navcore.mapdisplay.MiniMapViewWrapper")
}
```

`getDefaultMapViewTop` 中补：

```kotlin
if (mapHeight == FULL_MAP_HEIGHT) {
    methodHookParam.result = FULL_MAP_VIEW_TOP
    if (mLogger.isInfoEnabled) {
        mLogger.info(
            "event=hook_value_applied targetMethod={} rawValue={} newValue={}",
            "getDefaultMapViewTop",
            mapHeight,
            FULL_MAP_VIEW_TOP
        )
    }
}
```

`setMapMode` 中补：

```kotlin
if (mapHeight != FULL_MAP_HEIGHT) {
    if (mLogger.isDebugEnabled) {
        mLogger.debug("event=hook_skipped targetMethod={} reason={} rawValue={}", "setMapMode", "map_height_not_full", mapHeight)
    }
    return
}
```

```kotlin
if (mLogger.isInfoEnabled) {
    mLogger.info(
        "event=hook_value_applied targetMethod={} rawValue={} newValue={}",
        "setMapMode",
        mapLeft,
        FULL_MAP_VIEW_TOP
    )
}
```

- [ ] **Step 5: 运行 Kotlin 编译验证 `montecarlo` 日志补点**

Run:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Expected:

- `BUILD SUCCESSFUL`
- `MiniMapDynamicZoomHook.kt` 与 `MiniMapViewCenterHook.kt` 无编译错误

## Task 5: 最终验证与整理

**Files:**
- Verify only: all files modified in Tasks 1-4

- [ ] **Step 1: 运行完整 Kotlin 编译**

Run:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Expected:

- `BUILD SUCCESSFUL`

- [ ] **Step 2: 运行现有单元测试做回归确认**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Expected:

- `BUILD SUCCESSFUL`
- 无新增单测失败

- [ ] **Step 3: 搜索日志格式，检查没有引入 helper 或 `commons` 改动**

Run:

```powershell
rg -n "event=" app/src/main/java/com/xiaopeng/xposed/instrument app/src/main/java/com/xiaopeng/xposed/montecarlo
```

Expected:

- 新增日志集中在 `instrument` 与 `montecarlo`
- 不出现 `commons/` 与 `stub/` 改动

- [ ] **Step 4: 检查关键日志是否遵守级别判断约定**

Run:

```powershell
rg -n "isInfoEnabled|isDebugEnabled|LoggerFactory.getLogger" app/src/main/java/com/xiaopeng/xposed/instrument app/src/main/java/com/xiaopeng/xposed/montecarlo
```

Expected:

- 复杂摘要日志前存在 `isInfoEnabled` / `isDebugEnabled`
- logger 获取方式与现有项目保持一致

- [ ] **Step 5: 不执行提交，整理变更说明**

输出说明应包含：

```text
1. 入口层增加了哪些 event
2. instrument 主链路增加了哪些 event
3. montecarlo Hook 增加了哪些 event
4. 运行了哪些验证命令及结果
```

说明：

- 仓库约束要求不要自动提交代码
- 本计划执行结束后只汇报结果，不做 `git commit`

## Self-Review

### Spec coverage

- 模块入口日志：Task 1 覆盖
- `instrument` Hook 决策日志：Task 2 覆盖
- `MapFullFragment` 与控件/工具日志：Task 3 覆盖
- `montecarlo` Hook 日志：Task 4 覆盖
- 编译与回归验证：Task 5 覆盖

### Placeholder scan

- 本计划未使用 `TODO`、`TBD`、`implement later`
- 所有步骤都给出了具体文件、代码片段和验证命令

### Type consistency

- 日志统一使用 `mLogger`
- 统一使用 `event=... key=value` 消息风格
- 复杂日志统一要求 `isInfoEnabled` / `isDebugEnabled`

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-05-18-instrument-montecarlo-key-logging.md`. Two execution options:

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

Which approach?
