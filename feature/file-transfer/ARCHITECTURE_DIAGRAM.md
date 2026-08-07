# WebSocket 聊天架构图

## 系统架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                          Android 设备（服务器端）                      │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │                    FileTransferServer                        │   │
│  │                      (NanoWSD)                              │   │
│  │                                                              │   │
│  │  ┌────────────────┐         ┌─────────────────────┐        │   │
│  │  │  openWebSocket │         │    serveHttp       │        │   │
│  │  │   /ws/chat     │         │   HTTP Requests    │        │   │
│  │  └────────┬───────┘         └──────────┬─────────┘        │   │
│  │           │                             │                   │   │
│  │           ▼                             ▼                   │   │
│  │  ┌────────────────┐         ┌─────────────────────┐        │   │
│  │  │ ChatWebSocket  │         │   File APIs         │        │   │
│  │  │   Instances    │         │  /api/files         │        │   │
│  │  │                │         │  /api/upload        │        │   │
│  │  │  • onOpen()    │         │  /api/download      │        │   │
│  │  │  • onMessage() │         │  /api/chat/history  │        │   │
│  │  │  • broadcast() │         └─────────────────────┘        │   │
│  │  │  • onClose()   │                                         │   │
│  │  └────────┬───────┘                                         │   │
│  │           │                                                  │   │
│  │           ▼                                                  │   │
│  │  ┌────────────────┐                                         │   │
│  │  │  Chat History  │                                         │   │
│  │  │  (CopyOnWrite  │                                         │   │
│  │  │   ArrayList)   │                                         │   │
│  │  └────────────────┘                                         │   │
│  │                                                              │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │              FileTransferComponent (ViewModel)               │   │
│  │  • sendMessage()                                             │   │
│  │  • chatMessages: StateFlow<List<ChatMessage>>               │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │              FileTransferScreen (Compose UI)                 │   │
│  │  • 聊天消息列表                                               │   │
│  │  • 消息输入框                                                 │   │
│  │  • 发送按钮                                                   │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                       │
└───────────────────────────────────────────────────────────────────────┘
                              ▲
                              │ WebSocket (ws://ip:port/ws/chat)
                              │ HTTP (http://ip:port/api/*)
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     浏览器客户端（多个）                              │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │                      Web Application                         │   │
│  │                                                              │   │
│  │  ┌────────────────┐         ┌─────────────────────┐        │   │
│  │  │  WebSocket     │         │   File Manager      │        │   │
│  │  │  Connection    │         │   UI                │        │   │
│  │  │                │         │                     │        │   │
│  │  │  • onopen      │         │  • 文件列表          │        │   │
│  │  │  • onmessage   │         │  • 上传/下载         │        │   │
│  │  │  • onerror     │         │  • 预览             │        │   │
│  │  │  • onclose     │         │  • 操作             │        │   │
│  │  │  • send()      │         └─────────────────────┘        │   │
│  │  └────────┬───────┘                                         │   │
│  │           │                                                  │   │
│  │           ▼                                                  │   │
│  │  ┌────────────────┐                                         │   │
│  │  │  Chat Panel    │                                         │   │
│  │  │                │                                         │   │
│  │  │  • 消息列表     │                                         │   │
│  │  │  • 文本输入     │                                         │   │
│  │  │  • 文件上传     │                                         │   │
│  │  │  • 消息渲染     │                                         │   │
│  │  └────────────────┘                                         │   │
│  │                                                              │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                       │
└───────────────────────────────────────────────────────────────────────┘
```

## 消息流程

### 1. 文本消息流程

```
浏览器 A                  服务器                   浏览器 B
   │                       │                        │
   │  1. 输入文本消息       │                        │
   │  "你好"               │                        │
   │                       │                        │
   │  2. WebSocket.send() │                        │
   ├──────────────────────>│                        │
   │  {                    │                        │
   │    type: "text",      │  3. onMessage()        │
   │    content: "你好"    │     ↓                  │
   │  }                    │  4. chatHistory.add()  │
   │                       │     ↓                  │
   │                       │  5. broadcast()        │
   │                       ├────────────────────────>│
   │                       │                        │  6. onmessage
   │                       │                        │     ↓
   │  7. onmessage         │                        │  7. 渲染消息
   │<──────────────────────┤                        │
   │  显示: "你好"         │                        │  显示: "你好"
   │                       │                        │
```

### 2. 文件消息流程

```
浏览器                    服务器                   其他浏览器
   │                       │                        │
   │  1. 选择文件          │                        │
   │  example.pdf (2MB)    │                        │
   │                       │                        │
   │  2. 创建上传中消息     │                        │
   │  {                    │                        │
   │    type: "file",      │                        │
   │    status: "uploading"│                        │
   │  }                    │                        │
   │  ↓ 渲染上传中状态      │                        │
   │                       │                        │
   │  3. HTTP POST /api/upload                      │
   ├──────────────────────>│                        │
   │  FormData: file       │  4. 保存文件           │
   │                       │     ↓                  │
   │  5. Response          │  返回文件路径           │
   │<──────────────────────┤                        │
   │  {                    │                        │
   │    success: true,     │                        │
   │    filePath: "..."    │                        │
   │  }                    │                        │
   │                       │                        │
   │  6. 更新消息状态       │                        │
   │  status: "completed"  │                        │
   │  ↓ 渲染下载按钮        │                        │
   │                       │                        │
   │  7. WebSocket.send() │                        │
   ├──────────────────────>│                        │
   │  {                    │  8. broadcast()        │
   │    type: "file",      ├────────────────────────>│
   │    status: "completed"│                        │  9. 渲染文件消息
   │    filePath: "..."    │                        │     + 下载按钮
   │  }                    │                        │
   │                       │                        │
```

## 连接状态机

```
         ┌────────────┐
         │  CLOSED    │ 初始状态
         └──────┬─────┘
                │
                │ connectChat()
                ▼
         ┌────────────┐
    ┌───>│ CONNECTING │
    │    └──────┬─────┘
    │           │
    │           │ onopen()
    │           ▼
    │    ┌────────────┐
    │    │   OPEN     │◄───┐ 正常通信
    │    └──────┬─────┘    │
    │           │           │ 消息收发
    │           │ onerror() │
    │           │ onclose() │
    │           ▼           │
    │    ┌────────────┐    │
    │    │  CLOSING   │────┘
    │    └──────┬─────┘
    │           │
    │           ▼
    │    ┌────────────┐
    │    │   CLOSED   │
    │    └──────┬─────┘
    │           │
    │           │ 5秒后自动重连
    └───────────┘
```

## 数据结构

### ChatMessage (消息对象)

```kotlin
data class ChatMessage(
    val id: String,              // 唯一ID
    val type: String,            // "text" | "file" | "system"
    val content: String?,        // 文本内容（type=text时）
    val fileName: String?,       // 文件名（type=file时）
    val filePath: String?,       // 文件路径（type=file时）
    val fileSize: Long?,         // 文件大小（type=file时）
    val status: String?,         // "uploading" | "completed" | "failed"
    val error: String?,          // 错误信息
    val sender: String,          // "browser" | "mobile"
    val timestamp: Long          // 时间戳
)
```

### WebSocket 帧格式

```
Client -> Server:
{
  "id": "lwz3k4p2jh0.abc123",
  "type": "text",
  "content": "Hello",
  "sender": "browser",
  "timestamp": 1702886400000
}

Server -> Client:
{
  "id": "lwz3k4p2jh0.abc123",
  "type": "text",
  "content": "Hello",
  "sender": "browser",
  "timestamp": 1702886400000
}

Server -> Client (System):
{
  "type": "system",
  "event": "connected",
  "timestamp": 1702886400000
}
```

## 并发处理

```
多个浏览器同时连接:

Browser 1 ──┐
Browser 2 ──┤
Browser 3 ──┼──> FileTransferServer
Browser 4 ──┤       │
Browser 5 ──┘       │
                    ▼
            ┌──────────────────┐
            │ ChatWebSocket    │
            │ activeConnections│
            │                  │
            │ [WS1, WS2, WS3, │
            │  WS4, WS5]       │
            └────────┬─────────┘
                     │
                     │ broadcast()
                     ▼
         ┌───────────┴───────────┐
         │                       │
    WS1  WS2  WS3  WS4  WS5
     │    │    │    │    │
     ▼    ▼    ▼    ▼    ▼
    B1   B2   B3   B4   B5
```

## 性能优化点

### 1. 连接池管理
```
CopyOnWriteArrayList<ChatWebSocket>
• 线程安全的连接列表
• 读多写少的场景
• 遍历时不需要加锁
```

### 2. 消息广播优化
```
for each connection in activeConnections:
    try:
        connection.send(message)
    catch IOException:
        remove connection from pool
```

### 3. 自动重连机制
```
指数退避策略:
第1次: 5秒后重连
第2次: 10秒后重连
第3次: 20秒后重连
...
最多: 5次重连尝试
```

## 安全考虑

```
┌─────────────────┐
│  Browser Client │
└────────┬────────┘
         │
         │ 1. HTTP 认证
         ▼
┌─────────────────┐
│  Session Auth   │ Cookie: session_id
└────────┬────────┘
         │
         │ 2. WebSocket Upgrade
         ▼
┌─────────────────┐
│  WebSocket      │ 继承 HTTP Session
└────────┬────────┘
         │
         │ 3. 消息验证
         ▼
┌─────────────────┐
│  Server Logic   │ • 输入验证
└─────────────────┘ • XSS 防护
                    • 路径验证
```

---

**架构版本**: v1.0.0  
**更新日期**: 2025-12-18

