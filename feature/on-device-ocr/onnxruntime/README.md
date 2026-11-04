# PP-OCR 专用 ONNX Runtime

## 当前方案

`libs/onnxruntime-android-1.31.1-ppocrv6.aar` 是从 ONNX Runtime `v1.31.1`（提交 `6af1a932e2286d5c58166085d24c68783033edee`）构建的精简版，只服务当前 `ppocrv6-small-e47f928e` 模型包：

- `det.onnx`：文字检测；
- `rec.onnx`：文字识别；
- `ori.onnx`：180° 方向分类。

模型文件、下载地址、模型校验、OCR 业务代码和 `ALL_OPT` 图优化均未改变。Runtime 仍直接加载 ONNX 格式，不是 minimal build，也没有改用 ORT 格式。

## 裁剪内容

使用 reduced-operator build，只保留三个模型的原始图和 `ALL_OPT` 优化图实际需要的算子。完整算子清单见 `ppocrv6-ops.config`。

保留 31 种 `ai.onnx` 算子：

```text
Add, AveragePool, BatchNormalization, Concat, Constant, Conv, ConvTranspose,
Div, Erf, GlobalAveragePool, HardSigmoid, Identity, LayerNormalization, MatMul,
MaxPool, Mul, Pow, ReduceMean, Relu, Reshape, Resize, Shape, Sigmoid,
SimplifiedLayerNormalization, Slice, Softmax, Sqrt, Squeeze, Sub, Transpose,
Unsqueeze
```

保留 5 种优化后产生的 `com.microsoft` 算子：

```text
FusedConv, FusedMatMul, Gelu, QuickGelu, SkipLayerNormalization
```

其余未被当前模型使用的算子内核均不编入 Runtime。`FusedConv`、`Gelu` 等不出现在原始模型清单里，但会由 `ALL_OPT` 在加载模型时生成，不能只按原始图裁剪。

以下能力没有裁剪：

- CPU、NNAPI、XNNPACK、WebGPU 构建能力；
- Java/JNI API 和 Runtime 图优化；
- LTO 与 Release 构建；
- 动态检测尺寸、动态识别宽度和方向分类。

AAR 只构建 `arm64-v8a`，不包含 `armeabi-v7a`、`x86`、`x86_64`。App 本来就只打 arm64 包，因此其他 ABI 的移除不构成额外 APK 收益。

## 实测结果

同一工作区构建 Google arm64 release：

- 修改前：99,494,161 字节；
- 修改后：96,853,785 字节；
- 减少 2,640,376 字节，约 2.64 MB（2.65%）。

`libonnxruntime.so` 未压缩大小由 34,208,672 字节降至 26,046,504 字节。

已通过 `:app:assembleGoogleArm64Release`，并在 Release 模拟器用同一张包含中英日、长行和倒置文字的图片对照，6 行识别结果与完整 Runtime 一致。

## 重新构建

在 macOS 上执行：

```bash
./scripts/build_onnxruntime_ocr.sh
```

脚本使用本机 Android NDK 28.1、Java 17、Python 3，以及固定版本的 ONNX Runtime 和 vcpkg。CMake、Ninja 不在 PATH 时会下载到构建目录并校验 SHA-256。成功后覆盖 `libs/` 中的定制 AAR。

如果更换模型包，必须重新核对原始图和 `ALL_OPT` 优化图的算子并更新 `ppocrv6-ops.config`，不能只替换模型。新增算子未被编入时会在创建 Session 或推理阶段报 `ORT_NOT_IMPLEMENTED`。

## 恢复完整 ONNX Runtime

只需把 `feature/on-device-ocr/build.gradle.kts` 中的：

```kotlin
implementation(files("libs/onnxruntime-android-1.31.1-ppocrv6.aar"))
```

改回：

```kotlin
implementation(libs.onnxruntime.android)
```

版本目录中仍保留官方 `onnxruntime-android` 依赖定义。恢复后不需要修改模型或业务代码。

如果以后不再维护定制构建，可同时删除 `libs/`、`onnxruntime/` 和 `scripts/build_onnxruntime_ocr.sh`；保留这些文件但切换回官方依赖不会影响运行。
