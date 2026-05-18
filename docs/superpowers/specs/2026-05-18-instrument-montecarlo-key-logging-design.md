# Instrument / Montecarlo 关键链路日志补点设计

## 背景

当前项目已经完成 SLF4J 与 `logback-android` 接入，日志基础设施可用，但 `instrument` 与 `montecarlo` 两个宿主下的大部分业务链路仍缺少足够的关键日志。

这会导致后续排查功能问题时存在几个明显痛点：

- 只能看到少量异常日志，缺少正常链路上的关键状态
- 无法从日志直接复盘 Hook 是否注册成功、是否进入目标回调、是否命中关键分支
- 无法明确看到“宿主原始值 -> 模块决策值 -> 实际应用结果”的演变过程
- 地图、卡片、Fragment 切换、Surface 同步、缩放修正等行为缺少可串联的事件轨迹

本次工作目标不是重构日志体系，而是在现有 SLF4J 基础上，为 `instrument` 与 `montecarlo` 的运行时代码增加足够多的关键摘要日志，让后续问题排查能够靠日志复盘具体状态和决策过程。

## 目标

- 仅为 `instrument` 与 `montecarlo` 的运行时代码增加关键链路日志
- 保持现有 SLF4J 用法，不新增日志 helper、包装器或转发层
- 保留日志真实调用点行号，便于直接从日志定位源码
- 让日志能够覆盖模块入口、Hook 决策、Fragment/Widget 协作、反射取值和关键状态切换
- 让日志足以支持后续功能排查，而不是只覆盖异常路径

## 非目标

- 不修改 `commons/` 下的通用日志基础设施
- 不修改 `stub/` 下的 compile-only 桩代码
- 不引入新的日志框架、日志 DSL 或结构化埋点系统
- 不为了统一格式而新增只服务本次任务的日志工具类
- 不要求每个方法、每次 observer 回调、每次纯渲染更新都记录日志

## 范围

### 包范围

本次仅修改：

- `app/src/main/java/com/xiaopeng/xposed/instrument/`
- `app/src/main/java/com/xiaopeng/xposed/montecarlo/`

明确排除：

- `app/src/main/java/com/xiaopeng/xposed/commons/`
- `stub/src/main/java/`

### 文件范围分层

#### 高价值必加

这些文件位于模块入口、核心 Hook 与主链路页面上，必须完整增加关键入口和状态变化日志：

- `instrument/XposedMain.kt`
- `montecarlo/XposedMain.kt`
- `instrument/hooks/MainActivityHook.kt`
- `instrument/hooks/MainFragmentHook.kt`
- `instrument/fragments/MapFullFragment.kt`
- `montecarlo/hooks/MiniMapDynamicZoomHook.kt`
- `montecarlo/hooks/MiniMapViewCenterHook.kt`

#### 中价值选择性加

这些文件承接关键状态、反射结果或关键控件行为，增加初始化与决策日志，但不要求把所有普通调用铺满：

- `instrument/hooks/MainActivityExtension.kt`
- `instrument/widgets/MapBaseInfoView.kt`
- `instrument/widgets/MapLeftInfoViewGroup.kt`
- `instrument/widgets/MapRightInfoViewGroup.kt`
- `instrument/utils/LayoutInflaterXposed.kt`
- `instrument/utils/LeftSubCardAutoSwitch.kt`
- `instrument/utils/ReflectPrivateFieldDelegateStringOrNull.kt`
- `instrument/utils/ReflectPrivateFieldDelegateLong.kt`
- `instrument/constants/ConstantSurfaceViewManager.kt`

#### 默认不加

以下内容默认不单独补日志：

- 纯资源声明
- 无运行逻辑的简单常量
- 只有单层简单转发、且没有关键分支、反射、状态决策的代码

## 设计原则

1. 日志优先服务排查，不为了“控量”牺牲关键状态可见性
2. 仍然避免纯噪音日志，不记录没有分析价值的高频细枝末节
3. 关键日志要能串成链路，能从入口看到条件，再看到决策，再看到应用结果
4. 直接在业务调用点使用 SLF4J，不新增包裹，保留准确源码行号
5. 只在有价值的位置使用 `isInfoEnabled` / `isDebugEnabled`，避免机械铺满

## 方案选择

### 方案 A：直接在业务调用点补日志

做法：

- 在 `instrument` 与 `montecarlo` 的现有文件中直接增加 `mLogger.info(...)` / `mLogger.debug(...)` / `mLogger.error(...)`
- 统一使用 `event=... key=value` 风格
- 对带参数计算、反射读取、高频路径的日志加级别判断

优点：

- 改动最小，完全贴合现有代码组织
- 日志语义离业务最近，最容易理解与维护
- 行号准确落在真实业务代码
- 不引入额外抽象层，符合当前仓库风格

缺点：

- 需要人工保持消息格式一致
- 不同文件间的命名纪律需要通过约定维持

### 方案 B：新增轻量日志辅助方法

做法：

- 为若干 Hook 或 Fragment 抽取本地日志方法，统一消息格式

优点：

- 调用点更短
- 格式更容易统一

缺点：

- 会引入额外封装层
- 容易影响日志行号定位价值
- 对当前仓库的“少包装、少转发”约束不友好

### 方案 C：扩展通用基类做自动入口日志

做法：

- 在通用 Hook Wrapper 中自动补大量入口日志

优点：

- 覆盖范围快

缺点：

- 拿不到业务上下文，日志会很泛
- 噪音容易失控
- 与本次“关键点摘要”目标不匹配

## 结论

采用方案 A：直接在业务调用点补日志。

原因如下：

- 用户明确要求不要额外包裹，以免丢失 SLF4J 行号信息
- 当前仓库强调沿用现有代码结构，不额外引入包装或转发层
- 本次重点是让后续排查能读懂具体状态与决策，业务调用点是最合适的落点

## 日志格式约定

### 消息风格

统一采用：

```text
event=... key1=value1 key2=value2 ...
```

要求：

- 第一段固定是 `event=...`
- 后续字段尽量使用稳定的 `key=value`
- 参数全部使用 SLF4J `{}` 占位，不做字符串拼接
- 避免直接打印超大对象全文

### 通用字段

不同文件按需携带以下字段：

- `event`
- `process`
- `targetClass`
- `targetMethod`
- `fragment`
- `rawValue`
- `effectiveValue`
- `previousValue`
- `newValue`
- `reason`
- `result`
- `hidden`
- `navActive`
- `turnGuidanceVisible`
- `tbtVisible`
- `surfaceType`
- `width`
- `height`

不要求每条日志都带全量字段，但同一类事件要尽量保持字段稳定。

### 事件命名

模块入口层：

- `event=module_load_package`
- `event=module_init_zygote`
- `event=hook_registered`
- `event=hook_register_completed`

Hook 决策层：

- `event=hook_enter`
- `event=hook_skipped`
- `event=hook_state_initialized`
- `event=hook_state_changed`
- `event=hook_value_resolved`
- `event=hook_value_applied`

页面与控件层：

- `event=fragment_view_created`
- `event=fragment_hidden_changed`
- `event=observer_triggered`
- `event=card_render_resolved`
- `event=fragment_switch_started`
- `event=fragment_switch_completed`
- `event=surface_change_service_start`
- `event=surface_change_service_skipped`
- `event=surface_change_service_completed`

反射与工具层：

- `event=reflect_field_read`
- `event=reflect_value_initialized`
- `event=layout_inflate_intercepted`
- `event=layout_inflate_delegated`
- `event=auto_switch_resolved`

## SLF4J 使用规则

### 直接记录

以下情况可直接调用：

- 固定文案、无额外参数计算的 `info`
- 简单一两个原始参数、不会触发复杂对象访问的日志
- 异常日志 `mLogger.error("...", throwable)`

### 先做级别判断

以下情况先做 `isInfoEnabled` / `isDebugEnabled` 判断：

- 需要反射读取字段或方法结果后才打印
- 需要读取多个状态并组织一条摘要日志
- 位于高频路径，例如 observer、频繁 Hook 回调、控件更新
- 需要输出 `raw -> effective -> applied` 这类多参数决策链

约束如下：

- 以 `info` 为主，因此优先使用 `if (mLogger.isInfoEnabled)`
- 个别高频且更细的补充日志可使用 `debug`，并搭配 `if (mLogger.isDebugEnabled)`
- 不为了形式把所有日志都包进级别判断

## 分层覆盖策略

### 模块入口层

目标：

- 明确模块是否命中目标宿主
- 明确 `zygote` 初始化是否执行
- 明确关键 Hook 是否全部注册完成

适用文件：

- `instrument/XposedMain.kt`
- `montecarlo/XposedMain.kt`

建议事件：

- 进入 `handleLoadPackage` 时记录宿主包名和进程名
- 命中目标宿主分支时记录模块开始初始化
- 每类关键 Hook 注册后记录 `hook_registered`
- 全部注册完成后记录 `hook_register_completed`
- `initZygote` 完成后记录模块路径或关键初始化结果

### Hook 决策层

目标：

- 明确 Hook 回调是否进入
- 明确关键分支为何命中或跳过
- 明确原始值、决策值、应用值

适用文件：

- `instrument/hooks/MainActivityHook.kt`
- `instrument/hooks/MainFragmentHook.kt`
- `montecarlo/hooks/MiniMapDynamicZoomHook.kt`
- `montecarlo/hooks/MiniMapViewCenterHook.kt`

建议补点：

#### `MainActivityHook.kt`

- `showFragmentByClass` 进入时记录目标 Fragment、当前挡位、当前 Fragment 名
- 因目标 Fragment 不匹配、挡位不匹配而跳过时记录 `hook_skipped`
- 开始显示 `MapFullFragment` 时记录 `fragment_switch_started`
- 新建还是复用 `MapFullFragment` 时记录结果
- 隐藏 `MapFullFragment`、恢复宿主 `MainFragment` 时记录 `fragment_switch_completed`
- `SR` Surface 接管、恢复时记录 surface 相关结果
- `gearLiveData` 触发时记录挡位变化与触发动作

#### `MainFragmentHook.kt`

- Hook 注册完成
- `onViewCreated` 初始化导航状态后记录 `hook_state_initialized`
- `onResume` 可见时记录 surface 同步行为
- `onHiddenChanged` 从隐藏恢复时记录卡片回放与主地图恢复
- `showLeftSubCardView` 记录 `rawCardIndex`、`navActive`、`effectiveCardIndex`
- 因 `fragment.isHidden` 跳过真实显示时记录 `hook_skipped`
- 导航激活状态发生变化时记录 `hook_state_changed`
- `syncLeftMapSurfaceType` 时记录最终 `surfaceType`
- 导航信号 observer 触发重算时记录 `observer_triggered`

#### `MiniMapDynamicZoomHook.kt`

- 目标类查找完成后记录是否成功
- `setMapLevel` 进入时记录原始 level、是否命中目标实例
- `enableDynamicLevel` 进入时记录动态缩放开关与实际修正结果
- 因目标实例不匹配、字段缺失而提前返回时记录 `hook_skipped`
- 最终写回宿主的 level 或动态开关结果记录 `hook_value_applied`

#### `MiniMapViewCenterHook.kt`

- 关键 Hook 注册完成
- 中心点、顶部偏移、地图模式相关回调进入时记录 `hook_enter`
- 原始值修正为最终值时记录 `hook_value_resolved`
- 因模式不匹配或实例不匹配而跳过时记录 `hook_skipped`

### 页面与控件协作层

目标：

- 让 Fragment、Observer、卡片显示、Surface 通知形成完整链路

适用文件：

- `instrument/fragments/MapFullFragment.kt`
- `instrument/widgets/MapBaseInfoView.kt`
- `instrument/widgets/MapLeftInfoViewGroup.kt`
- `instrument/widgets/MapRightInfoViewGroup.kt`

建议补点：

#### `MapFullFragment.kt`

- `onCreateView` / `onViewCreated` 完成初始化时记录 `fragment_view_created`
- `initLeftSubCardAutoSwitch` 记录导航初始状态
- `renderLeftSubCard` 记录 `rawCardIndex -> effectiveCardIndex`
- `onHiddenChanged` 恢复显示时记录卡片回放与 surface 重发
- `startChangeService` 前记录宽高、目标 action、是否测试平台
- 测试平台跳过记录 `surface_change_service_skipped`
- 启动成功记录 `surface_change_service_completed`
- 启动失败继续保留 `error`
- 关键 observer 触发时记录触发源与最终动作

#### `MapBaseInfoView.kt`

- 关键列表数据更新、选中项变更、滚动目标变更时记录摘要
- 避免为每次纯 UI 赋值输出低价值明细

#### `MapLeftInfoViewGroup.kt` / `MapRightInfoViewGroup.kt`

- 关键卡片显示决策、地图卡片切换、列表显隐变化记录摘要
- 如果会触发地图或 Surface 相关动作，要记录结果

### 反射与工具支撑层

目标：

- 让一次性反射初始化和关键纯逻辑决策可见

适用文件：

- `instrument/utils/LayoutInflaterXposed.kt`
- `instrument/utils/LeftSubCardAutoSwitch.kt`
- `instrument/utils/ReflectPrivateFieldDelegateStringOrNull.kt`
- `instrument/utils/ReflectPrivateFieldDelegateLong.kt`
- `instrument/constants/ConstantSurfaceViewManager.kt`

建议补点：

#### `LayoutInflaterXposed.kt`

- 命中模块自定义 View 时记录 `layout_inflate_intercepted`
- 交由系统/原逻辑处理时记录 `layout_inflate_delegated`

#### `LeftSubCardAutoSwitch.kt`

- `resolve(...)` 记录关键输入与输出
- `isNavigationActive(...)` 记录输入状态与结果
- `resolveLeftMapSurfaceType(...)` 记录 surface 类型决策结果

这类方法可能被多次调用，应优先使用 `isDebugEnabled` 或仅在关键调用方记录 `info`，避免高频重复。

#### `ReflectPrivateFieldDelegateStringOrNull.kt` / `ReflectPrivateFieldDelegateLong.kt`

- 首次读取关键宿主私有字段时记录 `reflect_field_read`
- 字段缺失、类型不匹配或读取失败时记录 `error`

#### `ConstantSurfaceViewManager.kt`

- 首次完成关键常量反射解析时记录 `reflect_value_initialized`
- 包括 `PACKAGE_NAME`、`CLASS_NAME`、`ACTION_MAP_SURFACE_CHANGED`、地图尺寸等关键值

## 日志量控制

本次策略是“日志不怕多，但只要多在关键点上”。

因此采用以下控制方式：

- 优先记录主链路入口、条件判断、状态变化、最终应用结果
- 不为没有分析价值的纯重复渲染输出日志
- 对高频纯逻辑方法，优先把 `info` 留在调用方，把内部细节下沉到 `debug`
- 对明显会触发反射、字段读取、复杂参数准备的日志加级别判断

## 实施顺序

建议按以下顺序实施：

1. `instrument/XposedMain.kt`
2. `montecarlo/XposedMain.kt`
3. `instrument/hooks/MainActivityHook.kt`
4. `instrument/hooks/MainFragmentHook.kt`
5. `instrument/fragments/MapFullFragment.kt`
6. `montecarlo/hooks/MiniMapDynamicZoomHook.kt`
7. `montecarlo/hooks/MiniMapViewCenterHook.kt`
8. `instrument/widgets/` 下关键控件
9. `instrument/utils/` 与 `instrument/constants/`

原因：

- 先补入口和主链路，最先获得可用排查收益
- 再补控件与工具层，形成更细的状态闭环

## 风险与注意事项

- 日志过多的主要风险不在文件大小，而在可读性失控，因此事件命名和字段稳定性必须严格执行
- 高调用频率逻辑若直接大量 `info`，可能放大日志量；这类位置要谨慎分配 `info/debug`
- 不能为了日志补点去改动原有行为顺序，尤其是 Hook 前后置时机、Fragment 切换顺序、Surface 相关调用顺序
- 不允许通过新增 helper 统一格式，否则会损失行号与当前仓库约束

## 验证策略

实现完成后至少验证以下内容：

- 模块进入目标宿主后，能看到模块入口日志和 Hook 注册完成日志
- `instrument` 主链路中，能从日志串起挡位变化、Fragment 切换、左侧卡片决策、Surface 通知
- `montecarlo` 主链路中，能从日志看到缩放或视图中心修正的原始值与应用值
- 关键高频路径没有出现明显的无意义刷屏
- 异常路径仍能输出完整 `Throwable`

## 实现边界结论

本次实现采用“业务调用点直写 SLF4J”的方式，在 `instrument` 与 `montecarlo` 的关键运行时代码中增加足以支持排查的摘要日志，重点覆盖入口、条件、状态变化与应用结果，不改 `commons`，不改 `stub`，不新增日志包装层。
