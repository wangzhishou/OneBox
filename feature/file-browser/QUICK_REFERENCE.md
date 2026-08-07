# File Browser Module - Quick Reference

## 快速开始

### 1. 基础使用

```kotlin
// 注入 Component Factory
@Inject lateinit var fileBrowserComponentFactory: FileBrowserComponent.Factory

// 创建 Component
val component = fileBrowserComponentFactory(
    componentContext = componentContext,
    initialUri = null  // null = 默认目录
)

// 显示界面
FileBrowserScreen(
    component = component,
    onGoBack = { navController.popBackStack() }
)
```

### 2. 导航到指定文件

```kotlin
val fileUri = Uri.parse("content://...")

val component = fileBrowserComponentFactory(
    componentContext = componentContext,
    initialUri = fileUri  // 自动导航并高亮
)
```

### 3. 配置文件点击处理

```kotlin
// 在 Application 初始化时配置
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 自定义文件点击处理
        FileBrowserConfig.fileClickHandler = { context, fileItem ->
            when {
                fileItem.mimeType?.startsWith("image/") == true -> {
                    // 打开图片查看器
                    openImageViewer(context, fileItem.uri)
                }
                fileItem.mimeType?.startsWith("video/") == true -> {
                    // 打开视频播放器
                    openVideoPlayer(context, fileItem.uri)
                }
                else -> {
                    // 使用默认应用（系统打开）
                    // FileBrowserConfig 已经提供了默认实现
                }
            }
        }
    }
}
```

## 主要类说明

### FileItem
```kotlin
data class FileItem(
    val uri: Uri,                  // 文件 URI
    val name: String,              // 文件名
    val isDirectory: Boolean,      // 是否是目录
    val size: Long,                // 文件大小
    val lastModified: Date,        // 修改时间
    val mimeType: String?,         // MIME 类型
    val path: String,              // 路径
    val isHighlighted: Boolean     // 是否高亮
)
```

### SortConfig
```kotlin
data class SortConfig(
    val type: SortType,      // NAME, DATE, SIZE, TYPE
    val order: SortOrder     // ASCENDING, DESCENDING
)
```

### FileBrowserState
```kotlin
sealed interface FileBrowserState {
    data object Idle          // 初始
    data object Loading       // 加载中
    data class Success(...)   // 成功
    data class Empty(...)     // 空目录
    data class Error(...)     // 错误
    data object NoPermission  // 无权限
}
```

## Component API

### 状态
```kotlin
val state: StateFlow<FileBrowserState>
val currentUri: StateFlow<Uri?>
val sortConfig: StateFlow<SortConfig>
```

### 方法
```kotlin
fun loadDirectory(uri: Uri?)       // 加载目录
fun navigateUp()                   // 返回上级
fun navigateBack(): Boolean        // 导航返回
fun onItemClick(item: FileItem)    // 处理点击
fun changeSortType(type: SortType) // 改变排序
fun refresh()                      // 刷新
fun canNavigateBack(): Boolean     // 是否可返回
fun getCurrentPath(): String       // 当前路径
```

## 权限

### AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

### 运行时权限请求
```kotlin
// Android 6.0+
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    requestPermissions(
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
        REQUEST_CODE
    )
}
```

## UI 组件

### FileListItem
```kotlin
@Composable
fun FileListItem(
    item: FileItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

### FileList
```kotlin
@Composable
fun FileList(
    files: List<FileItem>,
    onItemClick: (FileItem) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
)
```

### SortMenu
```kotlin
@Composable
fun SortMenu(
    sortConfig: SortConfig,
    onSortTypeChange: (SortType) -> Unit,
    modifier: Modifier = Modifier
)
```

## 工具类

### FileHelper
```kotlin
class FileHelper @Inject constructor() {
    fun getDefaultDirectory(): File
    suspend fun loadFilesFromUri(uri: Uri?): Result<List<FileItem>>
    fun getParentUri(uri: Uri?): Uri?
    suspend fun findFileDirectory(targetUri: Uri): Result<Pair<Uri, String>>
    fun getDisplayPath(uri: Uri?): String
}
```

### FileSorter
```kotlin
object FileSorter {
    fun sort(files: List<FileItem>, config: SortConfig): List<FileItem>
}
```

## 字符串资源

在 `res/values/strings.xml` 中定义的字符串：

- `file_browser_title` - 标题
- `sort_by_name` - 按名称排序
- `sort_by_date` - 按日期排序
- `sort_by_size` - 按大小排序
- `sort_by_type` - 按类型排序
- `empty_folder_title` - 空文件夹标题
- `error_loading_title` - 错误标题
- 等等...

## 依赖关系

```kotlin
implementation(projects.core.ui)
implementation(projects.core.theme)
implementation(projects.core.base)
implementation(projects.feature.common)
implementation(libs.dagger.hilt.android)
implementation(libs.coil.compose)
```

## 性能提示

1. ✅ 使用 LazyColumn，支持大量文件
2. ✅ 所有 I/O 在后台线程
3. ✅ 状态管理使用 Flow
4. ✅ 列表项使用正确的 key
5. ✅ 自动回收视图

## 常见问题

### Q: 如何处理权限被拒绝？
A: Component 会发出 `NoPermission` 状态，显示相应 UI。

### Q: 支持哪些 URI 格式？
A: 支持 `file://` 和 `content://` 两种格式。

### Q: 如何自定义文件图标？
A: 修改 `FileListItem.kt` 中的 `FileIcon` composable。

### Q: 如何添加文件选择功能？
A: 在 Component 中添加选择状态，UI 中添加选择框。

### Q: 如何支持缩略图？
A: 集成 Coil，在 FileListItem 中加载图片缩略图。

## 调试

### 启用日志
```kotlin
// 在 Component 中添加日志
private val logger = Logger("FileBrowser")

logger.d("Loading directory: $uri")
logger.e("Error loading files", throwable)
```

### 查看状态
```kotlin
component.state.collectAsState().value.let { state ->
    Log.d("FileBrowser", "Current state: $state")
}
```

## 完整示例

详见 `INTEGRATION_EXAMPLES.kt` 文件，包含：
- 基础用法
- 导航到文件
- 文件类型处理
- Decompose 集成
- 权限请求

