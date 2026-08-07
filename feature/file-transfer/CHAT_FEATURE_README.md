# 文件传输聊天功能实现说明

## 已实现的功能

### 1. 数据模型 (ChatMessage.kt)
- ✅ 创建了聊天消息数据模型
- ✅ 支持文本消息、文件消息和系统消息三种类型
- ✅ 包含发送者、时间戳、文件信息等字段

### 2. Android端界面 (FileTransferScreen.kt)
- ✅ 添加了浮动聊天按钮(FAB)，带未读消息角标
- ✅ 实现了聊天对话框UI
- ✅ 支持消息列表展示和滚动
- ✅ 添加了消息输入框和发送按钮
- ✅ 自动滚动到最新消息
- ✅ 区分手机端和浏览器端消息

### 3. Component逻辑 (FileTransferComponent.kt)
- ✅ 添加了聊天消息状态管理
- ✅ 添加了未读消息计数
- ✅ 实现了发送文本消息功能
- ✅ 实现了发送文件消息功能
- ✅ 添加了清空聊天记录功能

### 4. Web前端界面 (index.html + app.js)
- ✅ 添加了聊天按钮(带未读角标)
- ✅ 实现了侧边栏聊天面板
- ✅ 添加了消息列表和输入框
- ✅ 实现了消息发送和接收逻辑
- ✅ 添加了自动滚动功能

## 需要完成的功能

### WebSocket连接问题

当前使用的 NanoHTTPD 2.3.1 版本**不包含** WebSocket支持。需要以下两种方案之一：

#### 方案1: 升级到支持WebSocket的版本

在 `build.gradle.kts` 中将：
```kotlin
implementation("org.nanohttpd:nanohttpd:2.3.1")
implementation("org.nanohttpd:nanohttpd-websocket:2.3.1")
```

升级为：
```kotlin
implementation("org.nanohttpd:nanohttpd:2.3.1")  
implementation("org.nanohttpd:nanohttpd-websocket:2.3.1")
```

然后修改 `FileTransferServer.kt`:

```kotlin
// 将继承改为 NanoWSD
class FileTransferServer(
    private val context: Context,
    private var config: TransferConfig
) : NanoWSD(config.port) {
    
    // WebSocket处理保持不变
    inner class ChatWebSocket(handshake: IHTTPSession) : WebSocket(handshake) {
        // ... 现有代码
    }
    
    // 在serve方法中处理WebSocket升级
    override fun serve(session: IHTTPSession): Response {
        if (session.uri == "/api/chat") {
            return if (isWebSocketRequested(session)) {
                ChatWebSocket(session)
            } else {
                super.serve(session)
            }
        }
        // ... 其他逻辑
    }
}
```

#### 方案2: 使用HTTP轮询(简单但效率较低)

如果无法使用WebSocket，可以改用HTTP轮询：

1. 在 `FileTransferServer.kt` 中添加轮询端点：

```kotlin
// 添加消息队列
private val messageQueue = CopyOnWriteArrayList<ChatMessage>()

// 获取新消息(长轮询)
uri == "/api/chat/poll" -> handleChatPoll(session)

// 发送消息  
uri == "/api/chat/send" && method == Method.POST -> handleChatSend(session)

private fun handleChatPoll(session: IHTTPSession): Response {
    val params = session.parameters
    val lastMessageId = params["lastId"]?.firstOrNull()
    
    // 返回lastId之后的所有消息
    val newMessages = if (lastMessageId != null) {
        chatHistory.filter { it.timestamp.toString() > lastMessageId }
    } else {
        chatHistory.toList()
    }
    
    return jsonResponse(mapOf(
        "success" to true,
        "messages" to newMessages
    ))
}

private fun handleChatSend(session: IHTTPSession): Response {
    val files = mutableMapOf<String, String>()
    session.parseBody(files)
    
    val postData = files["postData"] ?: ""
    val message = gson.fromJson(postData, ChatMessage::class.java)
    
    // 保存消息
    chatHistory.add(message)
    
    // 通知Android端
    onChatMessageReceived?.invoke(message)
    
    return jsonResponse(mapOf("success" to true))
}
```

2. 修改 `app.js` 的 `connectChat()` 函数：

```javascript
function connectChat() {
    // 改为轮询
    pollChatMessages();
}

let lastMessageId = null;
let pollingTimeout = null;

async function pollChatMessages() {
    try {
        const url = lastMessageId 
            ? `/api/chat/poll?lastId=${lastMessageId}` 
            : '/api/chat/poll';
        
        const response = await fetch(url);
        const data = await response.json();
        
        if (data.success && data.messages.length > 0) {
            data.messages.forEach(handleChatMessage);
            lastMessageId = data.messages[data.messages.length - 1].timestamp;
        }
    } catch (error) {
        console.error('Poll chat error:', error);
    } finally {
        // 继续轮询
        pollingTimeout = setTimeout(pollChatMessages, 2000); // 每2秒轮询
    }
}

function sendChatMessage() {
    const input = elements.chatInput;
    const text = input?.value.trim();
    
    if (!text) return;
    
    const message = {
        id: generateId(),
        type: 'text',
        content: text,
        sender: 'browser',
        timestamp: Date.now()
    };
    
    // 发送到服务器
    fetch('/api/chat/send', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(message)
    });
    
    // 清空输入框
    input.value = '';
    
    // 本地添加消息
    state.chatMessages.push(message);
    renderChatMessages();
    scrollChatToBottom();
}
```

## 推荐方案

**推荐使用方案2 (HTTP轮询)**，因为：
1. 不需要额外的WebSocket依赖
2. 实现简单，容易调试
3. 对于聊天这种低频操作足够用
4. 兼容性更好

如果未来需要实时性更高的功能，可以再考虑升级到WebSocket。

## 测试步骤

1. 启动应用并开启文件传输服务
2. 在手机端点击聊天按钮
3. 在PC浏览器中打开传输地址
4. 点击浏览器中的聊天按钮
5. 尝试在两端发送消息
6. 验证消息能否正常收发

## 注意事项

1. 聊天消息目前存储在内存中，服务重启后会丢失
2. 如需持久化，可以考虑存储到数据库或文件
3. 文件消息功能需要额外实现文件上传和下载逻辑
4. 建议添加消息发送失败的重试机制
5. 可以添加消息时间戳格式化显示

