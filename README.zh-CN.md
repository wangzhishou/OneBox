<div align="center">
  <img src=".github/readme/icon.webp" width="96" alt="万宝盒图标" />

  # 万宝盒 OneBox

  [English](README.md) | **简体中文**

  免费的 Android AI 智能体 & 工具箱:说句话,内置 Agent 就能驱动 90+ 应用内工具替你干活;
  图片与文档处理、效率与生活工具,一个入口完成日常百事。

  [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
  [![Google Play](https://img.shields.io/badge/Google%20Play-OneBox-green?logo=google-play)](https://play.google.com/store/apps/details?id=com.shifenmiao.app)
  [![Ko-fi](https://img.shields.io/badge/Ko--fi-donate-ff5e5b?logo=ko-fi&logoColor=white)](https://ko-fi.com/wangzhishou)
</div>

## 应用截图

<table>
  <tr>
    <td><img src=".github/readme/zh/01.webp?v=2" width="180" alt="90+ 工具一个 App 全装下" /></td>
    <td><img src=".github/readme/zh/02.webp?v=2" width="180" alt="说句话 AI 帮你干活" /></td>
    <td><img src=".github/readme/zh/03.webp?v=2" width="180" alt="记账说一句话就好" /></td>
    <td><a href="https://www.youtube.com/shorts/DSCKCWa0L2g"><img src=".github/readme/intro-zh.jpg?v=2" width="180" alt="视频介绍(YouTube Shorts)" /></a></td>
  </tr>
</table>

## 下载

- **中国**: [万宝盒官网](https://www.wanbaohe.com) · 小米 / 应用宝 / OPPO / vivo / 华为应用商店搜索「万宝盒」
- **海外**: [Google Play](https://play.google.com/store/apps/details?id=com.shifenmiao.app) · [国际版官网](https://www.oneboxable.com)

## AI 助手:不只是功能多

功能多,很多工具箱 App 都能做到。万宝盒内置的 AI 助手,让你不用再一个个找工具、记步骤——直接说话就行:

> “把图片转成 PDF。”
> “把账单截图记到账本。”
> “生成一份待办清单。”
> “联网查一下今天的股票行情。”

AI 理解、执行、反馈,一气呵成。

- **权限即边界,不碰隐私**:Agent 的一切操作都被限制在 App 自身的 Android 应用沙箱与已声明权限内——不读取通讯录、短信、通话记录等系统隐私数据;本地工具全程本机执行,只有与所选大模型的对话内容会联网。
- **Agent 主动调用工具**:AI Agent 能直接调用应用内 90+ 个本地工具(PDF 处理、图片编辑、文件管理、记账等),自动完成跨工具、多步骤的复杂任务;全程本地执行,带单工具超时与轮次限制,过程可预期。
- **画中画机器人,不必干等**:Agent 任务要跑一阵?直接退出 App 去做别的——AI 会话进行中离开时自动进入系统画中画,小窗里的机器人实时反馈任务进度,点一下即可回到结果。原生 PiP 能力,无需任何权限。
- **一个入口,多家模型**:一个对话入口接入多家主流大模型,支持自定义 AI 引擎与提示词管理。
- **自带 Key,零成本用 AI**:填入任意 OpenAI 兼容服务的 API Key 即可解锁全部 AI 能力——接上 Gemini、OpenRouter 等的免费额度,AI 助手和 Agent 零成本跑起来。(自定义引擎在海外版与自行构建的版本中完全开放;中国商店版为 VIP 能力)
- **代码透明,隐私可见**:所有代码公开可查——数据如何在本地处理、哪些信息绝不上传,一目了然。你不再需要“相信”我们,而是可以亲自验证。

## 核心特性

- **图片与媒体工具**：包含裁剪、拼图、滤镜、抠图、格式转换、EXIF 编辑、文档扫描、二维码扫描等多个功能模块。
- **AI 能力**：提供 AI 对话、Agent 工具调用、AI 图片相关能力，并支持本地/远程 AI 引擎配置。
- **效率与生活工具**：包含文件浏览、文件传输、Markdown 编辑、记事/待办、日历、记账、单位换算、提词器等模块。
- **DSH 客户端**：在手机上指挥电脑里运行的 [DeepSeek Harness](https://github.com/deepseek-ai/deepseek-harness) Agent——扫码配对、端到端加密,支持局域网直连与云端中继。电脑端配合开源插件 [`onebox-dsh-bridge`](https://github.com/wangzhishou/onebox-dsh-bridge) 使用(`dsh plugin add onebox-dsh-bridge`)。
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
./gradlew :app:assembleGoogleUniversalDebug   # 单变体 debug 包
./gradlew tasks --group=assemble              # 查看全部打包任务
./gradlew :app:tasks
```

### 变体说明

`app` 模块使用两个 flavor 维度：

- `app`：`xiaomi` / `yyb` / `oppo` / `vivo` / `huawei` / `onebox` / `google` / `foss`
  （`foss` 为完全 FOSS 构建：无 GMS、Firebase、微信/支付宝 SDK，也是 CI 校验的变体）
- `abi`：`arm64` / `universal`（仅 64 位）

**默认使用 `google`（海外 / Play 渠道）变体做构建与验证**，确需国内渠道时再替换。最终任务名会组合为：

- `:app:assembleGoogleUniversalDebug`
- `:app:assembleGoogleArm64Release`
- `:app:assembleHuaweiArm64Debug`

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

项目根目录提供 `build_release.sh` 脚本，用于本地一次性打出 **7 渠道 × 2 架构 (arm64 / universal，仅 64 位) = 14 个 Release APK**，统一归集到 `release/` 目录，并生成 `release/manifest.txt` 汇总清单。

文件名沿用 Gradle 生成的 `OneBox-<version>-<渠道>-<架构>-release.apk`（如 `OneBox-1.2.3-xiaomi-arm64-release.apk`）。

**常用命令**

```bash
./build_release.sh                                # 全部 14 个
./build_release.sh --channel xiaomi huawei        # 指定渠道
./build_release.sh --abi arm64                    # 只打 arm64 (7 个)
./build_release.sh --channel onebox --abi arm64   # 单个组合
./build_release.sh --no-offline --clean           # 联网拉依赖 + 先 clean
./build_release.sh --skip-build                   # 不跑 gradlew，只重新归集
```

**参数说明**

| 参数 | 说明 |
| --- | --- |
| `--channel <a> [b...]` | 只构建指定渠道 (可空格分隔多个) |
| `--abi <arm64\|universal>` | 只构建指定架构 (可空格分隔多个) |
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

### 更新提醒与国内分发

App 里"我的 → 关于 → 开源项目"那一行会显示最新版本（有新版本时角标 + 主题色副标题，
"我的"tab 上还有一个红点）。是否启动弹窗由后台远程配置 `appUpdate` 控制：

```json
"appUpdate": { "dialogEnabled": true, "dialogForVersionBelow": 150, "dialogCooldownHours": 24 }
```

- 未下发 = 只显示角标与副标题，不弹窗；老客户端不认识这个字段，完全不受影响。
- `dialogForVersionBelow` 用来把弹窗收窄到"版本特别老"的那一段装机，避免打扰新版本用户。
- 版本来源按渠道分：海外（google / foss）读 GitHub Releases，国内 6 渠道读 GitCode OpenAPI。

GitCode 的仓库镜像**只同步分支 / 标签 / 提交，不同步 Release 附件**，所以国内包要在 GitCode 上单独发一次。
正常发版不用手工做：**tag 推送时 CI 会额外构建国内 6 渠道（arm64）并自动发到 GitCode** ——
构建矩阵用 `matrix.include` 追加这 6 个组合，前置条件是配了 `GITCODE_TOKEN` secret、
且该 tag 已推到 GitCode（发布脚本用 tag 当 `target_commitish`）。

需要补发或离线发版时用脚本：

```bash
export GITCODE_TOKEN=xxx          # https://gitcode.com/setting/token-classic
git push gitcode --tags
./scripts/publish_gitcode_release.py --tag 1.4.0 --dir release --abi arm64
```

GitHub Release 只挂海外包（google/foss 各 universal + arm64），国内 6 渠道包只进 GitCode。
**GitCode 仓库必须公开**，否则匿名 API 返回 403。

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

## 赞助与支持

万宝盒由一人公司（OPC）独立开发与维护，服务器和 AI 资源成本有限。如果它帮到了你：

- **应用内打赏**：App 内「我的 → 请喝咖啡」，用积分请作者喝杯咖啡
- **合作与资源支持**：欢迎 AI 服务商、云厂商或合作伙伴提供 token / 资源赞助，请联系 admin@shifenmiao.com

每一份支持都会直接变成更好的功能和更稳定的服务。

## 交流与反馈

App 内「我的 → 加入社区」是与下面一致的入口：

- **海外社区**
  - **Discord**：[加入服务器](https://discord.gg/7j4hEBYGWv) —— 聊天、晒用法、抢先体验
  - **X(Twitter)**：[@OneBoxAndroid](https://x.com/OneBoxAndroid)
  - **YouTube**：[@OneBoxAndroid](https://www.youtube.com/@OneBoxAndroid) —— 介绍视频与功能演示
- **QQ 群**：[点击加入](https://qm.qq.com/q/1JOfn5KCue56UhXT1fRe6NgCLJB5sHFO)
- **微信群**：扫码加入（群二维码有时效，会定期更新；若失效请先提 Issue 提醒）

<div align="center">
  <img src=".github/readme/wechat-group.jpg" width="220" alt="微信群二维码" />
</div>

- **问题反馈与建议**：[GitHub Issues](https://github.com/wangzhishou/OneBox/issues) · [Discussions](https://github.com/wangzhishou/OneBox/discussions)

## 许可证

Apache-2.0，详见 `LICENSE`。本项目基于 [ImageToolbox](https://github.com/T8RIN/ImageToolbox)（T8RIN，Apache-2.0）二次开发，源文件中保留上游版权声明。

## 功能图鉴

万能宝盒，万事万物皆可 AI。全量开源、免费使用、数据在本地——18 张图看懂万宝盒能做什么：

<table>
  <tr>
    <td><img src=".github/readme/cards/zh/01.webp" width="240" alt="AI 语音记账" /></td>
    <td><img src=".github/readme/cards/zh/02.webp" width="240" alt="物品管理查重" /></td>
    <td><img src=".github/readme/cards/zh/03.webp" width="240" alt="经期记录" /></td>
  </tr>
  <tr>
    <td><img src=".github/readme/cards/zh/04.webp" width="240" alt="健康记录" /></td>
    <td><img src=".github/readme/cards/zh/05.webp" width="240" alt="习惯打卡" /></td>
    <td><img src=".github/readme/cards/zh/06.webp" width="240" alt="提词器" /></td>
  </tr>
  <tr>
    <td><img src=".github/readme/cards/zh/07.webp" width="240" alt="笔记" /></td>
    <td><img src=".github/readme/cards/zh/08.webp" width="240" alt="AI 操作记录" /></td>
    <td><img src=".github/readme/cards/zh/09.webp" width="240" alt="搜索与格式转换" /></td>
  </tr>
  <tr>
    <td><img src=".github/readme/cards/zh/10.webp" width="240" alt="功能自己做主" /></td>
    <td><img src=".github/readme/cards/zh/11.webp" width="240" alt="AI 干活你可以先走" /></td>
    <td><img src=".github/readme/cards/zh/12.webp" width="240" alt="先授权再执行" /></td>
  </tr>
  <tr>
    <td><img src=".github/readme/cards/zh/13.webp" width="240" alt="生活工具" /></td>
    <td><img src=".github/readme/cards/zh/14.webp" width="240" alt="手持弹幕" /></td>
    <td><img src=".github/readme/cards/zh/15.webp" width="240" alt="休闲小游戏" /></td>
  </tr>
  <tr>
    <td><img src=".github/readme/cards/zh/16.webp" width="240" alt="技术工具" /></td>
    <td><img src=".github/readme/cards/zh/17.webp" width="240" alt="实用工具" /></td>
    <td><img src=".github/readme/cards/zh/18.webp" width="240" alt="智能体与提示词" /></td>
  </tr>
</table>
