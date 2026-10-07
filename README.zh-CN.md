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
    <td><img src=".github/readme/zh/01.webp?v=3" width="180" alt="90+ 工具一个 App 全装下" /></td>
    <td><img src=".github/readme/zh/02.webp?v=3" width="180" alt="说句话 AI 帮你干活" /></td>
    <td><img src=".github/readme/zh/03.webp?v=3" width="180" alt="记账说一句话就好" /></td>
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

## 功能总览

万宝盒共 **104 个功能模块**。本地工具全程本机执行,只有与所选大模型的对话会联网。下表按分类列出全部模块——模块名对应仓库里的 `feature/*` 目录。

### AI 与智能

| 模块 | 功能 | 简介 |
| --- | --- | --- |
| `feature/ai` | AI 聊天与智能体 | 多模型对话、双模型对决、Prompt 执行、Agent 工具调用、Token 统计 |
| `feature/ai-image` | AI 绘图 | 文字描述生成图片,支持分享 / 复制 / 编辑 |
| `feature/ai-detect` | AI 检测 | 检测一段文本或一张图片是否由 AI 生成 |
| `feature/dsh-client` | DSH 客户端 | 在手机上指挥电脑里的 [DeepSeek Harness](https://github.com/deepseek-ai/deepseek-harness) Agent——扫码配对、端到端加密,支持局域网直连与云端中继(电脑端配合 [`onebox-dsh-bridge`](https://github.com/wangzhishou/onebox-dsh-bridge) 插件) |
| `feature/visual-automation` | 视觉自动化 | 截屏传给 AI 多模态模型,实现基于截图的 UI 自动化操作 |

### 图像查看与编辑

| 模块 | 功能 | 简介 |
| --- | --- | --- |
| `feature/image-viewer` | 图片查看器 | 全屏沉浸式查看,多图滑动浏览、双击/捏合缩放(最大 5 倍)、网络图片下载 |
| `feature/image-preview` | 图片预览 | 多图预览与管理,是进入编辑 / 裁剪 / 滤镜等子功能的入口 |
| `feature/single-edit` | 单图编辑 | 一站式编辑:裁剪、滤镜、绘制、色调曲线,支持尺寸调整、格式与质量、原图对比 |
| `feature/draw` | 画笔绘制 | 自由画布:钢笔/霓虹笔/荧光笔/模糊/像素化/文字/贴纸/斑点修复,形状路径与泛洪填充 |
| `feature/markup-layers` | 图片创作 | 图层系统创作:多图层(增删/排序/合并/混合/不透明度)+ 画笔/文字/形状/贴纸 |
| `feature/crop` | 图片裁剪 | 默认裁剪(带旋转)、无旋转裁剪、自由角点裁剪,多种形状轮廓与宽高比 |
| `feature/image-stitch` | 图片拼接 | 水平/垂直/网格拼接,可配置对齐方式、间距、渐隐边缘 |
| `feature/image-splitting` | 图片分割 | 按行×列网格把单张图片拆成多张子图 |
| `feature/image-stacking` | 图片叠加 | 将多张图片按层叠方式合成一张图 |
| `feature/image-cutting` | 图片裁切 | 按水平/垂直起止百分比精确裁切指定区域,支持反选 |
| `feature/compare` | 图片对比 | 并排或叠加对比两张图片,滑块对比与像素级差异高亮 |
| `feature/load-net-image` | 网络图片加载 | 输入 URL 自动解析页面图片资源,支持预览与下载 |
| `feature/pick-color` | 图片取色 | 点击/拖拽拾取像素颜色,支持平移与放大模式 |
| `feature/collage-maker` | 拼图制作 | 最多 10 张图片拼贴,多种模板布局、间距、圆角、背景色 |
| `feature/text-card` | 图文卡片 | 把文字语录做成精美卡片,支持纸张背景、图层与 AI 配图 |

### 滤镜与特效

| 模块 | 功能 | 简介 |
| --- | --- | --- |
| `feature/filters` | 图片滤镜 | 100+ 种滤镜(模糊、色彩、艺术、边缘检测、变形等),支持滤镜蒙版局部应用 |
| `feature/gradient-maker` | 渐变生成 | 纯渐变/渐变叠加/Mesh 渐变,可配置渐变类型与颜色停止点 |
| `feature/mesh-gradients` | Mesh 渐变 | Mesh 渐变收藏/浏览,在线下载渐变资源包,可跳转编辑器 |
| `feature/noise-generation` | 噪声纹理 | 基于 FastNoiseLite 的程序化噪声纹理(OpenSimplex2/Perlin/Value 等) |
| `feature/ascii-art` | ASCII 艺术 | 把图片转换成 ASCII 字符组成的文本艺术画 |
| `feature/color-tools` | 颜色工具 | 颜色信息(HEX/RGB/HSL)、和谐关系分析、阴影/渐变生成、直方图分析 |
| `feature/palette-tools` | 调色板提取 | 从图片提取主色调生成调色板,支持 Material You 风格与自定义导出 |

### 格式转换与压缩

| 模块 | 功能 | 简介 |
| --- | --- | --- |
| `feature/format-conversion` | 格式转换 | JPEG/PNG/WebP 等格式互转,质量调节、EXIF 保留、批量处理 |
| `feature/resize-convert` | 尺寸调整转换 | 尺寸调整 + 格式转换一体,宽高/预设/缩放模式,批量处理 |
| `feature/weight-resize` | 按大小压缩 | 按目标文件大小压缩图片,实时显示压缩前后大小对比 |
| `feature/limits-resize` | 限制尺寸缩放 | 在最大宽高限制内缩放,跳过/重编码/缩放三种模式 |
| `feature/gif-tools` | GIF 工具 | 多图合成 GIF,可配置帧率、重复次数、尺寸,逐帧预览 |
| `feature/apng-tools` | APNG 工具 | 多图合成 APNG,可配置帧延迟、重复次数 |
| `feature/webp-tools` | WebP 工具 | 多图合成 WebP 动图,可配置帧延迟、重复次数 |
| `feature/svg-maker` | SVG 生成器 | 将位图转换为 SVG 矢量格式 |
| `feature/zip` | 压缩归档 | 将图片打包为 ZIP |

### 元数据与编码

| 模块 | 功能 | 简介 |
| --- | --- | --- |
| `feature/edit-exif` | 编辑 EXIF | 查看和修改相机型号、拍摄时间、GPS 坐标、版权信息等 |
| `feature/delete-exif` | 删除 EXIF | 批量剥离 EXIF 保护隐私,也可添加新 EXIF 数据 |
| `feature/base64-tools` | Base64 工具 | 图片与 Base64 字符串双向转换 |
| `feature/checksum-tools` | 校验和工具 | 多种哈希算法计算与比对文件/文本校验值 |

### 文档处理

| 模块 | 功能 | 简介 |
| --- | --- | --- |
| `feature/pdf-tools` | PDF 工具箱 | 预览、图片转 PDF、PDF 转图片、合并、拆分、压缩、旋转、页面管理 |
| `feature/ocr-document` | OCR 识别 | OCR 文档识别与格式转换,任务状态追踪、结果预览与导出 |
| `feature/document-scanner` | 文档扫描 | 扫描纸质文档生成图片 / PDF |
| `feature/markdown-edit` | Markdown 编辑器 | 所见即所得编辑,支持导出 PDF |
| `feature/code-editor` | 代码编辑器 | 语法高亮、文件读写与历史记录 |

### 水印与证件

| 模块 | 功能 | 简介 |
| --- | --- | --- |
| `feature/watermarking` | 图片水印 | 文字/图片/数字隐写/印章水印,可配置位置/旋转/透明度/平铺 |
| `feature/camera-watermark` | 相机水印 | 为照片添加时间/地点/设备等水印信息 |
| `feature/id-photo` | 证件照 | 标准证件照尺寸裁剪,支持背景更换与批量导出 |
| `feature/wallpapers-export` | 壁纸导出 | 读取并保存系统壁纸与锁屏壁纸 |

### 二维码与扫描

| 模块 | 功能 | 简介 |
| --- | --- | --- |
| `feature/scan-qr-code` | 二维码工具 | 扫描(摄像头/图片)与生成二维码/条码,涵盖文本/URL/WiFi/联系人等类型 |

### 测量与实用工具

| 模块 | 功能 | 简介 |
| --- | --- | --- |
| `feature/unit-converter` | 单位转换 | 长度/重量/温度/面积/体积/速度/时间等多种物理量换算 |
| `feature/compass` | 电子罗盘 | 罗经盘与指南针双表盘,实时方位、二十四山坐向、磁偏角与校准提示 |
| `feature/measurement` | 测量工具 | 屏幕直尺与量角器,支持厘米/英寸切换 |
| `feature/altitude` | 海拔测量 | 通过 GPS 获取当前海拔,支持历史记录 |
| `feature/speed-test` | 网络测速 | 测量下载/上传速度,仪表盘动画展示 |
| `feature/calendar` | 万年历 | 日历查看、黄历宜忌、八字排盘、公农历转换 |
| `feature/schedule` | 日程管理 | 创建/管理日程事件,系统日历同步与提醒通知 |
| `feature/teleprompter` | 提词器 | 文稿编辑 + 全屏播放,滚动速度/字号/颜色调节 |
| `feature/dead-pixel-test` | 坏点检测 | 纯色色板全屏检测屏幕坏点,支持颜色切换与网格叠加 |
| `feature/marquee` | 跑马灯 | 全屏滚动文字,LED 风格与字幕模式 |
| `feature/audio-cover-extractor` | 音频封面提取 | 从音频文件提取专辑封面图片 |

### 加密与安全

| 模块 | 功能 | 简介 |
| --- | --- | --- |
| `feature/cipher` | 图片加密 | 图片加密/解密,支持密码输入与加密类型切换 |
| `feature/password-vault` | 密码保险箱 | 本地加密存储管理账号密码,支持分类筛选与搜索 |

### 生活效率

| 模块 | 功能 | 简介 |
| --- | --- | --- |
| `feature/bookkeeping` | 记账本 | 日常收支记录与统计图表分析,可让 AI 代记账 |
| `feature/marktodo` | 待办事项 | 按主题管理待办任务,支持标签、优先级、截止日与筛选排序 |
| `feature/habit-tracker` | 习惯打卡 | 自定义习惯每日打卡,带提醒、连续天数与打卡率统计 |
| `feature/household-items` | 物品管理 | 记录物品存放位置、保质期与备注,可让 AI 代记代查 |
| `feature/period` | 经期记录 | 记录经期起止、流量、症状与心情,自动预测下次经期 |
| `feature/record-center` | 健康记录 | 统一记录血压、体重、血糖等 12 类指标,看趋势与 AI 解读 |
| `feature/loan-calculator` | 贷款计算器 | 等额本息/等额本金,计算月供、总利息与还款计划 |
| `feature/lifetime` | 人生时间线 | 记录重要事件与里程碑,时间轴展示生命历程 |
| `feature/decision-wheel` | 决定转盘 | 自定义选项的随机转盘,帮助做选择 |
| `feature/blessing-wall` | 祝福墙 | 展示/编辑祝福语卡片,支持背景音乐与分享 |
| `feature/poem` | 中国古诗词 | 随机赏诗/关键词搜索/AI 解读,本地历史与收藏 |
| `feature/iching-divination` | 易经六爻 | 摇一摇起六爻卦,查看本卦、变卦、动爻与 AI 解卦 |

### 游戏娱乐

| 模块 | 功能 | 简介 |
| --- | --- | --- |
| `feature/game2048` | 2048 | 经典数字滑动合并 |
| `feature/xiangqi` | 中国象棋 | 人机对弈/在线对战/棋谱库/残局分析 |
| `feature/gomoku` | 五子棋 | 15×15 棋盘、人机对弈(LLM)、AI 对战、棋谱库、复盘分析 |
| `feature/chess` | 国际象棋 | 标准规则(易位/过路兵/升变)、人机对弈(LLM)、棋谱库、复盘分析 |
| `feature/minesweeper` | 扫雷 | 多种难度、计时、旗帜标记 |
| `feature/sudoku` | 数独 | 三档难度、提示、撤销、计时、战绩统计 |
| `feature/survive30s` | 生存 30 秒 | 限时躲避障碍物小游戏 |
| `feature/dice-roller` | 投骰子 | D4/D6/D8/D10/D12/D20 多种骰子 |

### 社交与内容

| 模块 | 功能 | 简介 |
| --- | --- | --- |
| `feature/online` | 内容广场 | 应用推荐/笔记/小程序等多 Tab 聚合 |
| `feature/blog` | 博客与反馈 | 查看文章、创建用户反馈 |
| `feature/profile` | 个人中心 | VIP 等级、数据备份恢复、关于我们 |
| `feature/notification` | 消息中心 | 通知列表与未读角标 |
| `feature/ad-watch` | 广告看看看 | 看激励广告赚积分,每日有次数上限 |

### 文件与传输

| 模块 | 功能 | 简介 |
| --- | --- | --- |
| `feature/file-browser` | 文件浏览器 | 目录导航/搜索/排序/多选操作 |
| `feature/file-transfer` | 文件传输 | 局域网文件传输,二维码配对、加密传输 |
| `feature/cloud-storage` | 云存储 | 云端文件浏览/上传/下载/删除 |
| `feature/media-picker` | 媒体选择器 | 供其他工具复用的图片/视频选择能力 |

### 系统与框架

| 模块 | 功能 | 简介 |
| --- | --- | --- |
| `feature/app` | 应用主壳 | 入口导航框架,竖屏抽屉+底栏、横屏导航栏的自适应布局 |
| `feature/settings` | 设置中心 | AI 引擎/主题/显示/TTS/系统 Prompt/授权码等配置 |
| `feature/common` | 公共基础 | 通用 UI 基座、数据同步、分类管理 |
| `feature/search` | 全局搜索 | 搜索功能/内容/消息,快速导航 |
| `feature/quick-tiles` | 快捷磁贴 | 通知栏快捷操作(截屏/取色/扫码等) |
| `feature/webview` | 内置浏览器 | 网页浏览,支持多标签页、书签与历史记录 |
| `feature/login` | 登录注册 | 用户认证流程与用户协议 |
| `feature/wechat` | 微信集成 | 微信 SDK 授权登录与支付回调 |
| `feature/demo` | 组件展示 | 开发者调试用功能入口网格 |
| `feature/libraries-info` | 开源许可 | 第三方开源库许可证列表 |
| `feature/library-details` | 库详情 | 单个开源库许可证详情 |
| `feature/boardgame` | 棋类 UI 公共库 | 棋类通用界面组件,供五子棋/国际象棋复用 |

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

- 使用仓库根目录下的 **Gradle Wrapper 9.7.0**
- 本仓库的 build-logic 与 CI 均以 **JDK 17** 为目标
- Android 构建目标：**Compile SDK 37 / Target SDK 37 / Min SDK 24**
- 当前构建工具版本：**Kotlin 2.4.0 / AGP 9.3.1**
- 版本信息以 `gradle/libs.versions.toml` 和 `gradle/wrapper/gradle-wrapper.properties` 为准，避免重复维护过期说明

### 常用命令

```bash
./gradlew :app:assembleGoogleArm64Debug       # 单变体 debug 包
./gradlew tasks --group=assemble              # 查看全部打包任务
./gradlew :app:tasks
```

### 变体说明

`app` 模块使用两个 flavor 维度：

- `app`：`xiaomi` / `yyb` / `oppo` / `vivo` / `huawei` / `onebox` / `google` / `foss`
  （`foss` 为完全 FOSS 构建：无 GMS、Firebase、微信/支付宝 SDK，也是 CI 校验的变体）
- `abi`：`arm64`（仅 64 位）

**默认使用 `google`（海外 / Play 渠道）变体做构建与验证**，确需国内渠道时再替换。最终任务名会组合为：

- `:app:assembleGoogleArm64Debug`
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

项目根目录提供 `build_release.sh` 脚本，用于本地一次性打出 **7 渠道 × 1 架构 (arm64，仅 64 位) = 7 个 Release APK**，统一归集到 `release/` 目录，并生成 `release/manifest.txt` 汇总清单。

文件名沿用 Gradle 生成的 `OneBox-<version>-<渠道>-<架构>-release.apk`（如 `OneBox-1.2.3-xiaomi-arm64-release.apk`）。

**常用命令**

```bash
./build_release.sh                                # 全部 7 个
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
| `--abi <arm64>` | 只构建指定架构 (可空格分隔多个) |
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

GitHub Release 只挂海外包（google/foss 各 arm64），国内 6 渠道包只进 GitCode。
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
