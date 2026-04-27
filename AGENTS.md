# 仓库指南

## 项目结构与模块组织

这是一个 Android Gradle 项目，包含 `app` 和 `stub` 两个模块。`app/` 是 Xposed 模块主体，包含 Kotlin Hook、Fragment、Widget、资源、assets、ProGuard 配置和测试。主源码位于 `app/src/main/java/com/xiaopeng/xposed/instrument/theme/`，单元测试位于 `app/src/test/java/`，资源位于 `app/src/main/res/`，`app/src/main/assets/xposed_init` 声明 Xposed 入口。`stub/` 存放宿主 XPeng 类的 compile-only Java 桩代码，应保持最小化，并与 JADX 反编译出的宿主 API 对齐。

## 构建、测试与开发命令

在仓库根目录使用内置 Gradle Wrapper：

- `.\gradlew.bat :app:compileDebugKotlin`：快速编译 Kotlin，验证语法和依赖。
- `.\gradlew.bat :app:testDebugUnitTest`：运行 JVM 单元测试。
- `.\gradlew.bat :app:assembleDebug`：构建 Debug APK。
- `.\gradlew.bat :app:assembleRelease`：构建开启混淆和资源压缩的 Release APK。
- `.\gradlew.bat clean`：清理生成产物。

## 编码风格与命名约定

遵循 `.editorconfig`：UTF-8、CRLF、4 空格缩进、不使用 Tab，Kotlin 文件保留末尾换行。主源码和 stub 文件应保留 Apache license header。命名风格尽量贴合现有代码，例如 Android 字段使用 `m` 前缀，Hook 对象使用 `mXCMethodOnViewCreated` 这类描述性名称，包名保持在 `com.xiaopeng.xposed.instrument.theme` 下。Kotlin 代码尽量少用 `let`、`apply`、`also`、`run` 等作用域函数，优先使用清晰的局部变量和提前返回，减少嵌套与隐式 `this`。代码应保持干练，不添加无实际价值的包装层、转发层或抽象；一个文件尽可能只承担一类职责。新增 Xposed Hook 优先继承 `XCMethodHookCatching`，反射和宿主 API 调用必须做好 null 或缺失 API 防护。stub 方法统一抛出 `RuntimeException("Stub!")`。

## 测试规范

单元测试使用 JUnit 4，放在 `app/src/test/java/`。测试类按被测对象命名，例如 `LeftSubCardAutoSwitchTest`。能抽成纯逻辑的行为应优先抽离，避免依赖 Android Runtime 或 Xposed 环境。提交前至少运行 `.\gradlew.bat :app:testDebugUnitTest`；涉及 Hook、stub 或宿主 API 时，还应运行 `.\gradlew.bat :app:compileDebugKotlin`，并用 JADX 核对真实宿主方法签名。

## 提交与 PR 规范

近期提交使用 emoji 前缀加中文描述，例如 `:bug: 修复 NaviSR 切换后的 SR Surface 恢复`、`:memo: 更新文档与忽略规则`。提交应聚焦单一行为变化，说明“为什么改”和“影响什么”，不要只描述改了哪些文件。PR 需要包含变更目的、影响的页面或 Hook、已运行的验证命令；涉及 UI、资源、Surface 或宿主交互时，应附截图、日志或复现场景。有关联 issue 或设计文档时一并链接。

## 安全与配置注意事项

不要提交本机路径、账号、密码或 `local.properties` 中的私有配置。谨慎处理 `app/app.jks` 和 Release 签名密码。由于模块运行在宿主进程中，避免无边界反射和大范围 Hook；所有宿主行为变更都应基于 JADX 验证，并保持失败时可安全跳过。
