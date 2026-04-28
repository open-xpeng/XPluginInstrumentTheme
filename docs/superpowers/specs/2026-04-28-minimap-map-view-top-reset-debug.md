# 小地图方向切换后坐标重置问题排查

## 背景

`MapFullFragment` 复用 MonteCarlo 的 `MiniMapViewWrapper` Surface 显示全屏地图。当前模块通过 Hook：

```text
com.xiaopeng.montecarlo.navcore.mapdisplay.MiniMapViewWrapper.getDefaultMapViewTop()
```

把宿主计算出的部分默认 top 偏移改成更适合全屏地图显示的值。

现象是：在地图 app 中切换小地图方向，例如“车头方向”或“北在上”后，mini map 坐标会被重置，导致全屏地图显示位置回到宿主默认位置。

## 已确认事实

JADX 核对到 `MiniMapViewWrapper.getDefaultMapViewTop()` 的核心逻辑：

```text
if (isLaneMode()) {
    top = mapHeight * 0.75f
} else {
    top = mapHeight * CarFeatureManager.INSTANCE.getMiniMapCarUpRatio()
}
```

这个方法不是固定返回值，而是依赖：

- 当前地图高度 `mapHeight`
- 当前是否为车道模式
- `CarFeatureManager.INSTANCE.getMiniMapCarUpRatio()` 返回的车型配置比例

`CarFeatureManager` 中 `MINIMAP_CAR_TOP_RATIO` 包含 `0.55f` 和 `0.7f` 等值，但这些值来自车型配置数组，不是 `getDefaultMapViewTop()` 里直接按“车头方向 / 北在上”分支计算。

JADX 核对到 `getDefaultMapViewTop()` 至少被两条关键路径使用：

```text
MapViewWrapper.restoreCarPosition()
BaseScene.saveOriginMapOffset()
```

其中 `restoreCarPosition()` 会执行：

```text
defaultTop = getDefaultMapViewTop()
mMapView.setMapLeftTop(defaultLeft, defaultTop)
```

因此，`getDefaultMapViewTop()` 的返回值会直接参与地图坐标恢复。

## 设置变化链路

JADX 核对到设置刷新存在以下路径：

```text
MiniMapAccountEventCallback.refreshSettingInfos()
    -> MiniMapServiceHelper.setMapMode(SettingWrapper.getCurrentMinimapMapMode(), true, false)
    -> MiniMapViewWrapper.setMapMode(...)
```

`MiniMapViewWrapper.setMapMode(...)` 的 Java 层实现会构造 `MapviewModeParam`，再调用底层：

```text
mMapView.setMapMode(mapviewModeParam, animation)
```

需要注意：JADX 中 `MiniMapViewWrapper.setMapMode(...)` Java 层本身没有直接调用 `restoreCarPosition()`。如果实车上切换方向后立刻发生坐标重置，直接重置点可能在底层 `MapView.setMapMode(...)` 内部，或在后续宿主恢复车位路径中触发。当前证据能证明“当前 Hook 的场景识别不稳定”，但不能单凭 JADX 完整证明方向切换到坐标重置的全部运行时因果链。

## 当前 Hook 的问题

当前 Hook 逻辑按原始返回值判断：

```kotlin
param.result = when (param.result) {
    363  -> 363
    396  -> 560
    else -> param.result
}
```

这个判断只覆盖了一个具体返回值 `396`。但宿主返回值会随地图高度、车道模式和车型配置比例变化。

按当前已知高度和比例推算：

```text
660 * 0.55 = 363   普通小地图在部分车型配置下的返回值
720 * 0.55 = 396   全屏地图在部分车型配置下的返回值
720 * 0.70 = 504   全屏地图在另一类车型配置下的返回值
720 * 0.75 = 540   全屏地图车道模式返回值
```

因此，`396` 只能代表“高度为 720 且比例为 0.55”的一个状态。只要车型配置比例或车道模式发生变化，当前 Hook 就不会继续返回 `560`。

## 根因判断

根因是当前 Hook 用 `getDefaultMapViewTop()` 的结果值 `396` 来识别全屏地图场景。

这个识别条件不稳定。全屏地图的更可靠特征不是宿主已经算出的 top，而是地图视口高度。`MapFullFragment` 当前通过 `SurfaceViewManager.SR_MAP_HEIGHT` 传入全屏地图高度，stub 中该值为 `720`；普通 SD 小地图高度为 `660`。

## 修复方向

第一层修复：把 `getDefaultMapViewTop()` Hook 从“按返回值判断”改为“按地图高度判断”。

推荐逻辑：

```text
如果 getMapHeight() == 720
则返回 560
否则保留宿主原始返回值
```

理由：

- 全屏地图高度是当前模块主动传给 MonteCarlo 的 Surface 高度。
- 这个判断不依赖 `CarFeatureManager` 的车型比例。
- 这个判断不依赖是否处于车道模式。
- 普通 SD 小地图高度为 `660`，不会被错误下移。

第二层加固：Hook `MiniMapViewWrapper.setMapMode(int, boolean, boolean)` 的 `after`。

如果实车验证发现仅修改 `getDefaultMapViewTop()` 后，切换方向仍会把底层坐标改回默认值，则在 `setMapMode(...)` 执行后，仅当 `getMapHeight() == 720` 时重新应用：

```text
setMapViewLeftTop(getDefaultMapViewLeft(), 560)
```

第二层加固用于覆盖底层 `MapView.setMapMode(...)` 内部可能重置坐标的情况。它比第一层更主动，建议作为同一轮修复的一部分实现，但必须严格限制在高度为 `720` 的 `MiniMapViewWrapper` 上。

## 方案边界

- 不修改 `MapFullFragment` 的 Surface 尺寸。
- 不修改普通主地图 `MapViewWrapper`。
- 不改变动态缩放偏移逻辑。
- 不新增设置项。
- 不按具体返回值 `396`、`504`、`540` 继续补丁式枚举。

## 验证清单

- 全屏地图初始化后仍保持当前期望偏移。
- 在地图 app 中切换“北在上”后，全屏地图坐标不再重置。
- 在地图 app 中切换“车头方向”后，全屏地图坐标不再重置。
- 导航进入车道模式或退出车道模式后，全屏地图坐标不被恢复到宿主默认位置。
- 普通 SD mini map 卡片不被错误下移。
- 中控主地图不受影响。
