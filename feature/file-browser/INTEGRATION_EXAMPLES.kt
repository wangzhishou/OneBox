package com.wanbaohe.file.browser

/**
 * Example integration code for the File Browser module
 *
 * This file demonstrates how to integrate and use the FileBrowser component
 * in your application.
 */

// Example 1: Basic usage - Start from default directory
/*
@Composable
fun MyScreen(
    componentContext: ComponentContext,
    fileBrowserComponentFactory: FileBrowserComponent.Factory
) {
    val component = remember {
        fileBrowserComponentFactory(
            componentContext = componentContext,
            initialUri = null  // null = start from default directory
        )
    }

    FileBrowserScreen(
        component = component,
        onGoBack = { /* Navigate back to previous screen */ }
    )
}
*/

// Example 2: Navigate to specific file
/*
@Composable
fun NavigateToFile(
    componentContext: ComponentContext,
    fileBrowserComponentFactory: FileBrowserComponent.Factory,
    targetFileUri: Uri
) {
    val component = remember(targetFileUri) {
        fileBrowserComponentFactory(
            componentContext = componentContext,
            initialUri = targetFileUri  // Navigate to this file
        )
    }

    FileBrowserScreen(
        component = component,
        onGoBack = { /* Navigate back */ }
    )
}
*/

// Example 3: Custom file click handler
/*
// 在应用初始化时配置文件点击处理
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 配置自定义的文件点击处理
        FileBrowserConfig.fileClickHandler = { context, fileItem ->
            when {
                fileItem.mimeType?.startsWith("image/") == true -> {
                    // 打开自定义图片查看器
                    openImageViewer(context, fileItem.uri)
                }
                fileItem.mimeType?.startsWith("video/") == true -> {
                    // 打开自定义视频播放器
                    openVideoPlayer(context, fileItem.uri)
                }
                fileItem.mimeType?.startsWith("audio/") == true -> {
                    // 打开自定义音频播放器
                    openAudioPlayer(context, fileItem.uri)
                }
                fileItem.mimeType == "application/pdf" -> {
                    // 打开 PDF 查看器
                    openPdfViewer(context, fileItem.uri)
                }
                else -> {
                    // 使用默认应用打开
                    openWithDefaultApp(context, fileItem)
                }
            }
        }
    }
}

fun openImageViewer(context: Context, uri: Uri) {
    // Implementation
}

fun openVideoPlayer(context: Context, uri: Uri) {
    // Implementation
}

fun openAudioPlayer(context: Context, uri: Uri) {
    // Implementation
}

fun openPdfViewer(context: Context, uri: Uri) {
    // Implementation
}

fun openWithDefaultApp(context: Context, fileItem: FileItem) {
    // Use FileBrowserConfig default implementation
    FileBrowserConfig.fileClickHandler(context, fileItem)
}
*/

// Example 4: Integration with Decompose navigation
/*
sealed class RootChild {
    data class FileBrowser(val component: FileBrowserComponent) : RootChild()
    // ... other children
}

class RootComponent(
    componentContext: ComponentContext,
    private val fileBrowserComponentFactory: FileBrowserComponent.Factory
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    val childStack = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Home,
        handleBackButton = true,
        childFactory = ::createChild
    )

    private fun createChild(
        config: Config,
        context: ComponentContext
    ): RootChild = when (config) {
        is Config.FileBrowser -> RootChild.FileBrowser(
            component = fileBrowserComponentFactory(
                componentContext = context,
                initialUri = config.initialUri
            )
        )
        // ... other configs
    }

    fun navigateToFileBrowser(initialUri: Uri? = null) {
        navigation.push(Config.FileBrowser(initialUri))
    }

    @Serializable
    sealed class Config {
        @Serializable
        data object Home : Config()

        @Serializable
        data class FileBrowser(
            val initialUri: Uri? = null
        ) : Config()
    }
}

@Composable
fun RootContent(component: RootComponent) {
    val childStack by component.childStack.subscribeAsState()

    Children(
        stack = childStack,
        animation = stackAnimation(fade())
    ) { child ->
        when (val instance = child.instance) {
            is RootChild.FileBrowser -> FileBrowserScreen(
                component = instance.component,
                onGoBack = { /* Navigate back */ }
            )
            // ... other children
        }
    }
}
*/

// Example 5: Request storage permission
/*
@Composable
fun FileBrowserWithPermission(
    componentContext: ComponentContext,
    fileBrowserComponentFactory: FileBrowserComponent.Factory
) {
    var hasPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                hasPermission = true
            }
        } else {
            hasPermission = true
        }
    }

    if (hasPermission) {
        val component = remember {
            fileBrowserComponentFactory(
                componentContext = componentContext,
                initialUri = null
            )
        }

        FileBrowserScreen(
            component = component,
            onGoBack = { /* Navigate back */ }
        )
    } else {
        // Show permission required UI
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Storage permission required")
            Button(onClick = {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }) {
                Text("Grant Permission")
            }
        }
    }
}
*/

