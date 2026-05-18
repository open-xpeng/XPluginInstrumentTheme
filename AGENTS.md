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

* 遵循 `.editorconfig`：UTF-8、CRLF、4 空格缩进、不使用 Tab，Kotlin 文件保留末尾换行。
* 主源码和 stub 文件应保留 Apache license header。
* 命名风格尽量贴合现有代码，例如 Android 字段使用 `m` 前缀，Hook 对象使用 `mXCMethodOnViewCreated` 这类描述性名称，包名保持在 `com.xiaopeng.xposed.instrument.theme` 下。
* Kotlin 代码尽量少用 `let`、`apply`、`also`、`run` 等作用域函数，优先使用清晰的局部变量和提前返回，减少嵌套与隐式 `this`。
* 代码应保持干练，不添加无实际价值的包装层、转发层或抽象；一个文件尽可能只承担一类职责。
* 代码优先保证可读性，尽量使用明确的局部变量、清晰的控制流和提前返回。
* 新增 Xposed Hook 优先继承 `XCMethodHookCatching`，反射和宿主 API 调用必须做好 null 或缺失 API 防护。stub 方法统一抛出 `RuntimeException("Stub!")`。

## 协作与代码约束

* 新增或修改文档时，默认使用中文撰写，除非已有文件约定，或任务明确要求使用其他语言。
* 不要自动提交代码。只有在人工明确要求提交时，才执行 `git commit`。
* 实现功能时优先沿用现有代码结构和命名方式，不额外引入包装、封装或自定义风格建议。

* 涉及反射字段访问或方法调用时，不要使用 `XposedHelpers.getObjectField`、`callMethod` 等通用函数。默认优先使用 Joor 完成对象访问与调用，减少零散字符串反射，提升可读性与可维护性。
* 使用 Joor 时，优先通过 `as` 函数将目标对象映射为接口后再访问成员，避免在调用点散落字段名和方法名字符串。
* 这类接口如果定义为类内私有成员，应补充 KDoc，说明其对应的能力、来源和版本假设，便于后续功能回溯（每个接口都要进行使用KDoc）。确有例外时，应在代码或文档中说明原因。
* 注意使用Jadx的时候，通常以f开头的变量是 JADX 伪名，不能拿它当运行时真字段名。是错误的，如：f8968c，应该从jadx中获取正确的变量名。

* 涉及 Hook 相关功能时，统一通过 `XposedHelpersWrapper` 进行包裹调用，不要在业务代码中直接散落使用底层 `XposedHelpers` 能力。
* 如果 `XposedHelpersWrapper` 暂无对应函数，应先补充相应的包裹函数，再在调用方接入。
* 对于仅服务单一版本 Hook 的辅助规则、常量、数据类、binder 或 widget 上下文，不要拆成新的顶层定义或额外 `Core` 包装文件。默认收敛在对应版本 `object/class` 内部；如确实需要抽离，必须有明确复用场景，并优先使用类内嵌套类型，而不是文件级顶层声明。
* 对于仅被某个 Hook 回调独占使用的流程或修复逻辑，优先内聚到该回调内部或其就近嵌套作用域，避免在版本 Hook `object/class` 外层继续扩散一次性方法，减少作用域污染，并让阅读路径顺着 hook 入口自然展开。

* 测试应按风险和收益补充，不为形式增加测试。只有变更涉及关键逻辑、易回归行为，或已有测试能明确承接时再新增测试，避免测试代码过多反而增加维护负担。
* 涉及 JADX 反编译结果的方法、字段或接口时，应使用 KDoc 说明其来源、用途和版本假设，便于后续维护。

* 不要使用worktree，直接在当前仓库编写代码。

## 测试规范

单元测试使用 JUnit。测试代码放在 `app/src/test/java`，包名应与被测源码保持一致。测试类按被测类或行为命名。修改解析逻辑、Wrapper、设置入口逻辑或版本选择逻辑时，优先补充聚焦测试。

* 测试通过后再运行 `.\gradlew.bat testDebugUnitTest` 做最终确认。
* 在编写任务时允许编写测试，在任务完成后，请移除测试并保证类中没有顶部class、interface、const、enmu的定义。

## 提交与 Pull Request 规范

现有提交使用 emoji 前缀加简短中文说明，例如 `:bug: 修复XXXX`、`:sparkles: 添加XXX`。每个提交只包含一个逻辑变更。

## 安全与配置提示

不要提交本机专属的 `local.properties` 变更或真实签名密钥。

