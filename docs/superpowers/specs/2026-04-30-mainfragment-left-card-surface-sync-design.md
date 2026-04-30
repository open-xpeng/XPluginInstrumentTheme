# MainFragment 左侧卡片 Surface 状态同步设计

## 背景

当前模块已经实现了“左侧导航卡片在无导航引导时自动显示音乐卡片”的逻辑。该逻辑作用于宿主 `MainFragment.showLeftSubCardView(int)`：当宿主原始左侧子卡片为导航卡片 `0`，但当前没有真实转向引导时，模块把最终显示参数改为音乐卡片 `1`。

实际车辆上出现一个恢复场景问题：

- 锁车去上班
- 车辆休眠
- 下班解锁上车
- 仪表恢复到宿主 `MainFragment`
- 左侧同时出现地图和音乐控件，二者重叠显示

图片确认该问题发生在宿主 `MainFragment`，不是模块自定义的 `MapFullFragment`。

## 目标

- 修复 `MainFragment` 中左侧音乐卡片与地图 Surface 在休眠恢复后重叠的问题
- 保持现有“无导航时左侧导航卡片自动显示音乐”的行为
- 保持有导航引导时左侧导航卡片正常显示地图
- 不改变 `MapFullFragment` 的全屏地图 Surface 流程
- 不改写宿主 `InfoViewModel` 的原始卡片选择数据

## 非目标

- 不重构宿主左右卡片逻辑
- 不改变右侧卡片行为
- 不修复宿主右侧地图卡片“先显示 View、后设置 Surface 类型”的顺序问题
- 不引入新的卡片配置策略
- 不直接修改宿主 APK 或 stub 之外的宿主类实现

## 现状分析

### 宿主左侧卡片链路

JADX 中 `InfoViewModel.onLeftCardIndex(int)` 的逻辑是：

```java
if (i == 0) {
    SurfaceViewManager.getInstance().setLeftViewType(2);
} else {
    SurfaceViewManager.getInstance().setLeftViewType(0);
}
showSubCard(0, i);
```

这说明宿主在左侧卡片切换时维护了两类状态：

- 卡片 UI 状态：通过 `showSubCard(0, i)` 最终驱动 `MainFragment.showLeftSubCardView(i)`
- 地图 Surface 状态：通过 `SurfaceViewManager.setLeftViewType(...)` 记录当前左侧是否是地图 Surface

当 `i == 0` 时，宿主认为左侧是地图卡片，并把 `mLeftViewType` 设为 `2`。

当 `i != 0` 时，宿主认为左侧不是地图卡片，并把 `mLeftViewType` 设为 `0`。

### 宿主卡片 View 互斥逻辑

JADX 中 `BaseInfoView.showSubCardView(int)` 的逻辑是：

```java
if (i == 0) {
    showMapCarView();
    return;
}
if (i == 1) {
    showMediaCarView();
    return;
}
```

其中：

- `showMapCarView()` 会隐藏媒体卡片并显示地图卡片
- `showMediaCarView()` 会隐藏地图卡片并显示媒体卡片

因此，普通 View 层的地图卡片和音乐卡片本身是互斥的。当前重叠现象不应优先解释为 `View.setVisibility()` 互斥失败。

需要区分的是：`BaseInfoView.showMapCarView()` 只负责 View 可见性；宿主左侧实际实例是 `LeftInfoViewGroup`，它覆写了 `showMapCarView()`，在 `BaseConfig.isSupportNaviSR()` 为真时还会调用：

```java
SurfaceViewManager.getInstance().setLeftSDSurface(this.mMapCardView);
SurfaceViewManager.getInstance().startLeftSDChangeService();
```

因此“显示左侧地图卡片是否会启动 Surface”取决于动态分派到 `LeftInfoViewGroup.showMapCarView()`，并且取决于调用前 `mLeftViewType` 是否已经是非零地图类型。

### 宿主 Surface 恢复链路

JADX 中 `SurfaceViewManager.resumeMainMap()` 的逻辑是：

```java
if (this.mRightViewType != 0) {
    startRightSDChangeService();
} else if (this.mLeftViewType != 0) {
    startLeftSDChangeService();
} else {
    XILog.i(str, "There is no map in main fragment ");
}
```

这说明休眠恢复时，宿主不看当前实际显示的是音乐卡片还是地图卡片，而是只看 `mLeftViewType` / `mRightViewType`。

只要 `mLeftViewType != 0`，恢复时就会尝试重新拉起左侧地图 Surface。

同时，JADX 中 `MainFragment.onResume()` 会直接调用 `resumeMainMap()`：

```java
public void onResume() {
    super.onResume();
    MeterSDRender.onResume();
    if (BaseConfig.getInstance().isSupportNaviSR()) {
        XILog.i(TAG, "SurfaceViewManager resumeMainMap");
        SurfaceViewManager.getInstance().resumeMainMap();
    }
}
```

这条路径对锁车休眠恢复尤其关键。锁车恢复可能只走 `onPause()` / `onResume()`，并不一定会重新触发 `showLeftSubCardView(int)` 或 `onHiddenChanged(false)`。因此，如果只在 `showLeftSubCardView(int)` Hook 中同步 `mLeftViewType`，恢复时仍可能让 `onResume()` 直接用旧的 `mLeftViewType == 2` 拉起左侧地图 Surface。

### 模块当前行为

模块当前在 `MainFragmentHook.showLeftSubCardView(int)` 前置 Hook 中做了显示参数覆盖：

```kotlin
val isNavigationActive = XposedHelpers.getAdditionalInstanceField(fragment, KEY_NAV_ACTIVE) as? Boolean ?: false
param.args[0] = LeftSubCardAutoSwitch.resolve(rawCardIndex = rawCardIndex, isNavigationActive = isNavigationActive)
```

当宿主原始卡片是 `0`，但没有导航引导时：

- 宿主原始语义：左侧是地图
- 宿主 Surface 状态：`mLeftViewType == 2`
- 模块最终 UI：把 `showLeftSubCardView(0)` 改成 `showLeftSubCardView(1)`
- 用户看到：音乐卡片

这会造成 UI 状态和 Surface 状态不一致。

## 根因

根因是模块只覆盖了左侧卡片 UI 的最终显示参数，没有同步修正宿主 `SurfaceViewManager` 中的左侧地图 Surface 状态。

具体状态错位如下：

| 状态来源 | 恢复前值 | 含义 |
| --- | --- | --- |
| 宿主原始卡片索引 | `0` | 左侧选择了导航卡片 |
| 模块最终显示索引 | `1` | 因无导航引导，显示音乐卡片 |
| `SurfaceViewManager.mLeftViewType` | `2` | 宿主仍认为左侧是地图 Surface |

锁车休眠恢复后，`resumeMainMap()` 看到 `mLeftViewType == 2`，重新启动左侧地图 Surface。此时模块 UI 仍显示音乐卡片，于是形成“音乐卡片 + 地图 Surface”重叠。

## 左右卡片行为差异

JADX 还显示，宿主左右卡片设置地图时顺序不一致。

左侧：

```java
if (i == 0) {
    SurfaceViewManager.getInstance().setLeftViewType(2);
} else {
    SurfaceViewManager.getInstance().setLeftViewType(0);
}
showSubCard(0, i);
```

右侧：

```java
showSubCard(1, i);
if (i == 0) {
    SurfaceViewManager.getInstance().setRightViewType(3);
} else {
    SurfaceViewManager.getInstance().setRightViewType(0);
}
```

左侧是先设置 Surface 类型，再显示卡片。右侧是先显示卡片，再设置 Surface 类型。

这会导致地图卡片效果不一致：

- 左侧显示地图时，动态分派到 `LeftInfoViewGroup.showMapCarView()`，内部调用 `startLeftSDChangeService()`；宿主原始顺序下此时 `mLeftViewType` 已经是 `2`，地图 Surface 可以立即启动
- 右侧显示地图时，动态分派到 `RightInfoViewGroup.showMapCarView()`，内部调用 `startRightSDChangeService()`；宿主原始顺序下此时 `mRightViewType` 可能仍是旧值 `0`，地图 Surface 可能不会立即启动

这个差异是宿主原始行为。本次问题发生在左侧自动切换路径上，修复应聚焦左侧 Surface 状态同步，不扩大到右侧重构。

## 设计原则

1. 卡片 UI 状态和地图 Surface 状态必须以模块最终显示结果为准
2. 不改宿主原始数据源，只在 Hook 边界同步宿主状态
3. 只处理左侧自动切换引入的不一致
4. 所有决策逻辑优先放在纯 Kotlin 工具类中，便于单元测试
5. Xposed Hook 只负责读取宿主状态、调用宿主 API 和应用纯逻辑结果

## 方案

### 状态模型

新增一个纯逻辑决策：根据“原始左侧子卡片索引”和“导航是否激活”，计算左侧地图 Surface 类型。

规则如下：

| 原始卡片 | 导航激活 | 最终显示卡片 | 左侧 Surface 类型 |
| --- | --- | --- | --- |
| `0` 导航 | `true` | `0` 导航 | `2` |
| `0` 导航 | `false` | `1` 音乐 | `0` |
| `1` 音乐 | 任意 | `1` 音乐 | `0` |
| `2/3/4/5` 其他 | 任意 | 原始卡片 | `0` |

这里的 `2` 来自宿主 `InfoViewModel.onLeftCardIndex(0)` 中的 `setLeftViewType(2)`，表示左侧 SD 地图 Surface。

### 代码边界

`LeftSubCardAutoSwitch` 负责纯决策：

- `resolve(rawCardIndex, isNavigationActive)`：决定最终显示哪个卡片
- `resolveLeftMapSurfaceType(rawCardIndex, isNavigationActive)`：决定左侧地图 Surface 类型

`MainFragmentHook` 负责应用决策。同步点必须覆盖两个入口：

#### `showLeftSubCardView(int)` 前同步

1. 读取宿主传入的 `rawCardIndex`
2. 读取当前 `isNavigationActive`
3. 计算 `effectiveCardIndex`
4. 计算 `leftMapSurfaceType`
5. 在调用宿主原始 `showLeftSubCardView(int)` 之前，调用 `SurfaceViewManager.getInstance().setLeftViewType(leftMapSurfaceType)`
6. 把 `param.args[0]` 改为 `effectiveCardIndex`

这个顺序是设计约束，不只是实现细节：

- 如果最终显示音乐，先把 `mLeftViewType` 设为 `0`，宿主后续恢复路径不会再拉起左侧地图 Surface
- 如果最终显示地图，先把 `mLeftViewType` 设为 `2`，随后宿主原始 `showLeftSubCardView(0)` 会进入 `LeftInfoViewGroup.showMapCarView()`，它调用 `startLeftSDChangeService()` 时能看到正确的非零地图类型

#### `onResume()` 前同步

`MainFragment.onResume()` 的宿主原始实现会直接调用 `resumeMainMap()`。因此模块需要 Hook `MainFragment.onResume()`，并在宿主原始 `onResume()` 执行前同步左侧 Surface 状态：

1. 读取已保存的 `KEY_RAW_LEFT_SUB_CARD`
2. 如果原始左侧卡片尚未记录，则不主动修改宿主 Surface 状态
3. 读取当前 `KEY_NAV_ACTIVE`
4. 计算 `leftMapSurfaceType`
5. 调用 `SurfaceViewManager.getInstance().setLeftViewType(leftMapSurfaceType)`
6. 让宿主原始 `onResume()` 继续执行

这样宿主 `onResume()` 随后调用 `resumeMainMap()` 时，会看到模块最终显示结果对应的左侧 Surface 状态。

### 隐藏 Fragment 的处理

现有 Hook 在 `fragment.isHidden` 时会跳过显示：

```kotlin
if (fragment.isHidden) {
    param.result = null
    return
}
```

设计上保持这个行为，不在隐藏状态下同步 `SurfaceViewManager`。

原因：

- 隐藏状态下不应触发可见页面的 Surface 变化
- `onHiddenChanged(false)` 已经会 replay 原始卡片
- replay 时会再次经过 `showLeftSubCardView(rawCardIndex)` Hook，并在可见状态下同步 Surface

### 恢复路径

恢复路径分两类，不能假设都会 replay 卡片显示。

#### `onHiddenChanged(false)` 路径

1. 模块拿到保存的原始左侧卡片索引
2. 重新调用 `showLeftSubCardView(rawCardIndex)`
3. Hook 重新计算最终显示卡片
4. Hook 同步 `mLeftViewType`
5. 再调用宿主原方法显示卡片
6. 随后的 `resumeMainMap()` 将看到正确的 `mLeftViewType`

#### `onResume()` 路径

1. 宿主 `MainFragment.onResume()` 即将执行
2. 模块在 before Hook 中读取已保存的原始左侧卡片索引
3. 如果原始左侧卡片已保存，模块按当前导航状态同步 `mLeftViewType`
4. 宿主原始 `onResume()` 继续执行
5. 宿主调用 `SurfaceViewManager.resumeMainMap()`
6. `resumeMainMap()` 看到同步后的 `mLeftViewType`

如果最终显示音乐，则 `mLeftViewType == 0`，`resumeMainMap()` 不会启动左侧地图 Surface。

如果最终显示导航，则 `mLeftViewType == 2`。在可见状态下从音乐切回导航时，宿主原始 `showLeftSubCardView(0)` 会动态分派到 `LeftInfoViewGroup.showMapCarView()` 并立即调用 `startLeftSDChangeService()`；在休眠恢复状态下，`resumeMainMap()` 也可以根据 `mLeftViewType == 2` 正常恢复左侧地图 Surface。

## 备选方案

### 方案 A：只在恢复时额外隐藏地图 View

在 `onHiddenChanged(false)` 或恢复后强制隐藏 `MapCardView`。

不采用原因：

- 这是症状修复，不是状态修复
- 地图 Surface 服务仍可能被启动
- SurfaceView 的层级和恢复时机不完全受普通 View visibility 控制

### 方案 B：Hook `SurfaceViewManager.resumeMainMap()`

在 `resumeMainMap()` 内部判断当前左侧实际卡片，再决定是否跳过左侧地图恢复。

不采用原因：

- 侵入点更底层
- 需要跨对象读取 `MainFragment` 当前 UI 状态
- 右侧地图恢复也经过同一方法，误伤面更大

### 方案 C：同步 `setLeftViewType` 到最终卡片结果

在模块改变左侧最终显示卡片时，同步设置 `SurfaceViewManager.mLeftViewType`。

采用原因：

- 修复状态源，而不是修复显示结果
- 影响范围只在左侧自动切换 Hook 内
- 与宿主自身语义一致：左侧非地图卡片应为 `mLeftViewType == 0`
- 容易用纯逻辑单元测试覆盖

## 测试设计

### 单元测试

在 `LeftSubCardAutoSwitchTest` 中覆盖：

- 原始卡片 `0` 且导航未激活时，最终显示 `1`
- 原始卡片 `0` 且导航未激活时，左侧 Surface 类型为 `0`
- 原始卡片 `0` 且导航激活时，最终显示 `0`
- 原始卡片 `0` 且导航激活时，左侧 Surface 类型为 `2`
- 原始卡片不是 `0` 时，左侧 Surface 类型始终为 `0`
- TBT 单独可见不应激活导航卡片

### 编译验证

需要运行：

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:compileDebugKotlin
```

涉及 Hook 和宿主 API，所以还需要用 JADX 确认：

- `SurfaceViewManager.getInstance()`
- `SurfaceViewManager.setLeftViewType(int)`
- `MainFragment.showLeftSubCardView(int)`
- `MainFragment.onResume()`
- `MainFragment.onHiddenChanged(boolean)`

### 车机验证

重点验证：

1. 左侧原始卡片为导航 `0`，无导航引导，显示音乐，锁车恢复后不出现地图重叠
2. 左侧原始卡片为导航 `0`，有导航引导，显示地图，锁车恢复后地图正常恢复
3. 左侧原始卡片为音乐 `1`，锁车恢复后仍是音乐，不出现地图
4. 左侧原始卡片为能耗、里程、车况等非地图卡片，锁车恢复后不出现地图
5. 导航状态从未激活变为激活时，左侧地图 Surface 能立即启动
6. 右侧卡片行为不因本次修改发生变化

## 风险

- 如果宿主未来改变左侧地图 Surface 类型常量，当前 `2` 需要重新通过 JADX 校准
- 如果宿主还有其他路径绕过 `showLeftSubCardView(int)` 和 `onResume()` 直接恢复 Surface，仍可能需要补充 Hook
- 如果导航状态 LiveData 在恢复瞬间短暂为旧值，可能出现一次短暂错误同步，但后续导航状态变化会 replay 原始卡片并修正
- 如果 `onResume()` 发生在原始左侧卡片尚未记录之前，应跳过同步，避免误清理宿主自己的 Surface 状态
- 右侧卡片原本存在顺序差异，本设计不处理该问题，避免扩大影响范围

## 结论

本问题的根因是模块改变了左侧卡片最终显示结果，但没有同步宿主地图 Surface 状态。设计上应把 `SurfaceViewManager.mLeftViewType` 视为与左侧最终显示卡片强相关的宿主状态，在 `MainFragmentHook` 中按最终显示结果同步。

最小修复是新增一个可测试的左侧 Surface 类型决策，并在 `showLeftSubCardView(int)` 和 `MainFragment.onResume()` 两个入口前调用 `SurfaceViewManager.setLeftViewType(...)`。前者覆盖卡片切换和导航状态变化，后者覆盖锁车休眠恢复但没有重新发卡片事件的场景。这样能让 `resumeMainMap()` 看到正确的 Surface 状态，从根源上避免音乐卡片和地图 Surface 重叠。
