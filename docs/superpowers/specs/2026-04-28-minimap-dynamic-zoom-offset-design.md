# 小地图动态缩放偏移设计

## 背景

`MapFullFragment` 把 MonteCarlo 小地图 Surface 渲染到仪表全屏地图区域。当前地图视野偏远，也就是实际 zoom level 偏小，路口和近距离道路细节不够清楚。

通过 JADX 核对，宿主小地图自身保留了动态缩放逻辑：

- `MiniMapServiceHelper.initMiniMap()` 初始化时调用 `setMapLevel(18.0f)`。
- `MiniMapCruiseDynamicLevelHelper` 在巡航时按车速动态更新缩放等级。
- `MiniMapGuideDynamicLevelHelper` 在导航时按剩余距离、道路类型和 2D/3D 模式动态更新缩放等级。
- 这些动态缩放最终都会通过 `MapViewWrapper.setMapLevel(float)` 应用到地图。

目标不是固定某一个缩放等级，而是保留宿主动态缩放计算，只在最终应用时把小地图 zoom level 整体提高 `+1.5f`。

## 目标

对 MonteCarlo 小地图引擎保留动态缩放行为，并在每次最终调用 `setMapLevel(float)` 时追加 `+1.5f` 缩放偏移。该小地图引擎覆盖 `MapFullFragment` 复用的地图 Surface。

## 非目标

- 不把地图锁定到固定 zoom level。
- 不改变路线、车道、SR 或实时路况渲染逻辑。
- 不影响中控主地图使用的普通 `MapViewWrapper`。
- 不修改现有 `MapFullFragment` 的 Surface 尺寸或布局。

## 设计

在现有 MonteCarlo 包 Hook 路径中增加一个 Hook，目标方法为：

```text
com.xiaopeng.montecarlo.navcore.mapdisplay.MapViewWrapper.setMapLevel(float)
```

在 `beforeHookedMethod` 中检查 `param.thisObject`。如果当前对象是：

```text
com.xiaopeng.montecarlo.navcore.mapdisplay.MiniMapViewWrapper
```

则读取原始 `Float` 参数，并替换为：

```text
min(originalLevel + 1.5f, 19.0f)
```

`19.0f` 上限与宿主小地图初始化保持一致。JADX 中 `MiniMapViewWrapper.creatMapSurfaceView()` 会调用 `setMaxZoomLevel(19.0f)`。

Hook 在修改参数前把宿主传入的原始缩放等级保存到当前 `MiniMapViewWrapper` 实例上。这样实际地图使用放大后的等级，但仍能在动态缩放重新启用时恢复宿主动态 helper 的原始基准值。

为了避免重复偏移，不 Hook `getMapLevelF()`。宿主 `MapViewWrapper.setMapLevel(float)` 内部会调用 `getMapLevelF()` 做幂等判断；如果全局修改 `getMapLevelF()` 返回值，会导致重复设置时无法跳过 `resetTickCount(1)` 和 `setZoomLevel(...)`。

新增 Hook 目标：

```text
com.xiaopeng.montecarlo.dynamiclevel.base.DynamicLevelHelper.enableDynamicLevel()
```

在 `afterHookedMethod` 中读取动态 helper 的 `mMapViewWrapper` 字段。只有该字段是 `MiniMapViewWrapper` 时，才读取前面保存的原始缩放等级，并写回动态 helper 的 `mPreLevel` 字段。这样巡航和导航仍然按宿主原始等级做动态过渡，最终显示时再统一加 `+1.5f`。

当前版本按 wrapper 类型限定生效范围，而不是按 Surface 来源限定。它会影响 `MiniMapViewWrapper` 实例，并排除普通 `MapViewWrapper` 主地图引擎。如果后续需要只影响 `MapFullFragment`、同时完全保留另一条仪表小地图路径不变，则需要增加额外运行时标记，用来识别当前 Surface 是否来自全屏地图。

## 组件

- `MiniMapViewWrapperHook`
  - 保留现有 `getDefaultMapViewTop()` Hook。
  - 新增 `MapViewWrapper.setMapLevel(float)` Hook。
  - 新增 `DynamicLevelHelper.enableDynamicLevel()` Hook，用于恢复动态缩放内部基准等级。
  - 使用 `XCMethodHookCatching` 做安全异常处理。

当前版本不新增 UI、设置项或持久化配置。

## 异常处理

Hook 必须失败时安全跳过：

- 如果找不到 `MapViewWrapper` 或 `MiniMapViewWrapper`，跳过缩放 Hook。
- `DynamicLevelHelper.enableDynamicLevel()` Hook 必须先于 `setMapLevel(float)` Hook 注册；如果动态基准修正不可用，不启用缩放偏移，避免回到重复偏移问题。
- 如果参数缺失或不是 `Float`，保持原值不变。
- 如果偏移后的等级超过 `19.0f`，限制为 `19.0f`。
- 如果动态 helper 没有可用的 `mMapViewWrapper`、`mPreLevel` 或保存的原始等级，保持宿主原行为不变。
- 如果发生反射或 Xposed 异常，通过现有 catching hook 路径记录日志，不阻断宿主启动。

## 验证

运行：

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:testDebugUnitTest
```

人工复核：

- 不新增 `getMapLevelF()` Hook，避免破坏宿主 `setMapLevel(float)` 的重复设置跳过逻辑。
- `setMapLevel(float)` Hook 只在 `thisObject` 是 `MiniMapViewWrapper` 时生效。
- `DynamicLevelHelper.enableDynamicLevel()` Hook 只修正 `mMapViewWrapper` 为 `MiniMapViewWrapper` 的动态 helper。

实车或测试环境手动验证：

- 全屏地图比修改前更接近，路口细节更清楚。
- 巡航时动态缩放仍会随车速变化。
- 导航时动态缩放仍会随引导和路口场景变化。
- 重复设置相同缩放等级时不会破坏宿主 `setMapLevel(float)` 内部的跳过逻辑。
- 中控主地图行为不变。
