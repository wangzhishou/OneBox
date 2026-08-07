# 视觉自动化模块架构说明

## 设计目标

在 App 内部实现"视觉驱动"的自动化操作，无需申请无障碍权限、无需 Root、无需系统级悬浮窗。核心思路是：**将当前界面截图发送给多模态 AI，AI 返回操作指令，App 在自己的 Window 内模拟执行。**

## 架构分层

```
┌─────────────────────────────────────────────────────────────┐
│  Presentation Layer (Compose UI)                             │
│  VisualAutomationTrigger.kt — 状态展示组件                   │
├─────────────────────────────────────────────────────────────┤
│  Controller Layer                                            │
│  VisualAutomationController — 状态机 + 主循环 + 动作编排     │
├─────────────────────────────────────────────────────────────┤
│  AI Layer                                                    │
│  VisualAIClient — 多模态请求构建 + 响应解析                  │
├─────────────────────────────────────────────────────────────┤
│  Action Layer                                                │
│  TouchInjector / TextInputInjector — 事件注入执行器          │
├─────────────────────────────────────────────────────────────┤
│  Capture Layer                                               │
│  ScreenshotCapturer — 截图 + 坐标系映射                      │
└─────────────────────────────────────────────────────────────┘
```

## 核心数据流

```
                ┌─────────────┐
   用户 ───────▶│ startTask() │
                └──────┬──────┘
                       ▼
              ┌────────────────┐
              │  CAPTURING     │◀──────── 截图 DecorView
              │  (截图中)      │           ScreenshotCapturer
              └───────┬────────┘
                      ▼
              ┌────────────────┐
              │  WAITING_AI    │◀──────── 多模态请求
              │  (等待 AI)     │           VisualAIClient
              └───────┬────────┘           (图片 + 文字)
                      ▼
              ┌────────────────┐
              │  EXECUTING     │◀──────── 解析 JSON 动作
              │  (执行中)      │           AIAction.parse()
              └───────┬────────┘
                      ▼
         ┌────────────────────────┐
         │   注入触摸/输入事件    │◀── TouchInjector / TextInputInjector
         │   dispatchTouchEvent() │    (在 DecorView 上分发)
         └───────────┬────────────┘
                     ▼
            ┌────────────────┐
            │  是 done?      │
            └───────┬────────┘
         是 ◄───────┼───────► 否
            ▼               ▼
     ┌──────────┐    ┌──────────┐
     │COMPLETED │    │  延迟    │───▶ 回到 CAPTURING
     │  (完成)  │    │ stepDelay│     (循环下一步)
     └──────────┘    └──────────┘
```

## 关键设计决策

### 1. 为什么不使用 AccessibilityService？

AccessibilityService 可以跨应用操作，但需要：
- 在系统设置中手动开启（用户路径长）
- 申请 `BIND_ACCESSIBILITY_SERVICE` 权限
- 系统可能限制后台使用

本模块的定位是 **App 内部自动化**，不需要跨应用能力。通过 `Activity.window.decorView.dispatchTouchEvent()` 即可在自有 Window 内完整模拟用户操作，包括 Material 涟漪效果、点击回调等。

### 2. 坐标系映射

AI 看到的截图尺寸与实际屏幕像素可能不一致（如截图压缩、屏幕密度差异）。映射公式：

```
actualX = aiX * screenWidth  / screenshotWidth
actualY = aiY * screenHeight / screenshotHeight
```

截图时记录 `decorView.width/height` 作为 screenshot 尺寸，执行时重新获取当前屏幕尺寸进行映射，确保坐标准确。

### 3. 多模态消息格式

复用项目已有的 OpenAI 兼容消息格式，图片以 Base64 Data URI 嵌入：

```json
{
  "messages": [
    {
      "role": "system",
      "content": "你是一个 Android App 自动化助手..."
    },
    {
      "role": "user",
      "content": [
        { "type": "text", "text": "任务目标：帮我搜索天气预报" },
        { "type": "image_url", "image_url": { "url": "data:image/jpeg;base64,/9j/4AAQ..." } }
      ]
    }
  ],
  "stream": false
}
```

### 4. 动作解析的容错设计

AI 可能返回 Markdown 代码块、多余解释文字等。`AIAction.parse()` 的处理策略：

1. 去除首尾空白
2. 尝试从 ` ```json ` 代码块中提取 JSON
3. 使用 `ignoreUnknownKeys = true` 的宽松 JSON 解析
4. 解析失败时返回 `AIAction.Error`，不会崩溃

### 5. 防无限循环机制

- `maxSteps` 默认限制 20 步
- 每步强制延迟 `stepDelayMs`（默认 800ms），给 UI 和网络请求留出时间
- 历史记录 `actionHistory` 传给 AI 上下文，让 AI 知道已经做过什么

### 6. 状态机设计

使用 `StateFlow<AutomationState>` 暴露状态，UI 可以监听并展示不同界面：

| 状态 | 含义 | UI 建议 |
|---|---|---|
| IDLE | 空闲 | 显示"启动"按钮 |
| RUNNING | 任务运行中 | 全局遮罩 |
| CAPTURING | 截图中 | 闪烁或短暂提示 |
| WAITING_AI | 等待 AI 响应 | 加载动画 |
| EXECUTING | 执行动作中 | 显示当前动作 |
| COMPLETED | 任务完成 | 成功提示 |
| ERROR | 出错 | 错误提示 + 重试按钮 |

## 依赖关系

```
feature:visual-automation
    ├── core:model         (AIAction, RequestMessage, ChatCompletionRequest 等)
    ├── core:ui            (Compose 基础)
    ├── core:base          (工具类)
    ├── core:network       (OpenAICompatibleService, OwnProxyAIService)
    └── feature:common     (AIEngineManager, AIPromptExecutor)
```

`feature:app` 需要引入本模块：

```kotlin
// feature/app/build.gradle.kts
implementation(projects.feature.visualAutomation)
```

## 扩展点

### 添加新动作类型

1. 在 `AIAction.kt` 的 sealed class 中添加新数据类
2. 在 `ActionWrapper` 中添加对应字段
3. 在 `VisualAutomationController.executeAction()` 中添加执行逻辑
4. 更新系统提示词 `SYSTEM_PROMPT`，告诉 AI 新动作的格式

### 替换截图方式

`ScreenshotCapturer.capture()` 默认使用 `decorView.draw(canvas)`。如需更高精度（含系统状态栏）或视频流截图，可替换为 `MediaProjection` 实现。

### 自定义 AI 提示词

修改 `VisualAIClient.SYSTEM_PROMPT`，可以：
- 限定操作范围（如"只能点击按钮，不能滑动"）
- 添加业务领域知识（如"设置页面的搜索图标在右上角"）
- 调整坐标精度要求

## 已知限制

1. **无法操作系统键盘**：系统输入法属于另一个 Window，不在当前 App 的 DecorView 范围内
2. **Popup/Dialog 有独立 Window**：如果 Dialog 使用了新的 Window（如 `DialogFragment`），需要在其自身的 DecorView 上分发事件
3. **AI 模型依赖**：必须使用支持图片输入的多模态模型（GPT-4o、Claude 3、Gemini、通义千问 VL 等）
4. **坐标精度依赖截图质量**：JPEG 压缩或截图尺寸过小可能导致 AI 坐标估算偏差
