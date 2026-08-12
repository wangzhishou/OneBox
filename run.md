# Wanbaohe 常用命令（Android 编译/安装/检查）

所有命令默认在项目根目录执行。

- macOS / Linux：`./gradlew ...`
- Windows：`gradlew.bat ...`

## 环境前提

- 优先使用仓库自带的 **Gradle Wrapper 9.5.0**
- JDK：**17**
- Android 构建目标：**Compile SDK 36 / Target SDK 36 / Min SDK 23**
- 当前构建工具版本：**Kotlin 2.3.21 / AGP 9.2.0**
- 版本信息以 `gradle/libs.versions.toml` 和 `gradle/wrapper/gradle-wrapper.properties` 为准

## 变体（Variant）命名规则

本项目 `app` 模块有 2 个 flavor 维度：`app` + `abi`（见 `app/build.gradle.kts`）。

- app flavors：`xiaomi` / `yyb` / `oppo` / `vivo` / `huawei` / `onebox`
- abi flavors：`arm64` / `universal`（仅 64 位）
- buildTypes：`Debug` / `Release`

最终 Variant 名称形如：`<AppFlavor><AbiFlavor><BuildType>`（首字母大写驼峰）

示例：

- `HuaweiArm64Debug`
- `OneboxUniversalRelease`

## 先查任务名（推荐）

不同机器/环境下可用的 `install*`、`assemble*` 任务会很多，先列出来再复制最稳：

```bash
./gradlew tasks --group=assemble
./gradlew tasks --group=install
./gradlew tasks --group=verification
```

只看 `app` 模块的任务（更聚焦，但输出更长）：

```bash
./gradlew :app:tasks
```

## 常用构建（APK / AAB）

清理：

```bash
./gradlew clean
```

快速编译校验（会编译所有 Debug 变体，可能较慢）：

```bash
./gradlew :app:assembleDebug
```

构建指定 APK（推荐直接指定 Variant，输出更可控）：

```bash
./gradlew :app:assembleHuaweiArm64Debug
./gradlew :app:assembleOneboxUniversalDebug
./gradlew :app:assembleOneboxArm64Release
```

构建 AAB（Play 上架常用）：

```bash
./gradlew :app:bundleOneboxArm64Release
./gradlew :app:bundleOneboxUniversalRelease
```

构建所有 Release 变体（通常很慢）：

```bash
./gradlew :app:assembleRelease
```

## 产物路径

- APK：`app/build/outputs/apk/<variant>/`
- AAB：`app/build/outputs/bundle/<variant>/`

## 查看连接设备（ADB）

列出已连接的设备（基本）：

```bash
adb devices
```

列出已连接的设备（详细信息，包含设备型号/系统版本）：

```bash
adb devices -l
```

无设备时常见排查：

1. 确认 USB 调试已在手机上开启（设置 → 开发者选项 → USB 调试）
2. 重新插拔 USB 线或重启无线连接
3. 重启 ADB 服务：`adb kill-server && adb start-server`

## 安装到设备（ADB 已连接）

```bash
./gradlew :app:installHuaweiArm64Debug
./gradlew :app:installOneboxUniversalDebug
```

有些环境下也可以直接跑根任务（不带 `:app:` 前缀），以 `tasks --group=install` 的结果为准：

```bash
./gradlew installHuaweiArm64Debug
```

## 检查与诊断

Lint：

```bash
./gradlew :app:lintHuaweiArm64Debug
```

## 常用排错参数（复制到命令末尾）

```bash
--stacktrace
--info
--debug
```

示例：

```bash
./gradlew :app:assembleHuaweiArm64Debug --stacktrace --info
adb pull /data/data/<pkg>/cache/startup_trace.log，查看启动性能日志
```

## 签名文件提示（macOS）

本项目在非 Linux 环境会读取根目录的 `keystore.properties`（见 `app/build.gradle.kts`），如果本地没有该文件，Gradle 配置阶段可能会直接报错。需要的话把签名文件放到根目录再执行上述命令。

