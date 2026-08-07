# 文件传输功能测试指南

## 快速测试步骤

### 1. 启动服务器
1. 打开 Wanbaohe 应用
2. 进入"文件传输"功能
3. 授予存储权限（如果提示）
4. 点击"启动服务器"按钮
5. 记下显示的 IP 地址（例如：http://192.168.1.100:8080）

### 2. 浏览器访问
1. 在电脑上打开浏览器
2. 输入手机显示的 IP 地址
3. 如果设置了密码，输入密码登录

### 3. 查看调试信息

#### 手机端 (Android Studio)
```bash
# 查看服务器日志
adb logcat | grep FileTransferServer

# 应该看到:
# handleFileList - requestPath: 
# handleFileList - rootPath: /storage/emulated/0
# handleFileList - safePath: ...
# handleFileList - files count: XX
```

#### 浏览器端 (F12 控制台)
```
按 F12 打开开发者工具 -> Console 标签

应该看到:
✓ checkAuth - starting...
✓ checkAuth - response status: 200
✓ checkAuth - response data: {success: true, files: [...], ...}
✓ handleFilesResponse - files count: XX
✓ renderFiles - called with XX files
✓ renderFiles - rendering complete

如果有错误，会显示红色错误信息
```

## 常见错误及解决方案

### 错误 1: Network error / 无法连接
**原因**: 
- 手机和电脑不在同一 WiFi
- 防火墙阻止
- 服务器未启动

**解决**:
```bash
# 检查网络连接
ping <手机IP地址>

# 检查服务器是否运行
adb logcat | grep "Server started"
```

### 错误 2: 文件列表为空
**检查 Logcat**:
```bash
adb logcat | grep "handleFileList"
```

**可能原因**:
- `rootPath: /storage/emulated/0` - 根路径
- `files count: 0` - 如果为 0，可能是:
  - 没有存储权限
  - 目录确实为空
  - 隐藏了隐藏文件

**解决**:
1. 确认存储权限已授予
2. 在设置中开启"显示隐藏文件"
3. 换一个有文件的目录

### 错误 3: CORS 错误
**浏览器控制台显示**:
```
Access to fetch at 'http://192.168.1.100:8080/api/files' 
from origin 'http://192.168.1.100:8080' has been blocked by CORS policy
```

**解决**: 
- 清除浏览器缓存
- 硬刷新页面（Ctrl+Shift+R）
- 确保使用修复后的代码

### 错误 4: JavaScript 错误
**浏览器控制台显示**:
```
Uncaught TypeError: Cannot read properties of undefined
```

**解决**:
1. 清除浏览器缓存
2. 检查是否有网络问题导致 JS 文件加载失败
3. 查看 Network 标签，确认 app.js 加载成功

## 功能测试清单

### 基础功能
- [ ] 页面正常加载
- [ ] 显示文件列表
- [ ] 显示文件夹图标（黄色）
- [ ] 显示文件图标（根据类型）
- [ ] 显示文件大小
- [ ] 显示修改时间

### 导航功能
- [ ] 点击文件夹进入
- [ ] 面包屑导航显示正确
- [ ] 返回按钮可用
- [ ] 点击面包屑跳转

### 视图功能
- [ ] 网格视图显示正常
- [ ] 列表视图显示正常
- [ ] 视图切换正常

### 文件操作
- [ ] 点击文件下载
- [ ] 悬停显示操作按钮
- [ ] 右键菜单显示

### 上传功能（需要启用）
- [ ] 点击上传按钮打开对话框
- [ ] 选择文件上传
- [ ] 显示上传进度
- [ ] 拖拽文件上传
- [ ] 创建文件夹

### 搜索和排序
- [ ] 搜索文件名
- [ ] 按名称排序
- [ ] 按大小排序
- [ ] 按日期排序
- [ ] 升序/降序切换

### 多选功能
- [ ] 点击选择框
- [ ] 全选（Ctrl+A）
- [ ] 批量下载
- [ ] 取消选择

### 响应式设计
- [ ] 手机端显示正常
- [ ] 平板显示正常
- [ ] 桌面端显示正常
- [ ] 深色模式切换

## 性能测试

### 大文件夹测试
1. 测试包含 100+ 文件的文件夹
2. 检查加载时间
3. 检查滚动流畅度

### 大文件测试
1. 上传 100MB+ 文件
2. 检查上传进度
3. 下载大文件测试

### 并发测试
1. 多个浏览器同时连接
2. 同时上传多个文件
3. 检查服务器稳定性

## 调试技巧

### 1. 使用 Chrome DevTools
```
F12 -> Network 标签
- 查看所有 API 请求
- 检查请求/响应内容
- 查看请求耗时
```

### 2. 查看 API 响应
```
Network -> 点击请求 -> Response 标签

正常响应示例:
{
  "success": true,
  "files": [
    {
      "name": "test.txt",
      "path": "/storage/emulated/0/test.txt",
      "size": 1024,
      "isDirectory": false,
      "mimeType": "text/plain",
      "lastModified": 1702800000000
    }
  ],
  "currentPath": "",
  "canGoUp": false,
  "parentPath": null
}
```

### 3. 使用 ADB 实时查看日志
```bash
# 只看文件传输相关日志
adb logcat -s FileTransferServer

# 查看所有错误
adb logcat *:E

# 清除日志后重新测试
adb logcat -c
adb logcat | grep FileTransferServer
```

### 4. 检查服务器状态
```bash
# 检查端口是否开放
adb shell netstat -an | grep 8080

# 应该看到:
# tcp6  0  0 :::8080  :::*  LISTEN
```

## 报告问题

如果发现问题，请提供以下信息:

1. **Android 版本**: 
2. **浏览器及版本**: 
3. **错误描述**: 
4. **Logcat 输出**:
   ```
   粘贴相关日志
   ```
5. **浏览器控制台输出**:
   ```
   粘贴错误信息
   ```
6. **重现步骤**:
   1. 
   2. 
   3. 

7. **截图**（如适用）

## 参考资料

- [BUGFIXES.md](BUGFIXES.md) - 详细的 bug 修复说明
- [ENHANCEMENTS.md](ENHANCEMENTS.md) - 功能增强计划
- [NanoHTTPD 文档](https://github.com/NanoHttpd/nanohttpd)

