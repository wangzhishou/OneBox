# 文件传输聊天功能 - 架构图

## 系统架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         Android 应用                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌────────────────────────────────────────────────────────┐     │
│  │              FileTransferScreen.kt (UI层)               │     │
│  │  • 聊天按钮 (FloatingActionButton)                      │     │
│  │  • 聊天对话框 (AlertDialog)                             │     │
│  │  • 消息列表 (LazyColumn)                                │     │
│  │  • 未读角标 (Badge)                                      │     │
│  └──────────────────┬─────────────────────────────────────┘     │
│                     │                                             │
│                     ▼                                             │
│  ┌────────────────────────────────────────────────────────┐     │
│  │          FileTransferComponent.kt (逻辑层)              │     │
│  │  • 聊天消息状态 (StateFlow<List<ChatMessage>>)          │     │
│  │  • 未读计数 (StateFlow<Int>)                            │     │
│  │  • sendTextMessage()                                    │     │
│  │  • sendFileMessage()                                    │     │
│  │  • clearUnreadCount()                                   │     │
│  └──────────────────┬─────────────────────────────────────┘     │
│                     │                                             │
│                     ▼                                             │
│  ┌────────────────────────────────────────────────────────┐     │
│  │          FileTransferService.kt (服务层)                │     │
│  │  • 前台服务                                              │     │
│  │  • 管理服务器生命周期                                     │     │
│  │  • getServer() - 暴露服务器实例                          │     │
│  └──────────────────┬─────────────────────────────────────┘     │
│                     │                                             │
└─────────────────────┼─────────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│               FileTransferServer.kt (HTTP服务器)                 │
├─────────────────────────────────────────────────────────────────┤
│  基于 NanoHTTPD                                                  │
│                                                                   │
│  聊天API:                                                         │
│  • GET  /api/chat/history    - 获取历史消息                      │
│  • GET  /api/chat/poll       - 轮询新消息                        │
│  • POST /api/chat/send       - 发送消息                          │
│                                                                   │
│  数据存储:                                                        │
│  • chatHistory: CopyOnWriteArrayList<ChatMessage>               │
│  • onChatMessageReceived: 回调函数                               │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      │ HTTP/JSON
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Web 浏览器端                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌────────────────────────────────────────────────────────┐     │
│  │                index.html (HTML结构)                    │     │
│  │  • 聊天按钮                                              │     │
│  │  • 聊天面板 (侧边栏)                                     │     │
│  │  • 消息容器                                              │     │
│  │  • 输入框                                                │     │
│  └──────────────────┬─────────────────────────────────────┘     │
│                     │                                             │
│                     ▼                                             │
│  ┌────────────────────────────────────────────────────────┐     │
│  │              app.js (JavaScript逻辑)                    │     │
│  │                                                          │     │
│  │  状态管理:                                               │     │
│  │  • chatMessages: []                                     │     │
│  │  • unreadMessages: 0                                    │     │
│  │  • isChatOpen: false                                    │     │
│  │  • lastMessageTimestamp: 0                              │     │
│  │                                                          │     │
│  │  核心功能:                                               │     │
│  │  • connectChat() - 启动轮询                             │     │
│  │  • pollChatMessages() - 每2秒轮询                       │     │
│  │  • sendChatMessage() - 发送消息                         │     │
│  │  • renderChatMessages() - 渲染消息                      │     │
│  │  • updateChatBadge() - 更新角标                         │     │
│  └────────────────────────────────────────────────────────┘     │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘
```

## 消息流转图

### 手机端发送消息流程

```
┌──────────────┐
│   用户输入    │
└──────┬───────┘
       │
       ▼
┌──────────────────────┐
│  输入框 TextField     │
└──────┬───────────────┘
       │ 点击发送
       ▼
┌─────────────────────────────┐
│ sendTextMessage()            │
│ • 创建 ChatMessage           │
│ • timestamp = now()          │
│ • sender = "mobile"          │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ FileTransferServer           │
│ • sendChatMessage(message)   │
│ • chatHistory.add(message)   │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ 更新UI                       │
│ • _chatMessages.value += msg │
│ • renderMessages()           │
└──────────────────────────────┘
```

### 浏览器端发送消息流程

```
┌──────────────┐
│   用户输入    │
└──────┬───────┘
       │
       ▼
┌──────────────────────┐
│  输入框 textarea      │
└──────┬───────────────┘
       │ 按Enter或点击发送
       ▼
┌─────────────────────────────┐
│ sendChatMessage()            │
│ • 创建消息对象               │
│ • sender = "browser"         │
└──────┬──────────────────────┘
       │
       ▼ POST /api/chat/send
┌─────────────────────────────┐
│ FileTransferServer           │
│ • handleChatSend()           │
│ • chatHistory.add(message)   │
│ • 通知Android端              │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ 更新浏览器UI                 │
│ • state.chatMessages.push()  │
│ • renderChatMessages()       │
└──────────────────────────────┘
```

### 浏览器端接收消息流程

```
┌─────────────────────────────┐
│ 定时器触发 (每2秒)            │
└──────┬──────────────────────┘
       │
       ▼ GET /api/chat/poll?lastTimestamp=xxx
┌─────────────────────────────┐
│ FileTransferServer           │
│ • handleChatPoll()           │
│ • 过滤新消息                 │
│ • return newMessages         │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ handleChatMessage()          │
│ • 检查是否重复               │
│ • 更新lastTimestamp          │
│ • 增加未读计数               │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ 渲染消息                     │
│ • renderChatMessages()       │
│ • updateChatBadge()          │
│ • scrollChatToBottom()       │
└──────────────────────────────┘
```

### 手机端接收消息流程

```
┌─────────────────────────────┐
│ 浏览器发送消息               │
│ POST /api/chat/send          │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ FileTransferServer           │
│ • handleChatSend()           │
│ • chatHistory.add(message)   │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ 回调触发                     │
│ onChatMessageReceived(msg)   │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ FileTransferComponent        │
│ • 更新状态                   │
│ • _chatMessages += message   │
│ • _unreadCount++             │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ UI自动更新                   │
│ • StateFlow触发重组          │
│ • 显示新消息                 │
│ • 显示未读角标               │
└──────────────────────────────┘
```

## 数据模型

```kotlin
data class ChatMessage(
    val id: String,              // 唯一ID
    val type: MessageType,       // TEXT, FILE, SYSTEM
    val content: String,         // 消息内容
    val sender: String,          // "mobile" 或 "browser"
    val timestamp: Long,         // 时间戳(毫秒)
    val fileName: String? = null,   // 文件名(可选)
    val fileSize: Long? = null,     // 文件大小(可选)
    val filePath: String? = null    // 文件路径(可选)
)
```

## 状态管理

### Android端 (FileTransferComponent)
```kotlin
_chatMessages: MutableStateFlow<List<ChatMessage>> = emptyList()
_unreadCount: MutableStateFlow<Int> = 0

// 观察者模式
chatMessages.collectAsState() // UI监听变化
```

### Web端 (app.js)
```javascript
state = {
    chatMessages: [],
    unreadMessages: 0,
    isChatOpen: false,
    lastMessageTimestamp: 0
}

// 主动渲染
renderChatMessages() // 手动更新DOM
```

## 网络通信

### HTTP轮询时序图

```
浏览器                    服务器                    Android
  │                        │                         │
  │─────── 轮询 ──────────>│                         │
  │ GET /api/chat/poll     │                         │
  │ ?lastTimestamp=0       │                         │
  │                        │                         │
  │<────── 返回 ───────────│                         │
  │ {messages: [...]}      │                         │
  │                        │                         │
  │────────────── 2秒延迟 ────────────              │
  │                        │                         │
  │─────── 轮询 ──────────>│                         │
  │ GET /api/chat/poll     │                         │
  │ ?lastTimestamp=xxx     │                         │
  │                        │                         │
  │                        │<──── 手机发送 ──────────│
  │                        │ sendChatMessage()       │
  │                        │                         │
  │<────── 返回 ───────────│                         │
  │ {messages: [新消息]}    │                         │
  │                        │                         │
  │──── 渲染新消息          │                         │
  │                        │                         │
```

## 技术栈总结

### Android端
- **UI框架**: Jetpack Compose
- **状态管理**: StateFlow / MutableStateFlow
- **并发**: Coroutines
- **依赖注入**: Hilt
- **HTTP服务器**: NanoHTTPD 2.3.1

### Web端
- **UI**: HTML5 + Tailwind CSS
- **逻辑**: Vanilla JavaScript (ES6+)
- **通信**: Fetch API
- **状态**: Plain JavaScript Objects

### 通信协议
- **传输**: HTTP/1.1
- **格式**: JSON
- **方式**: HTTP轮询 (Long Polling的简化版)

---

**说明**: 此架构采用HTTP轮询而非WebSocket，是为了：
1. 简化实现复杂度
2. 提高兼容性
3. 降低服务器资源占用
4. 便于调试和维护

对于聊天这种低频交互场景，2秒的延迟是可接受的。

