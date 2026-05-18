# Koin 与 SLF4J 双宿主接入设计

## 背景

当前模块已经在 `gradle/libs.versions.toml` 和 `app/build.gradle.kts` 中加入了 Koin 与 SLF4J 相关依赖，但仓库里还没有真正的启动流程、module 组织和日志初始化代码。

现状特点有两个：

- 模块同时 Hook `com.xiaopeng.instrument` 和 `com.xiaopeng.montecarlo`
- 现有 Hook 主要以 `object` 形式组织，例如 `MainActivityHook`、`MainFragmentHook`、`MiniMapViewWrapperHook`

参考项目 `D:\AndroidStudioProjects\So\Hyper125833` 已经验证过一套接入形态：

- 在宿主 `Application#onCreate` Hook 中启动 Koin
- 通过自定义 `LoggerKoin` 把 Koin 日志桥接到 SLF4J
- 通过 `logback-android` 同时输出 Logcat 和文件滚动日志

本次工作需要把这套形态引入当前项目，但不能无差别照搬联系人项目的业务结构，也不能顺手把当前 Hook 体系重构掉。

## 目标

- 为 `com.xiaopeng.instrument` 和 `com.xiaopeng.montecarlo` 两个宿主补齐 Koin 启动流程
- 为两个宿主补齐 SLF4J / `logback-android` 初始化流程
- 初始化时机统一放在宿主 `Application#onCreate` 的 Hook 内
- Koin module 采用“共享基础 module + 宿主专属 module”的结构
- 保持现有 Hook 入口和 `object` 组织方式，不强制改成容器驱动
- 允许后续代码逐步使用 `LoggerFactory` 和 Koin 共享依赖

## 非目标

- 不在本次把所有 Hook 改造成可注入类
- 不重构 `XposedMan` 的整体分发模型
- 不引入与当前项目无关的联系人项目业务模块
- 不在本次大面积替换现有日志调用
- 不为了接 Koin 而额外增加无价值的包装层或转发层

## 现状分析

### 现有入口

当前真正稳定的模块入口是：

- `XposedMan.handleLoadPackage(...)`
- `MainActivity.onCreate(...)` 的业务 Hook

`XposedMan` 负责按包名分发：

- `com.xiaopeng.instrument`：注册 `MainActivityHook`、`MainFragmentHook`
- `com.xiaopeng.montecarlo`：注册 `MiniMapViewWrapperHook`

这说明新增 Koin / SLF4J 接入时，最合理的改动点仍然是 `XposedMan`，由它继续负责按包名注册宿主初始化 Hook。

### 依赖现状

当前工作区未提交改动里已经包含：

- `koin-bom`
- `koin-core`
- `koin-android`
- `slf4j-api`
- `logback-android`

同时还顺手加入了 RxJava 依赖。由于这部分改动已经存在，本设计不讨论依赖是否引入，只讨论如何真正接通初始化与使用边界。

### 参考项目可复用模式

参考项目中可直接复用的设计思想包括：

- 用宿主 `Application` 实例作为 Koin 的 `androidContext`
- 提供单独的 Koin logger 适配器
- 用共享日志初始化器配置 Logcat 与滚动文件日志
- 把 `XC_LoadPackage.LoadPackageParam`、主线程 `Handler` 等基础能力放入 Koin

当前项目与参考项目的差异在于：

- 当前项目有两个宿主，不是单宿主
- 当前项目现有 Hook 结构更轻，不适合为接 DI 一次性重构

## 方案选择

### 方案 A：双宿主分启动器，共享基础设施，Hook 结构不动

做法：

- `XposedMan` 继续按包名分发
- 两个宿主各自新增 `Application#onCreate` Hook 回调
- 回调内部统一执行 SLF4J 初始化与 Koin 启动
- Koin module 拆成 `common + instrument + montecarlo`
- 现有 Hook 保持 `object` 结构不动

优点：

- 与参考项目启动方式一致
- 宿主边界清晰，后续扩展时不容易把两个宿主的逻辑混在一起
- 不会为了接入基础设施而打散现在已经稳定的 Hook 代码

缺点：

- 需要分别处理两个宿主的 `Application` 启动入口
- 必须显式处理重复初始化和宿主差异

### 方案 B：统一启动器，按包名分流

做法：

- 只保留一个总入口初始化器
- 由总入口内部根据 `packageName` 决定 module 和日志配置

优点：

- 文件数量更少

缺点：

- 所有宿主差异都堆进一个类里
- 随着 Hook 和基础设施增多，分支逻辑会持续膨胀

### 方案 C：容器驱动 Hook 注册

做法：

- Koin 启动后由容器统一提供 Hook 注册器、日志器和业务对象
- 现有 Hook 逐步改造成注入类

优点：

- 架构上最统一

缺点：

- 明显超出本次范围
- 需要重构现有 Hook 组织方式，风险偏大

## 结论

采用方案 A：双宿主分启动器，共享基础设施，保持现有 Hook 结构不动。

该方案最符合本次明确约束：

- 启动时机固定在 `Application#onCreate`
- 两个宿主都要接入
- module 采用共享基础层加宿主专属层
- 现有 Hook 只做最小必要调整

## 设计

### 架构分层

本次新增的代码分成三层：

1. `XposedMan` 宿主分发层
2. 宿主初始化层
3. 基础设施层

职责如下：

- `XposedMan`
  - 继续判断 `packageName`
  - 继续注册现有业务 Hook
  - 额外注册宿主 `Application#onCreate` 初始化 Hook

- 宿主初始化层
  - 拿到真实 `Application`
  - 执行初始化幂等判断
  - 初始化 SLF4J
  - 启动 Koin

- 基础设施层
  - 提供 Koin modules
  - 提供 Koin logger
  - 提供 Logback 初始化器
  - 提供宿主标识与共享对象

这样拆分后：

- 启动与业务 Hook 解耦
- 共享能力集中管理
- 两个宿主可以共用基础设施，但不会共用错误的宿主状态

### 启动流程

#### `com.xiaopeng.instrument`

1. `XposedMan.handleLoadPackage(...)` 进入 `com.xiaopeng.instrument` 分支
2. 继续注册 `MainActivityHook`、`MainFragmentHook`
3. 额外注册 `Application#onCreate` Hook
4. 宿主 `Application.onCreate()` 执行到模块回调时，拿到 `Application` 实例
5. 以该 `Application` 执行 SLF4J 初始化
6. 以该 `Application` 启动 Koin，并装载 `common + instrument` modules

#### `com.xiaopeng.montecarlo`

1. `XposedMan.handleLoadPackage(...)` 进入 `com.xiaopeng.montecarlo` 分支
2. 继续注册 `MiniMapViewWrapperHook`
3. 额外注册 `Application#onCreate` Hook
4. 宿主 `Application.onCreate()` 执行到模块回调时，拿到 `Application` 实例
5. 以该 `Application` 执行 SLF4J 初始化
6. 以该 `Application` 启动 Koin，并装载 `common + montecarlo` modules

### 幂等策略

运行在宿主进程里的初始化必须可重复进入但只执行一次。

因此需要一个显式的启动状态守卫，至少按以下维度判定：

- 宿主包名
- 当前进程

约束如下：

- 同一宿主进程中不能重复 `startKoin`
- 同一宿主进程中不能重复 reset `LoggerContext`
- 同一 Hook 回调多次触发时，只允许第一次真正初始化
- 一个宿主初始化失败时，不能污染另一个宿主的初始化状态

实现上允许用共享注册表或启动管理器来记录状态，但对外语义必须是“按宿主独立初始化”。

### Koin 结构

Koin 只承担基础设施容器角色，不接管现有 Hook 入口。

#### `common` module

建议放入：

- `Application`
- `XC_LoadPackage.LoadPackageParam`
- 主线程 `Handler`
- 可选的 `Gson`
- 宿主包名、进程名等宿主上下文信息

这些对象有两个特点：

- 两个宿主都可能需要
- 放到容器里可以减少后续重复手搓单例

#### `instrument` module

先只放 `com.xiaopeng.instrument` 专属依赖。

本次不强求立即填满，只要求预留清晰边界。后续若新增与仪表盘宿主绑定的协调器、配置对象或服务，直接放在这一层。

#### `montecarlo` module

先只放 `com.xiaopeng.montecarlo` 专属依赖。

本次也不强求塞业务对象，重点是把 module 边界先搭出来，避免以后把地图宿主依赖误塞进公共层。

### SLF4J 与 Logback 结构

日志层基本对齐参考项目，但收敛到当前项目真正需要的部分。

保留两类输出：

- Logcat
- 文件滚动日志

设计要求：

- 日志初始化器为共享组件
- 不同宿主的日志目录要隔离
- 日志标识中要能区分宿主包名或进程
- 文件日志创建失败时，不影响 Logcat 输出

文件日志策略保持参考项目的思路：

- 使用 `RollingFileAppender`
- 使用按时间与大小滚动的策略
- 将日志写入宿主可访问的缓存目录下的模块专属子目录

这样做的目的不是追求复杂日志体系，而是保留车机问题定位时最有价值的两个能力：

- 开发联调时看 Logcat
- 现场追查时保留文件日志

### Koin Logger

需要新增一个 Koin logger 适配器，把 Koin 内部日志桥接到 SLF4J。

设计要求：

- 统一从 `LoggerFactory` 取 logger
- 按 Koin 的 `Level` 映射到 SLF4J 的 `debug` / `info` / `warn` / `error`
- 不在适配器里引入业务逻辑

这样可以保证：

- Koin 自己的启动与依赖解析日志进入同一套日志体系
- 后续排查容器初始化问题时，不用同时看两套输出

### 现有 Hook 的接入方式

现有 Hook 不改成 Koin 组件，这是本次的硬边界。

允许的接入方式是：

- 新增共享能力时，由 Hook 主动从 Koin 获取
- 新增日志时，Hook 直接用 `LoggerFactory.getLogger(...)`
- 少量示范性地把一两个类接到新日志体系上

不做的事情：

- 不新增“为注入而注入”的转发层
- 不把 `object` Hook 强改成 `class + inject()`
- 不把 `XposedMan` 改成完整的容器驱动分发器

### 失败策略

这是运行在宿主进程中的代码，失败策略必须偏保守。

要求如下：

- 任一宿主的初始化异常都要被捕获，失败时允许跳过增强能力
- 顶层兜底日志仍可保留 `XposedBridge.log(...)`
- 文件日志失败时至少保留 Logcat
- `montecarlo` 如果实际宿主 `Application` Hook 条件与预期不符，允许只保留现有业务 Hook，不阻断模块
- 现有功能不能因为 Koin 未启动就整体失效

核心原则是：基础设施接入是增强，不是现有功能的硬依赖。

## 数据与调用边界

### 初始化阶段输入

初始化器需要接收的核心输入包括：

- `Application`
- `XC_LoadPackage.LoadPackageParam`
- 宿主包名

### 初始化阶段输出

初始化成功后，对系统产生的结果包括：

- 当前宿主进程内的日志系统可用
- 当前宿主进程内的 Koin 容器可用
- 宿主专属与公共依赖都已注册

### 运行阶段使用

后续普通 Hook / 工具类对基础设施的使用边界应保持简单：

- 要日志时直接 `LoggerFactory.getLogger(...)`
- 要共享依赖时通过 Koin 获取
- 要判断宿主信息时使用容器内统一提供的宿主上下文

## 风险

- `com.xiaopeng.montecarlo` 的真实 `Application` 启动链如果与预期不一致，可能需要额外用 JADX 校准挂点
- Koin 是全局上下文模型，若幂等守卫设计不清晰，容易出现重复启动或跨宿主污染
- `logback-android` 的文件目录如果依赖外部缓存路径，部分宿主环境下可能需要额外判空保护
- 如果过早把太多业务对象塞进 module，容易把本次基础设施接入变成隐性重构
- 当前工作区已经存在依赖层未提交改动，后续实现时需要和这些改动协同，而不是回退重做

## 验证计划

本次设计对应的最小验证闭环是：

1. 代码层确认 `instrument` 与 `montecarlo` 都注册了各自的 `Application` 初始化 Hook
2. 代码层确认 Koin 启动按宿主装载 `common + host-specific` modules
3. 代码层确认 SLF4J 初始化同时包含 Logcat 与文件滚动日志
4. 代码层确认存在宿主级别的幂等保护
5. 编译验证通过：

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:testDebugUnitTest
```

6. 如果实现阶段涉及宿主类名或方法签名，需再次用 JADX 校验实际宿主 API

## 实施范围

本次实现范围控制在基础设施接入本身：

- 补齐双宿主 `Application#onCreate` 初始化 Hook
- 补齐共享 Koin 启动器与 modules
- 补齐共享 SLF4J / Logback 初始化器
- 补齐 Koin logger
- 选择少量现有类做示范性接入

不包含：

- 大面积日志调用替换
- Hook 结构性重构
- 业务逻辑迁移进容器

## 结论

本次接入应把 Koin 和 SLF4J 视为“双宿主共享基础设施”，而不是“重写现有 Hook 架构”的起点。

最合适的落地方式是：

- 由 `XposedMan` 按包名继续分发
- 在两个宿主各自的 `Application#onCreate` Hook 中完成初始化
- 用 `common + instrument + montecarlo` 组织容器
- 用共享日志初始化器统一配置 Logcat 与文件滚动日志
- 保持现有 Hook 结构不动，只提供后续渐进接入的能力边界

这样既能对齐参考项目已经验证过的接入方式，也不会把当前仓库拖进一次没有必要的重构。
