# 小地图动态缩放 Hook 拆分设计

## 背景

`MiniMapViewWrapperHook` 最初同时承担了两类职责：

- 小地图坐标/中心点修复。
- 小地图动态缩放偏移。

坐标/中心点修复已经拆到：

```text
MiniMapViewCenterHook
```

当前 `MiniMapViewWrapperHook` 中仍然保留动态缩放偏移逻辑，包括：

- Hook `MapViewWrapper.setMapLevel(float)`。
- 仅当 `thisObject` 是 `MiniMapViewWrapper` 时，把宿主传入的 level 保存为 raw level。
- 实际传给宿主的 level 加 `+1.5f`，并限制到 `19.0f`。
- Hook `DynamicLevelHelper.enableDynamicLevel()`，在动态缩放重新启用后把 `mPreLevel` 修回 raw level，避免重复叠加偏移。

为了保持 Hook 类功能单一，需要把动态缩放偏移也拆到独立类。

## 目标

新增独立 Hook 类：

```text
MiniMapDynamicZoomHook
```

它只负责小地图动态缩放偏移。`MiniMapViewWrapperHook` 收敛为聚合入口，只负责调用：

```text
MiniMapViewCenterHook
MiniMapDynamicZoomHook
```

## 非目标

- 不改变动态缩放偏移行为。
- 不调整 `+1.5f` 偏移量。
- 不调整最大 zoom level `19.0f`。
- 不改 `MiniMapViewCenterHook` 的坐标/中心点逻辑。
- 不改 `XposedMan` 的 MonteCarlo 包入口。
- 不新增 helper、wrapper、配置项或测试桩。

## 设计

### 聚合入口

`MiniMapViewWrapperHook` 保留对象名和入口签名：

```kotlin
object MiniMapViewWrapperHook : (XC_LoadPackage.LoadPackageParam) -> Unit
```

`invoke` 中只做聚合：

```kotlin
MiniMapViewCenterHook(loadPackageParam = loadPackageParam)
MiniMapDynamicZoomHook(loadPackageParam = loadPackageParam)
```

这样 `XposedMan` 仍然只需要调用 `MiniMapViewWrapperHook`，不会知道小地图内部被拆成了哪些具体功能。

### 动态缩放 Hook

新增 `MiniMapDynamicZoomHook`：

```kotlin
object MiniMapDynamicZoomHook : (XC_LoadPackage.LoadPackageParam) -> Unit
```

该类迁移现有动态缩放相关成员：

```text
MAP_LEVEL_OFFSET = 1.5f
MAX_MAP_LEVEL = 19.0f
KEY_RAW_MAP_LEVEL = "xpit_raw_map_level"
mMiniMapViewWrapperClass
mXCMethodSetMapLevel
mXCMethodEnableDynamicLevel
```

注册顺序保持不变：

```text
findClass(MiniMapViewWrapper)
hook DynamicLevelHelper.enableDynamicLevel()
hook MapViewWrapper.setMapLevel(float)
```

保持 `enableDynamicLevel()` 先于 `setMapLevel(float)` 注册。这样如果动态基准修正 Hook 因宿主签名变化无法注册，`setMapLevel(float)` 偏移 Hook 也不会单独启用，避免重复叠加 zoom 偏移。

### 行为保持

`setMapLevel(float)` Hook 行为保持不变：

- `thisObject` 不是 `MiniMapViewWrapper` 时直接返回。
- 保存原始 level 到 Xposed additional instance field。
- 计算 `level + 1.5f`。
- 大于 `19.0f` 时限制为 `19.0f`。

`enableDynamicLevel()` Hook 行为保持不变：

- 读取 helper 的 `mMapViewWrapper`。
- 只处理 `mMapViewWrapper` 是 `MiniMapViewWrapper` 的动态 helper。
- 读取保存的 raw level。
- 写回 helper 的 `mPreLevel`。

## 错误处理

保持现有错误处理策略：

- 注册 Hook 使用直接 `try/catch`。
- 失败时 `XposedBridge.log(t)`。
- Hook callback 继续继承 `XCMethodHookCatching`。
- 反射字段或方法不存在时由 catching hook 记录并安全跳过。

## 验证

运行：

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:testDebugUnitTest
```

人工复核：

- `MiniMapViewWrapperHook` 只调用 `MiniMapViewCenterHook` 和 `MiniMapDynamicZoomHook`。
- `MiniMapDynamicZoomHook` 包含全部动态缩放偏移逻辑。
- `MiniMapViewCenterHook` 不被修改。
- `MAP_LEVEL_OFFSET`、`MAX_MAP_LEVEL`、`KEY_RAW_MAP_LEVEL` 值不变。
- 没有新增无关抽象。
