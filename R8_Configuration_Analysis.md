# R8 配置与 1.5.1 后变更审查

## 范围

- 基线：`1.5.1`（`4ab23d26`）。
- 当前：`da9926ce`，共 56 个后续提交。
- 重点：新增端侧 OCR、相机、PDF、麻将、AI 服务设置，以及依赖升级。

## 构建配置

- `release` 已启用 `isMinifyEnabled = true`、`isShrinkResources = true`，并使用 `proguard-android-optimize.txt`。
- 已启用 R8 Full Mode；AGP 为 9.4.1，不需要额外开启旧版资源缩减开关。
- 1.5.1 后没有修改任何应用或模块的 keep 规则；主要新增风险来自 ONNX Runtime、CameraX、PDF 反射访问及新增序列化模型。
- mapping 文件由 release 构建生成，Google 渠道另有 Crashlytics mapping file id 配套上传流程。

## 本次结论

- `:app:assembleGoogleArm64Release` 已成功，R8、资源缩减、打包均通过。
- APK 包含 `libonnxruntime.so`、`libonnxruntime4j_jni.so` 和 `libopencv_java5.so`。ONNX Runtime AAR 自带规则只覆盖遥测类，不能覆盖 JNI 按名回查的模型对象。
- mapping 中 `OcrHistoryEntry`、`RecognizedLine`、`PixelPoint` 及其序列化构造器均保留；`CameraCaptureActivity` 名称与入口也保留。
- 152 首轮 release 走查发现，OCR 在 `OrtSession.run` 的输出转换阶段因 `TensorInfo` 等 JNI 类型被裁剪/混淆而原生崩溃；`app/proguard-rules.pro` 已补充 `ai.onnxruntime.**` 边界保留。修复后相册图片识别、扫描到 OCR、历史结果及最终包致命日志检查均通过。
- CameraX、麻将和 PDF 的本次静态审查未发现需要新增 keep 规则的入口。PDF 中两处私有字段反射早于本次基线，不属于后续回归。
- R8 的 249 条相同警告均来自既有微信 SDK 的非线性字节码栈映射，没有缺类或应用模型警告；Google 包虽不应使用微信能力，该警告不是此次新增问题。
- ONNX 启动时的 telemetry `UnsatisfiedLinkError` 是 AAR 自带 native 库未包含对应可选符号，库内已捕获并降级；与本次 JNI 输出转换崩溃无关。
- APK 资源表抽查了麻将、Snap Text、待办和日程条目，16 种打包语言的值均存在。

## 现有 keep 规则审查

以下按保留范围从大到小排列，仅记录后续处置建议；本次只新增 ONNX JNI 边界规则，其它规则未调整。

### 应缩小或删除的包级规则

- `core.filters.**`：代码确实通过 `Class.forName`、主构造器和 Kotlin 反射加载滤镜，但整包类名、接口实现、构造器等多条规则互相覆盖。保留实现类的构造器及序列化实际访问成员即可，删除整包和重复接口规则。
- `com.shifenmiao.ai.agent.tool.builtin.**`、`com.wanbaohe.**.ai.tool.**`：大量 DTO 已由 `@Serializable` 规则覆盖。按已证明会被反射合并破坏的具体 DTO 保留，不应把整个工具包的工具实现也一起固定。
- `com.shifenmiao.model.**`、`network.model.**`、`ai.model.**`：`keepnames`、字段保留和整包规则重复。以序列化注解及实际 Retrofit 泛型入口为准收敛；TTS 泛型响应可保留为明确的窄规则。
- `com.tencent.*`、广告聚合、百度语音、QWeather、JNA/resvg、`com.nlf.calendar.**`：分别服务于 SDK 反射、JNI 或按名加载。保留必要能力，但应按 SDK 自带规则与实际反射入口去重；农历和 JNA 的现有代码有明确反射依据，不能直接删除。
- `org.bouncycastle.jcajce.provider.**` 与 `org.bouncycastle.jce.provider.**` 各重复出现一次，删除重复行；实际算法提供类是否还需整包固定应另做加密/PDF 回归后收敛。
- `com.wanbaohe.explorer.**`、`org.eclipse.tm4e.**`、`org.joni.**` 等编辑器规则：未在新增代码中发现新反射入口，后续按代码编辑器实际动态加载点缩小，不在本次发布前改动。
- 模块目录中的 `file-browser`、`file-transfer`、`lifetime`、`code-editor`、`markdown-edit` 等整包规则当前没有被构建脚本注册，不影响 release；若以后注册，应先缩小范围。`libs/twain` 的 `-dontobfuscate` 同样未被注册，不能把它当作当前包未混淆的原因。

### 可依赖默认或库自带规则删除的项

- Parcelable `CREATOR`：默认优化配置和 Parcelize 已覆盖。
- `androidx.lifecycle.LiveData`、所有 Fragment 子类：没有对应动态加载依据，Manifest/XML 与库规则已覆盖。
- Retrofit 3.0.0 的 HTTP 注解、接口、泛型返回值、`Response`，以及 `kotlin.coroutines.Continuation`：库自带规则已覆盖，删除重复规则后应回归网络与流式响应。
- `@Serializable class * { *; }` 与字段级序列化规则存在覆盖关系。考虑到本项目曾出现横向类合并问题，应先保留并在独立优化任务中逐类验证，不和本次功能修复混在一起。
- EventBus 的 `@Subscribe` 方法规则对应真实反射调用；保留方法规则，`ThreadMode` 枚举规则可由库规则覆盖。

### 需要保留的窄规则与属性

- `SourceFile`、`LineNumberTable` 及注解/签名属性对崩溃定位和反射有必要，保留。
- 具体 Agent 请求/响应 DTO 的强 keep 有历史横向合并依据，在对应 release 序列化回归前保留。
- QWeather 的具体回调和 JWT 入口、EventBus 订阅方法、滤镜构造器等规则对应实际使用场景，但应优先使用条件成员规则，避免同时固定整包。

## 验证建议

- 最终改动后重新执行 Google 与国内单渠道 release 构建及双渠道资源合并。
- 在 release 包上依次验证麻将新局、OCR 模型下载/识别/历史重开、相机拍摄、PDF 加密与签名、AI 服务新增与模型拉取。
- 若后续清理上述规则，按滤镜、网络序列化、代码编辑器、广告/支付、JNA/resvg 分组做 UI Automator 或等价真机回归，不要一次性全局删除。
