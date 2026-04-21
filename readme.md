## 描述

小鹏P5车机的新仪表盘样式

## 预览

![白天预览效果图](./docs/photo_2026-03-16_01-10-51.jpg)

![白天预览效果图](./docs/photo_2026-03-16_01-09-29.jpg)

## 安装说明

1. **前置要求**
    - 已 Root 的 Android 设备
    - 已安装 Xposed Framework 或 LSPosed

2. **安装步骤**
    - 安装 APK 文件
    - 在 Xposed 管理器中启用 "方位指示器" 模块
    - 将作用域设置为目标应用（`com.xiaopeng.instrument` 或 `com.xiaopeng.montecarlo`）
    - 重启目标应用或整个车机

## 使用说明

安装并激活模块后，挂入D挡即可看到效果。

## 作者
- [X] 臭 · 搞技术的
- [X] 。。。(⬅️ 就是叫这个名 😅)

## 开发工具

强制杀死目标应用进程（用于开发调试）：

```shell
ps -elf | grep -w 'com.xiaopeng.instrument' | grep -v 'grep' | awk '{print $2}' | xargs kill -9
ps -elf | grep -w 'com.xiaopeng.montecarlo' | grep -v 'grep' | awk '{print $2}' | xargs kill -9
```

### Codex + JADX MCP

启动 JADX MCP Server：

```powershell
uv --directory "D:\Tools\jadx-mcp-server" run jadx_mcp_server.py --http
```

启动 Codex 时可临时启用：

```powershell
codex -c 'mcp_servers.jadx.enabled=true' --yolo
```

如果需要写入 Codex 配置，可使用以下片段：

```toml
[mcp_servers.jadx]
type = "steamable-http"
startupTimeout = 30000
toolTimeout = 300000
url = "http://127.0.0.1:8651/mcp"
enabled = false
```

## 版权

```text
Copyright 2026 Reccmost

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
