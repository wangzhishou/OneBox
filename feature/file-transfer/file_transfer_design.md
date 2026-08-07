# 文件传输模块设计方案 (feature:file_transfer)

## 📋 模块概述

### 功能目标
实现手机端与电脑端的文件快速传输，手机端作为文件服务器，PC端通过浏览器访问、浏览、下载、聊天和上传文件。

### 核心特性
- 🌐 内置HTTP服务器，支持局域网访问
- 📁 文件/图片浏览与预览
- ⬇️ 文件下载到PC端
- ⬆️ 文件上传到手机端
- 🔒 可选密码保护
- 📱 简洁美观的Material3界面

---

## 🏗️ 架构设计

### 模块结构
```
feature/file_transfer/
├── build.gradle.kts
├── src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/wanbaohe/file_transfer/
│   │   ├── screen/
│   │   │   └── FileTransferScreen.kt          # 主界面
│   │   ├── screenLogic/
│   │   │   └── FileTransferComponent.kt       # 业务逻辑组件
│   │   ├── server/
│   │   │   ├── FileTransferServer.kt          # HTTP服务器核心
│   │   │   ├── FileTransferService.kt         # 前台服务
│   │   │   └── handlers/
│   │   │       ├── FileListHandler.kt         # 文件列表API
│   │   │       ├── FileDownloadHandler.kt     # 文件下载API
│   │   │       ├── FileUploadHandler.kt       # 文件上传API
│   │   │       └── StaticResourceHandler.kt   # 静态资源服务
│   │   ├── model/
│   │   │   ├── FileItem.kt                    # 文件数据模型
│   │   │   ├── ServerState.kt                 # 服务器状态
│   │   │   └── TransferConfig.kt              # 配置项
│   │   ├── util/
│   │   │   ├── NetworkUtils.kt                # 网络工具类
│   │   │   ├── FileUtils.kt                   # 文件操作工具
│   │   │   └── QRCodeGenerator.kt             # 二维码生成
│   │   └── di/
│   │       └── FileTransferModule.kt          # Hilt依赖注入
│   ├── res/
│   │   ├── values/
│   │   │   └── strings.xml                    # 字符串资源
│   │   └── raw/
│   │       └── web/                           # Web前端资源
│   │           ├── index.html
│   │           ├── styles.css
│   │           └── app.js
│   └── assets/
│       └── web/                               # Web资源(可选)
```

---

## 📐 技术方案

### 1. HTTP服务器选型
使用 **NanoHTTPD** 或 **Ktor Server** 作为内嵌HTTP服务器：
- 轻量级，适合移动端
- 支持文件上传/下载
- 易于集成和扩展

推荐依赖：
```kotlin
// NanoHTTPD (轻量)
implementation("org.nanohttpd:nanohttpd:2.3.1")
// 或 Ktor Server (功能丰富)
implementation("io.ktor:ktor-server-core:2.3.x")
implementation("io.ktor:ktor-server-netty:2.3.x")
```

### 2. 前台服务
确保服务在后台稳定运行：
```kotlin
class FileTransferService : Service() {
    // 前台服务，保持HTTP服务器运行
    // 显示持久通知，显示访问地址
}
```

### 3. Web前端界面
简洁的响应式Web界面：
- HTML5 + CSS3 + JavaScript
- Material Design风格
- 支持文件拖拽上传
- 图片预览/缩略图
- 文件列表展示

---

## 🎨 界面设计

### Android端界面

#### 主界面元素
1. **服务状态卡片**
   - 服务开关（Switch）
   - 当前状态指示
   - 访问地址显示

2. **二维码区域**
   - 显示访问URL的二维码
   - 方便PC端扫码访问

3. **设置区域**
   - 端口设置（默认8080）
   - 密码保护开关
   - 根目录选择
   - 允许上传开关

4. **连接信息**
   - 当前连接数
   - 传输统计

### Web端界面

#### 页面结构
```
┌─────────────────────────────────────────┐
│  📱 文件传输 - 设备名称                    │
├─────────────────────────────────────────┤
│  📁 路径: /storage/emulated/0/          │
│  [返回上级] [上传文件] [刷新]             │
├─────────────────────────────────────────┤
│  🗂️ 文件夹                               │
│  ├── 📁 DCIM                            │
│  ├── 📁 Download                        │
│  └── 📁 Pictures                        │
│                                         │
│  📄 文件                                 │
│  ├── 📷 photo.jpg  (2.3MB)  [下载]      │
│  ├── 📄 doc.pdf    (1.1MB)  [下载]      │
│  └── 🎵 music.mp3  (4.5MB)  [下载]      │
├─────────────────────────────────────────┤
│  拖拽文件到此处上传                        │
└─────────────────────────────────────────┘
```

---

## 🔌 API设计

### RESTful API

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/` | Web主页 |
| GET | `/api/files?path=xxx` | 获取文件列表 |
| GET | `/api/download?path=xxx` | 下载文件 |
| POST | `/api/upload?path=xxx` | 上传文件 |
| GET | `/api/thumbnail?path=xxx` | 获取缩略图 |
| GET | `/api/info` | 获取设备信息 |

### 响应格式
```json
{
  "success": true,
  "data": {
    "files": [
      {
        "name": "photo.jpg",
        "path": "/storage/emulated/0/DCIM/photo.jpg",
        "size": 2365478,
        "isDirectory": false,
        "mimeType": "image/jpeg",
        "lastModified": 1702800000000
      }
    ],
    "currentPath": "/storage/emulated/0/DCIM",
    "canGoUp": true
  }
}
```

---

## 🔧 核心代码结构

### FileTransferComponent.kt
```kotlin
class FileTransferComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext) {

    // 服务器状态
    private val _serverState = MutableStateFlow<ServerState>(ServerState.Stopped)
    val serverState: StateFlow<ServerState> = _serverState

    // 配置
    private val _config = MutableStateFlow(TransferConfig())
    val config: StateFlow<TransferConfig> = _config

    // IP地址
    private val _ipAddress = MutableStateFlow<String?>(null)
    val ipAddress: StateFlow<String?> = _ipAddress

    fun startServer() { ... }
    fun stopServer() { ... }
    fun updateConfig(config: TransferConfig) { ... }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(componentContext: ComponentContext): FileTransferComponent
    }
}
```

### ServerState.kt
```kotlin
sealed class ServerState {
    object Stopped : ServerState()
    object Starting : ServerState()
    data class Running(val port: Int, val address: String) : ServerState()
    data class Error(val message: String) : ServerState()
}
```

### TransferConfig.kt
```kotlin
data class TransferConfig(
    val port: Int = 8080,
    val password: String? = null,
    val allowUpload: Boolean = true,
    val rootPath: String = Environment.getExternalStorageDirectory().absolutePath
)
```

---

## 📝 权限要求

```xml
<manifest>
    <!-- 网络权限 -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    
    <!-- 存储权限 -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />
    
    <!-- 前台服务 -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
    
    <!-- 服务声明 -->
    <service
        android:name=".server.FileTransferService"
        android:foregroundServiceType="dataSync"
        android:exported="false" />
</manifest>
```

---

## 🚀 实现步骤

### Phase 1: 基础框架 (Day 1)
1. 创建模块结构
2. 配置build.gradle.kts
3. 实现FileTransferComponent基础框架
4. 实现FileTransferScreen基础UI

### Phase 2: HTTP服务器 (Day 2-3)
1. 集成NanoHTTPD/Ktor
2. 实现文件列表API
3. 实现文件下载功能
4. 实现文件上传功能

### Phase 3: 前台服务 (Day 3)
1. 实现FileTransferService
2. 服务通知管理
3. 生命周期处理

### Phase 4: Web前端 (Day 4)
1. 设计HTML页面结构
2. 实现CSS样式
3. JavaScript交互逻辑
4. 文件拖拽上传

### Phase 5: 优化完善 (Day 5)
1. 二维码生成
2. 密码保护
3. 错误处理
4. UI优化与测试

---

## 📋 字符串资源 (strings.xml)

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="file_transfer_title">文件传输</string>
    <string name="server_status_stopped">服务已停止</string>
    <string name="server_status_running">服务运行中</string>
    <string name="server_status_starting">正在启动...</string>
    <string name="start_server">启动服务</string>
    <string name="stop_server">停止服务</string>
    <string name="access_address">访问地址</string>
    <string name="scan_qr_code">扫描二维码访问</string>
    <string name="port_setting">端口设置</string>
    <string name="password_protection">密码保护</string>
    <string name="allow_upload">允许上传</string>
    <string name="root_directory">根目录</string>
    <string name="connected_clients">已连接客户端</string>
    <string name="transfer_statistics">传输统计</string>
    <string name="copy_address">复制地址</string>
    <string name="address_copied">地址已复制</string>
    <string name="server_error">服务器错误</string>
    <string name="network_unavailable">网络不可用</string>
    <string name="storage_permission_required">需要存储权限</string>
</resources>
```

---

## 🔄 更新记录 (2025-12-17)

### 1. 路径导航修复
- 修复了 `FileUtils.validatePath` 无法正确处理绝对路径的问题。
- 现在支持前端传递绝对路径，解决了子目录访问报错 "目录不存在" 的问题。

### 2. 配置状态同步
- 修复了服务启动后 "允许上传" 开关状态重置的问题。
- `FileTransferService` 现在公开 `currentConfig` 属性。
- `FileTransferComponent` 在绑定服务时会同步服务端的当前配置。

### 3. 权限申请优化
- 优化了权限申请流程，不再进入页面立即申请。
- 点击 "启动服务" 时检查权限，如果未授权则显示友好提示对话框。
- 用户确认后才发起系统权限请求。

### 4. Web界面重构
- 重构了Web前端，采用SPA（单页应用）架构。
- 移除了独立的 `login.html`，改为 `index.html` 内置登录遮罩层。
- 实现了左右分栏布局（大屏设备）：左侧显示设备信息和导航，右侧显示文件列表。
- 优化了文件图标显示、面包屑导航和上传交互。
- `FileTransferServer` 更新了路由逻辑，支持SPA模式的静态资源访问和API鉴权。

### 5. 兼容性修复
- 修复了 `FileTransferService` 中 `stopForeground` API 兼容性问题。

### 6. SPA模式路由修复 (2025-12-17)
- 修复了 `FileTransferServer` 中 `serveLoginPage()` 函数未定义导致的编译错误。
- 由于Web界面已重构为SPA模式（登录遮罩层内置于index.html），移除了独立登录页面的路由逻辑。
- 更新了密码验证策略：
  - 主页、静态资源和认证接口允许无条件访问。
  - 其他API接口在未认证时返回JSON响应 `{"success": false, "requiresAuth": true, "message": "需要登录"}`。
  - 前端通过检测 `requiresAuth` 字段来显示登录遮罩层。

---

# AI编程提示词

## 提示词1: 创建模块基础结构

```
请帮我在Android项目中创建一个新的feature模块 `feature:file_transfer`，用于实现手机端文件传输服务功能。

技术要求：
- 开发语言：Kotlin
- UI框架：Jetpack Compose + Material3
- 架构：基于Decompose的组件化架构
- 依赖注入：Hilt

参考已有模块结构：
- 参考 feature:demo 模块的 build.gradle.kts 配置
- 参考 DemoComponent.kt 和 DemoScreen.kt 的代码结构

需要创建的文件：
1. feature/file_transfer/build.gradle.kts
2. feature/file_transfer/src/main/AndroidManifest.xml
3. feature/file_transfer/src/main/java/com/wanbaohe/file_transfer/screenLogic/FileTransferComponent.kt
4. feature/file_transfer/src/main/java/com/wanbaohe/file_transfer/screen/FileTransferScreen.kt
5. feature/file_transfer/src/main/res/values/strings.xml

FileTransferComponent应该包含：
- 服务器状态管理 (ServerState: Stopped, Starting, Running, Error)
- 配置管理 (端口、密码、根目录、是否允许上传)
- IP地址获取
- 启动/停止服务方法
- 使用AssistedInject注入

FileTransferScreen应该包含：
- 服务开关
- 访问地址显示
- 二维码显示区域
- 设置项（端口、密码、根目录）
- 使用BaseScreen作为基础布局
```

## 提示词2: 实现HTTP服务器

```
请帮我在 feature:file_transfer 模块中实现基于NanoHTTPD的HTTP文件服务器。

需要创建的文件：
1. server/FileTransferServer.kt - HTTP服务器核心类
2. server/handlers/FileListHandler.kt - 处理文件列表请求
3. server/handlers/FileDownloadHandler.kt - 处理文件下载
4. server/handlers/FileUploadHandler.kt - 处理文件上传
5. model/FileItem.kt - 文件数据模型

FileTransferServer功能：
- 继承NanoHTTPD
- 支持自定义端口
- 可选密码认证
- 路由分发到各个Handler

API设计：
- GET /api/files?path=xxx - 返回JSON格式的文件列表
- GET /api/download?path=xxx - 下载文件
- POST /api/upload?path=xxx - multipart/form-data上传文件
- GET / - 返回Web界面HTML

FileItem数据结构：
- name: String
- path: String  
- size: Long
- isDirectory: Boolean
- mimeType: String?
- lastModified: Long

请使用Kotlin协程处理异步操作，并添加适当的错误处理。
```

## 提示词3: 实现前台服务

```
请帮我实现 FileTransferService 前台服务，确保HTTP服务器在后台稳定运行。

需要创建的文件：
1. server/FileTransferService.kt

功能要求：
- 继承Service，实现前台服务
- 显示持久通知，包含访问地址和停止按钮
- 管理FileTransferServer的生命周期
- 使用Binder与Activity通信
- 处理服务停止时的资源清理

AndroidManifest.xml需要添加：
- FOREGROUND_SERVICE权限
- FOREGROUND_SERVICE_DATA_SYNC权限
- Service声明，foregroundServiceType="dataSync"

通知内容：
- 标题：文件传输服务运行中
- 内容：访问地址 http://192.168.x.x:8080
- 操作按钮：停止服务
```

## 提示词4: 实现Web前端界面

```
请帮我创建嵌入到Android应用中的Web前端界面，用于PC端浏览器访问。

需要创建的文件：
1. src/main/res/raw/index.html - 主页面
2. src/main/res/raw/styles.css - 样式
3. src/main/res/raw/app.js - 交互逻辑

设计要求：
- 响应式设计，适配桌面和移动端浏览器
- Material Design风格
- 简洁美观的界面

功能需求：
1. 文件列表展示
   - 文件夹和文件分类显示
   - 显示文件名、大小、修改时间
   - 文件图标根据类型显示

2. 导航功能
   - 路径面包屑
   - 返回上级目录
   - 点击文件夹进入

3. 文件操作
   - 文件下载按钮
   - 图片缩略图预览
   - 拖拽上传支持
   - 上传进度显示

4. 样式
   - 浅色/深色主题切换
   - 列表/网格视图切换
   - 加载动画
```

## 提示词5: 工具类和优化

```
请帮我实现 feature:file_transfer 模块的工具类和优化功能。

需要创建的文件：
1. util/NetworkUtils.kt - 网络工具类
2. util/FileUtils.kt - 文件操作工具
3. util/QRCodeGenerator.kt - 二维码生成

NetworkUtils功能：
- 获取设备IP地址（WiFi优先）
- 检查网络是否可用
- 获取网络类型

FileUtils功能：
- 获取文件MIME类型
- 格式化文件大小
- 检查文件访问权限
- 安全路径验证（防止路径穿越攻击）

QRCodeGenerator功能：
- 使用ZXing库生成二维码
- 返回Bitmap供Compose显示
- 支持自定义尺寸

UI优化：
- 添加权限请求处理
- 网络状态监听和提示
- 服务状态实时更新
- 复制地址到剪贴板功能
```

## 提示词6: 集成到主应用

```
请帮我将 feature:file_transfer 模块集成到主应用中。

需要修改的文件：
1. settings.gradle.kts - 添加模块引用
2. app/build.gradle.kts - 添加模块依赖
3. 主应用的导航配置 - 添加FileTransfer路由

添加到settings.gradle.kts：
include(":feature:file_transfer")

添加到导航系统：
- 在Screen密封类中添加FileTransfer屏幕
- 在导航图中注册FileTransferScreen
- 在主页或功能列表中添加入口

入口设计：
- 图标：Folder或Share图标
- 标题：文件传输
- 描述：通过浏览器访问手机文件
```

---

## 🎯 验收标准

### 功能验收
- [ ] 服务能正常启动和停止
- [ ] PC浏览器能访问手机端
- [ ] 文件列表正确显示
- [ ] 文件下载功能正常
- [ ] 文件上传功能正常
- [ ] 密码保护生效
- [ ] 二维码正确生成

### 性能验收
- [ ] 大文件传输稳定
- [ ] 多文件上传支持
- [ ] 图片缩略图快速加载
- [ ] 服务后台运行稳定

### UI验收
- [ ] Material3风格一致
- [ ] 响应式布局适配
- [ ] 加载状态显示
- [ ] 错误提示友好

---

## 📚 参考资源

- [NanoHTTPD GitHub](https://github.com/NanoHttpd/nanohttpd)
- [Ktor Server](https://ktor.io/docs/server-create-a-new-project.html)
- [Material3 Design](https://m3.material.io/)
- [Decompose](https://arkivanov.github.io/Decompose/)
- [ZXing](https://github.com/zxing/zxing)

---

## ✅ 已实现文件清单

以下文件已创建完成：

### 模块配置
- [x] `feature/file-transfer/build.gradle.kts` - 模块构建配置
- [x] `feature/file-transfer/src/main/AndroidManifest.xml` - 清单文件
- [x] `feature/file-transfer/proguard-rules.pro` - ProGuard规则
- [x] `feature/file-transfer/consumer-rules.pro` - Consumer规则
- [x] `feature/file-transfer/.gitignore` - Git忽略文件

### 数据模型
- [x] `model/ServerState.kt` - 服务器状态密封类
- [x] `model/TransferConfig.kt` - 传输配置数据类
- [x] `model/FileItem.kt` - 文件项及响应数据类

### 工具类
- [x] `util/NetworkUtils.kt` - 网络工具（IP获取、网络状态检查）
- [x] `util/FileUtils.kt` - 文件工具（MIME类型、路径验证、文件列表）
- [x] `util/QRCodeGenerator.kt` - 二维码生成工具

### HTTP服务器
- [x] `server/FileTransferServer.kt` - NanoHTTPD HTTP服务器（包含完整Web前端）
- [x] `server/FileTransferService.kt` - 前台服务

### 业务逻辑
- [x] `screenLogic/FileTransferComponent.kt` - Decompose组件

### UI界面
- [x] `screen/FileTransferScreen.kt` - Compose主界面

### 资源文件
- [x] `res/values/strings.xml` - 字符串资源

### 项目配置
- [x] `settings.gradle.kts` - 已添加模块引用
