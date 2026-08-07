<div align="center">
  <img src="fastlane/metadata/android/zh-CN/images/icon/01.png" width="96" alt="万宝盒图标" />

  # 万宝盒 OneBox

  [English](README.md) | **简体中文**

  免费的一站式 Android 工具箱：图片与文档工具、AI 助手 / Agent、效率与生活工具，一个入口完成日常百事。

  [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
  [![Google Play](https://img.shields.io/badge/Google%20Play-OneBox-green?logo=google-play)](https://play.google.com/store/apps/details?id=com.shifenmiao.app)
</div>

## 应用截图

<table>
  <tr>
    <td><img src="fastlane/metadata/android/zh-CN/images/phoneScreenshots/01%20-%20Tools.png" width="180" alt="工具" /></td>
    <td><img src="fastlane/metadata/android/zh-CN/images/phoneScreenshots/02%20-%20AI%20Assistant.png" width="180" alt="AI 助手" /></td>
    <td><img src="fastlane/metadata/android/zh-CN/images/phoneScreenshots/03%20-%20Privacy.png" width="180" alt="隐私" /></td>
    <td><img src="fastlane/metadata/android/zh-CN/images/phoneScreenshots/04%20-%20Decision%20Turnboard.png" width="180" alt="决策转盘" /></td>
  </tr>
  <tr>
    <td><img src="fastlane/metadata/android/zh-CN/images/phoneScreenshots/05%20-%20Markdown%20Editor.png" width="180" alt="Markdown 编辑器" /></td>
    <td><img src="fastlane/metadata/android/zh-CN/images/phoneScreenshots/06%20-%20Peace%20%26%20Joy.png" width="180" alt="木鱼祈福" /></td>
    <td><img src="fastlane/metadata/android/zh-CN/images/phoneScreenshots/07%20-%20Speed%20Test.png" width="180" alt="测速" /></td>
    <td><img src="fastlane/metadata/android/zh-CN/images/phoneScreenshots/08%20-%20To-Do%20List.png" width="180" alt="待办清单" /></td>
  </tr>
</table>

## 下载

- **中国大陆**: [万宝盒官网](https://www.wanbaohe.com) · 小米 / 应用宝 / OPPO / vivo / 华为应用商店搜索「万宝盒」
- **海外**: [Google Play](https://play.google.com/store/apps/details?id=com.shifenmiao.app) · [国际版官网](https://www.oneboxable.com)

## 核心特性

- **图片与媒体工具**：包含裁剪、拼图、滤镜、抠图、格式转换、EXIF 编辑、文档扫描、二维码扫描等多个功能模块。
- **AI 能力**：提供 AI 对话、Agent 工具调用、AI 图片相关能力，并支持本地/远程 AI 引擎配置。
- **效率与生活工具**：包含文件浏览、文件传输、Markdown 编辑、记事/待办、日历、记账、单位换算、提词器等模块。
- **自适应顶层导航**：竖屏使用抽屉 + 底部栏，横屏使用 navigation rail，并由 `feature/app` 统一编排壳层与全局状态。

## 技术栈

- **UI**：Jetpack Compose、Material 3
- **导航 / 组件**：Decompose (`StackNavigation`, `childStack`, retained component)
- **依赖注入**：Hilt
- **代码生成**：KSP
- **构建约定**：`build-logic/convention` 中的自定义 Gradle convention plugins
- **数据持久化**：SQLite / Room（`AppDatabase`）、MMKV（按模块使用）
- **网络与图片**：Retrofit / OkHttp、Coil

## 仓库结构概览

| 路径 | 说明 |
| --- | --- |
| `app/` | Android 应用壳层与打包入口；`AppActivity.kt` 为主 Activity 入口 |
| `feature/app/` | 顶层壳层、主导航、全局 CompositionLocals、页面接线与自适应布局 |
| `feature/*` | 各业务 / 工具功能模块 |
| `feature/ai/` | AI 对话、Agent、工具调用链、AI 图像相关能力 |
| `feature/common/` | 通用业务能力、AI 引擎目录/同步等共享逻辑 |
| `feature/settings/` | 设置、主题、AI 引擎与工作模型配置 |
| `core/*` | 基础能力层：UI、domain、data、database、theme、settings 等 |
| `libs/*` | 项目内复用库 |
| `build-logic/convention/` | 自定义 Gradle convention plugins |
| `web/` | 文件传输模块的 Web 前端源码（esbuild 工程） |
| `fastlane/` | Google Play 商店文案与素材（fastlane supply 格式） |

## 关键架构入口

- **应用入口**：`app/src/main/java/com/shifenmiao/app/AppActivity.kt`
- **运行时导航根组件**：`feature/app/src/main/java/com/t8rin/imagetoolbox/feature/root/presentation/screenLogic/RootComponent.kt`
- **路由定义**：`core/ui/src/main/kotlin/com/t8rin/imagetoolbox/core/ui/utils/navigation/Screen.kt`
- **子页面工厂接线**：`feature/app/.../navigation/ChildProvider.kt`
- **子页面 Composable 包装**：`feature/app/.../navigation/NavigationChild.kt`
- **顶层壳层选择**：`feature/app/src/main/java/com/wanbaohe/app/screen/ScreenSelector.kt`
- **自适应导航布局**：`feature/app/src/main/java/com/wanbaohe/app/navigation/AdaptiveNavigationLayout.kt`

## 构建与运行

### 环境建议

- 使用仓库根目录下的 **Gradle Wrapper 9.5.1**
- 本仓库的 build-logic 与 CI 均以 **JDK 17** 为目标
- Android 构建目标：**Compile SDK 37 / Target SDK 37 / Min SDK 24**
- 当前构建工具版本：**Kotlin 2.4.0 / AGP 9.3.0**
- 版本信息以 `gradle/libs.versions.toml` 和 `gradle/wrapper/gradle-wrapper.properties` 为准，避免重复维护过期说明

### 常用命令

```bash
./gradlew :app:assembleOneboxUniversalDebug   # 单变体 debug 包
./gradlew tasks --group=assemble              # 查看全部打包任务
./gradlew :app:tasks
```

### 变体说明

`app` 模块使用两个 flavor 维度：

- `app`：`xiaomi` / `yyb` / `oppo` / `vivo` / `huawei` / `onebox` / `google`
- `abi`：`arm64` / `arm32` / `universal`

因此最终任务名会组合为：

- `:app:assembleHuaweiArm64Debug`
- `:app:installOneboxUniversalDebug`

更完整的命令示例与变体验证方式，请查看 `run.md`。

### 签名与密钥配置（可选）

不配置任何密钥文件也能正常同步与构建：

- release 签名使用默认占位值（不可用于正式发布）
- debug 构建回退到 AGP 默认 debug 签名
- 各第三方服务密钥（AI 服务商、Google Places 等）为空，对应功能不启用

需要自定义时，复制根目录模板文件并填入真实值：

- `keystore.properties.template` → `keystore.properties`：release 签名 + 各服务商密钥
- `keystore-google.properties.template` → `keystore-google.properties`：google flavor 独立签名

两个目标文件均已在 `.gitignore` 中，请勿提交真实值。

### 多渠道发布打包

项目根目录提供 `build_release.sh` 脚本，用于本地一次性打出 **6 渠道 × 2 架构 (arm64 / arm32) = 12 个 Release APK**，统一归集到 `release/` 目录，并生成 `release/manifest.txt` 汇总清单。

文件名沿用 Gradle 生成的 `OneBox-<version>-<渠道>-<架构>-release.apk`（如 `OneBox-1.2.3-xiaomi-arm64-release.apk`）。

**常用命令**

```bash
./build_release.sh                                # 全部 12 个
./build_release.sh --channel xiaomi huawei        # 指定渠道
./build_release.sh --abi arm64                    # 只打 64 位 (6 个)
./build_release.sh --channel onebox --abi arm32   # 单个组合
./build_release.sh --no-offline --clean           # 联网拉依赖 + 先 clean
./build_release.sh --skip-build                   # 不跑 gradlew，只重新归集
```

**参数说明**

| 参数 | 说明 |
| --- | --- |
| `--channel <a> [b...]` | 只构建指定渠道 (可空格分隔多个) |
| `--abi <arm64\|arm32>` | 只构建指定架构 (可空格分隔多个) |
| `--no-offline` | 覆盖离线模式配置，允许联网拉依赖（本仓库默认在线） |
| `--clean` | 构建前先执行 `clean` |
| `--skip-build` | 跳过 `gradlew`，只把已有 `app/build/outputs/apk/...` 复制到 `release/` |

**前置条件**

- 非 Linux 环境必须在根目录准备好 `keystore.properties`，否则脚本会直接退出

### CI 发布

`.github/workflows/android.yml` 定义了基于 Git tag 的发布流程：

- Ubuntu runner
- JDK 17
- 执行 `assembleRelease`
- 对 APK 签名并上传 release 产物

## AI / Agent 能力边界

项目中的 AI 能力不是单点实现，而是由多个模块协作完成：

- `feature/ai/`：AI 对话、Agent Loop、工具调用执行、交互式工具桥接
- `feature/common/`：AI 引擎目录、引擎同步、共享管理器
- `feature/settings/`：AI 引擎、模型与工作模式配置入口

其中工具调用链路基于：

- `ToolCallTaskManager`
- `AgentLoopExecutor`
- `AppDatabase`

## 安全与第三方服务说明

- **第三方密钥不随源码分发**：支付（微信/支付宝）、AI 服务商、Google Places 等密钥全部由可选的 `keystore.properties` 注入或保存在服务端，仓库中没有可用于正式环境的密钥。
- **后端域名与游客 Token 不入库**：API 域名和只读游客凭证通过 `keystore.properties` 在构建期注入，从本仓库构建的包默认不会连接生产服务器、云端内容为空；需要联网功能请接入自己的后端（见 `core/r/**/UrlConstantsFlavor.kt`）。
- **微信 appId / 企业微信 corpId** 为公开标识符，微信平台通过"包名 + 签名"校验调用方身份，第三方构建无法冒用。
- **Fork 与二次发布**：请自行修改 `applicationId`（`com.shifenmiao.app` 已被占用）、替换 `app/src/google/assets/google-services.json` 为你自己的 Firebase 配置，并接入自己的后端服务（API 域名见 `core/r/**/UrlConstantsFlavor.kt`）。

## 开发文档索引

- `run.md`：构建、安装、检查、Lint 常用命令
- `docs/modules.md`：各 feature / core / libs 模块中文目录
- `CONTRIBUTING.md`：基础贡献流程说明

## 开发约定摘要

- 优先沿用现有 convention plugins，不要轻易手写重复的 Android / Kotlin 配置
- 新页面接入通常需要同步检查：`Screen.kt`、`ChildProvider.kt`、`NavigationChild.kt`
- 顶级入口或快捷入口还要额外检查 `Screen.tabEntries`、`Navigation.kt`、`AdaptiveNavigationLayout.kt` 等壳层接线
- 存量代码命名空间遵循周边风格；新项目 / 新模块优先使用 `com.wanbaohe.*`
- 用户可见文本优先进入 Android 字符串资源

## 许可证

Apache-2.0，详见 `LICENSE`。本项目基于 [ImageToolbox](https://github.com/T8RIN/ImageToolbox)（T8RIN，Apache-2.0）二次开发，源文件中保留上游版权声明。
