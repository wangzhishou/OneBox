# 视觉自动化模块使用说明

## 功能概述

截图当前 App 界面，发送给 AI 分析，AI 返回操作指令，App 自动执行（点击、滑动、输入文字等）。

**核心优势**：无需无障碍权限、无需 Root、无需系统级悬浮窗，仅在 App 内部生效。

## 快速开始

### 1. 确保模块已引入

`feature/app/build.gradle.kts` 中应有：

```kotlin
implementation(projects.feature.visualAutomation)
```

### 2. 在 Component 中注入

```kotlin
class MyComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val onNavigate: (Screen) -> Unit,
    val visualAutomationController: VisualAutomationController,
) : ComponentContext by componentContext {
    // ...
}
```

### 3. 在 Compose UI 中使用

```kotlin
@Composable
fun MyScreen(component: MyComponent) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by component.visualAutomationController.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // 触发按钮
        Button(
            onClick = {
                val activity = context.findActivity() as? Activity
                activity?.let {
                    component.visualAutomationController.startTask(
                        activity = it,
                        taskDescription = "帮我搜索天气预报"
                    )
                }
            },
            enabled = state == VisualAutomationController.AutomationState.IDLE
        ) {
            Text(
                when (state) {
                    VisualAutomationController.AutomationState.IDLE -> "启动 AI 自动化"
                    VisualAutomationController.AutomationState.RUNNING -> "运行中..."
                    VisualAutomationController.AutomationState.CAPTURING -> "截图中..."
                    VisualAutomationController.AutomationState.WAITING_AI -> "AI 思考中..."
                    VisualAutomationController.AutomationState.EXECUTING -> "执行中..."
                    VisualAutomationController.AutomationState.COMPLETED -> "已完成"
                    VisualAutomationController.AutomationState.ERROR -> "出错，点击重试"
                }
            )
        }

        // 停止按钮（运行中显示）
        if (state != VisualAutomationController.AutomationState.IDLE &&
            state != VisualAutomationController.AutomationState.COMPLETED
        ) {
            TextButton(
                onClick = { component.visualAutomationController.stopTask() }
            ) {
                Text("停止")
            }
        }
    }

    // 可选：显示自动化状态遮罩（运行中显示加载圈）
    VisualAutomationTrigger(component.visualAutomationController)

    // 观察 AI 动作（用于调试或日志）
    LaunchedEffect(Unit) {
        component.visualAutomationController.actions.collect { action ->
            when (action) {
                is AIAction.Click -> println("AI 点击: (${action.x}, ${action.y}) - ${action.reason}")
                is AIAction.Swipe -> println("AI 滑动: (${action.fromX},${action.fromY}) -> (${action.toX},${action.toY})")
                is AIAction.InputText -> println("AI 输入: ${action.text}")
                is AIAction.GoBack -> println("AI 返回")
                is AIAction.Wait -> println("AI 等待 ${action.durationMs}ms")
                is AIAction.Done -> println("任务完成: ${action.message}")
                is AIAction.Error -> println("错误: ${action.message}")
                else -> {}
            }
        }
    }

    // 观察执行结果
    LaunchedEffect(Unit) {
        component.visualAutomationController.results.collect { result ->
            when (result) {
                is AutomationResult.Success -> println("执行成功: ${result.action}")
                is AutomationResult.Failure -> println("执行失败: ${result.message}")
            }
        }
    }
}
```

## 支持的 AI 动作

AI 返回的 JSON 格式动作，模块会自动解析并执行：

| 动作类型 | JSON 示例 | 说明 |
|---|---|---|
| 点击 | `{"action":"click","x":320,"y":640,"reason":"点击确认按钮"}` | 单点点击 |
| 长按 | `{"action":"long_press","x":320,"y":640,"durationMs":800}` | 长按指定时间 |
| 滑动 | `{"action":"swipe","fromX":300,"fromY":800,"toX":300,"toY":400}` | 从 A 滑动到 B |
| 输入文字 | `{"action":"input_text","text":"Hello World"}` | 输入到当前焦点输入框 |
| 返回 | `{"action":"go_back"}` | 触发返回键 |
| 等待 | `{"action":"wait","durationMs":2000}` | 等待界面加载 |
| 完成 | `{"action":"done","message":"任务已完成"}` | 结束循环 |

## 配置参数

```kotlin
val controller = component.visualAutomationController

// 最大执行步数（防止无限循环）
controller.maxSteps = 20

// 每步之间的延迟（毫秒）
controller.stepDelayMs = 800L

// 是否显示点击位置红色指示器（调试用）
controller.showClickIndicator = true
```

## 绑定到悬浮机器人

如果想通过现有的悬浮机器人触发视觉自动化，在 `FloatingRobotDragController` 或相关组件中：

```kotlin
FloatingRobot(
    robotState = robotState,
    onRobotClick = {
        // 长按机器人启动视觉自动化
        component.visualAutomationController.startTask(
            activity = context.findActivity() as Activity,
            taskDescription = "根据当前界面，帮我完成接下来的操作"
        )
    },
    // ...
)
```

## 单独使用子功能

### 仅截图

```kotlin
val bitmap = ScreenshotCapturer.capture(activity)
val base64 = ScreenshotCapturer.captureToBase64(activity)
val dataUri = ScreenshotCapturer.captureToDataUri(activity)
```

### 仅注入点击

```kotlin
// 在协程中调用
TouchInjector.performClick(activity, x = 500f, y = 800f)
TouchInjector.performLongPress(activity, x = 500f, y = 800f, durationMs = 1000)
TouchInjector.performSwipe(activity, fromX = 300f, fromY = 800f, toX = 300f, toY = 400f)
```

### 仅输入文字

```kotlin
// 方式1：点击坐标聚焦后输入
TextInputInjector.inputText(activity, x = 500f, y = 600f, text = "搜索内容")

// 方式2：直接对已知 EditText 输入
TextInputInjector.inputTextDirectly(editText, text = "搜索内容", simulateTyping = true)
```

### 纯文本模式（模型不支持图片）

```kotlin
val action = visualAIClient.requestActionTextOnly(
    taskDescription = "当前在搜索页面，帮我点击搜索框",
    history = listOf("已进入搜索页面")
)
```

## 常见问题

**Q: AI 返回的坐标不准确怎么办？**
A: 确保使用的 AI 模型支持多模态（图片理解）。如果坐标有偏差，可在 `VisualAutomationController` 中调整 `stepDelayMs` 给 UI 更多加载时间。

**Q: 点击后没有反应？**
A: 检查目标坐标是否在 `Dialog` 或 `PopupWindow` 上。这些有独立的 Window，需要在对应的 DecorView 上分发事件。

**Q: 输入文字没效果？**
A: `input_text` 需要当前焦点是 `EditText`。如果点击后没有聚焦到输入框，AI 可能需要先返回一个 `click` 动作聚焦输入框，再返回 `input_text`。

**Q: 支持哪些 AI 模型？**
A: 任何支持 OpenAI 兼容多模态 API 的模型：GPT-4o、Claude 3、Gemini、通义千问 VL、Kimi 等。纯文本模式支持所有文本模型。

**Q: 会消耗多少流量？**
A: 每张截图约 100-300KB（JPEG 压缩），根据 `ScreenshotCapturer.captureToBase64` 的 quality 参数可调。
