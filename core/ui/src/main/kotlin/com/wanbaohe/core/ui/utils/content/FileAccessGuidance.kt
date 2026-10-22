package com.wanbaohe.core.ui.utils.content

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.line.LineError
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.glass.GlassTextButton
import com.wanbaohe.core.ui.widget.glass.GlassTonalButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * file:// 直开无读权限时的统一授权引导。
 *
 * 三条恢复路径,按文件类型自动组合:
 * - 已持久化的 SAF 目录授权覆盖该文件 → 静默解析成文档 Uri,不打扰用户
 * - 媒体文件(图片/音视频)→ 运行时媒体权限(主)+ 仅授权所在文件夹 SAF(次)
 * - 非媒体文件 → 仅授权所在文件夹 SAF(Download 根目录系统禁授权,见 commit 记录)
 *
 * 用法(以查看器/编辑器页面为例):
 * ```kotlin
 * val controller = rememberFileAccessController(
 *     uriString = uri,
 *     isLoadFailed = loadError,
 *     mediaReadPermission = Manifest.permission.READ_MEDIA_IMAGES // 非媒体传 null
 * )
 * key(controller.retrySignal) { load(controller.effectiveUriString) }
 * if (controller.isGuidanceVisible) FileAccessGuidance(controller)
 * ```
 */

/**
 * 授权后的文件解析:在 SAF 目录树里按相对路径找回目标文件的可读 Uri。
 * 只需要 Context,不依赖 Hilt,任何模块可直接构造。
 */
class FileAccessResolver(context: Context) {

    private val appContext = context.applicationContext

    /** 持久化授权并解析;返回 null 表示授权目录里找不到该文件 */
    suspend fun resolveInGrantedTree(treeUri: Uri, fileUriString: String): String? {
        return withContext(Dispatchers.IO) {
            runCatching {
                appContext.contentResolver.takePersistableUriPermission(
                    treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            resolveDocumentInTree(treeUri, fileUriString)
        }
    }

    /** 已有持久授权覆盖该文件时静默解析出可读 Uri;没有则 null */
    suspend fun resolveFromPersistedGrants(fileUriString: String): String? {
        return withContext(Dispatchers.IO) {
            appContext.contentResolver.persistedUriPermissions
                .asSequence()
                .filter { it.isReadPermission }
                .mapNotNull { permission ->
                    runCatching {
                        resolveDocumentInTree(permission.uri, fileUriString)
                    }.getOrNull()
                }
                .firstOrNull()
        }
    }

    /** SAF 目录选择器的初始定位(图片所在文件夹);构造失败返回 null 由系统默认位置兜底 */
    fun initialFolderTreeUri(fileUriString: String): Uri? {
        return runCatching {
            val path = Uri.parse(fileUriString).path ?: return null
            val normalized = path.replaceFirst("^/sdcard/".toRegex(), "/storage/emulated/0/")
            val parent = normalized.substringBeforeLast('/', "")
            if (!parent.startsWith("/storage/emulated/0/")) return null
            // EXTRA_INITIAL_URI 必须是 document 形态的 Uri,
            // tree 形态会被 DocumentsUI 静默忽略回退到上次位置
            DocumentsContract.buildDocumentUri(
                "com.android.externalstorage.documents",
                "primary:${parent.removePrefix("/storage/emulated/0/")}"
            )
        }.getOrNull()
    }

    private fun resolveDocumentInTree(treeUri: Uri, fileUriString: String): String? {
        val filePath = Uri.parse(fileUriString).path ?: return null
        val tree = DocumentFile.fromTreeUri(appContext, treeUri) ?: return null

        // 授权目录是文件所在目录(或其上级):按相对路径逐段下钻
        val relative = relativePathInsideTree(treeUri, filePath)
        if (relative != null) {
            val segments = relative.split('/').filter { it.isNotEmpty() }
            var dir: DocumentFile? = tree
            for (segment in segments.dropLast(1)) {
                dir = dir?.listFiles()?.firstOrNull { it.isDirectory && it.name == segment }
            }
            val target = segments.lastOrNull()?.let { name ->
                dir?.listFiles()?.firstOrNull { it.isFile && it.name == name }
            }
            if (target != null) return target.uri.toString()
        }
        // 兜底:授权根下直接按文件名找
        val fileName = filePath.substringAfterLast('/')
        return tree.findFile(fileName)?.uri?.toString()
    }

    /**
     * 计算文件在授权目录树内的相对路径;不在树内返回 null。
     * treeDocumentId 形如 "primary:Pictures" 或 "<volume>:Pictures"
     */
    private fun relativePathInsideTree(treeUri: Uri, filePath: String): String? {
        val treeDocId = runCatching {
            DocumentsContract.getTreeDocumentId(treeUri)
        }.getOrNull() ?: return null

        val normalized = filePath.replaceFirst("^/sdcard/".toRegex(), "/storage/emulated/0/")
        val volumeRoot = when {
            treeDocId.startsWith("primary:") -> "/storage/emulated/0/"
            else -> {
                val volume = treeDocId.substringBefore(':', "")
                if (volume.isEmpty()) return null else "/storage/$volume/"
            }
        }
        if (!normalized.startsWith(volumeRoot)) return null

        val fileRelative = normalized.removePrefix(volumeRoot)
        val treeRelative = treeDocId.substringAfter(':', "")
        if (treeRelative.isEmpty()) return fileRelative
        if (!fileRelative.startsWith("$treeRelative/")) return null
        return fileRelative.removePrefix("$treeRelative/")
    }
}

@Stable
class FileAccessController internal constructor(
    val uriString: String,
    /** 是否应展示授权引导面板(已排除静默恢复与已授权的情况) */
    val isGuidanceVisible: Boolean,
    /** 正在静默检查已持久化的 SAF 授权(期间建议不渲染任何错误态,避免闪烁) */
    val isCheckingPersistedGrants: Boolean,
    /** SAF 授权成功后的可读文档 Uri;未授权时为 null */
    val resolvedUriString: String?,
    /**
     * 权限恢复事件:非 null 即代表"现在可以读了",值为可用于重试的 Uri
     * (SAF 恢复 = 文档 Uri;媒体权限恢复 = 原 file://)。加载是命令式的页面
     * (编辑器/PDF/zip)用 LaunchedEffect(recoveredUriString) 挂钩重载。
     */
    val recoveredUriString: String?,
    /** 权限到位后变化,调用方用 key(retrySignal) 驱动重载 */
    val retrySignal: Int,
    /** 是否有运行时媒体权限这条路(非媒体文件没有) */
    val hasMediaAccessPath: Boolean,
    internal val requestMediaAccess: () -> Unit,
    internal val requestFolderAccess: () -> Unit,
) {
    /** 实际用于加载的 Uri(SAF 授权后换成文档 Uri,否则原样) */
    val effectiveUriString: String get() = resolvedUriString ?: uriString
}

/**
 * 创建文件访问控制器。详见文件头 KDoc。
 *
 * @param uriString 目标文件 Uri 字符串(file:// 才可能触发引导)
 * @param isLoadFailed 调用方加载失败的信号(如 coil Error 态、openInputStream 抛异常)
 * @param mediaReadPermission 媒体文件对应的运行时权限(API 33+ 形态,
 * 如 [android.Manifest.permission.READ_MEDIA_IMAGES]);非媒体文件传 null
 */
@Composable
fun rememberFileAccessController(
    uriString: String,
    isLoadFailed: Boolean,
    mediaReadPermission: String? = null,
): FileAccessController {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val resolver = remember { FileAccessResolver(context) }

    var retrySignal by remember(uriString) { mutableIntStateOf(0) }
    var grantedUri by remember(uriString) { mutableStateOf<String?>(null) }
    var recoveredUri by remember(uriString) { mutableStateOf<String?>(null) }
    var persistedGrantChecked by remember(uriString) { mutableStateOf(false) }

    val mediaPermission = remember(mediaReadPermission) {
        mediaReadPermission?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) it
            else android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }
    var mediaPermissionGranted by remember(uriString, retrySignal) {
        mutableStateOf(
            mediaPermission != null && ContextCompat.checkSelfPermission(
                context, mediaPermission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val isFileUri = uriString.startsWith("file://")
    val needsGuidance = isLoadFailed && isFileUri &&
        grantedUri == null && !mediaPermissionGranted

    // 先静默尝试已持久化的 SAF 授权,兜不住再出引导
    LaunchedEffect(needsGuidance) {
        if (needsGuidance && !persistedGrantChecked) {
            val resolved = resolver.resolveFromPersistedGrants(uriString)
            if (resolved != null) {
                grantedUri = resolved
                recoveredUri = resolved
            }
            persistedGrantChecked = true
        }
    }

    val treeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        if (treeUri != null && treeUri != Uri.EMPTY) {
            scope.launch {
                val resolved = resolver.resolveInGrantedTree(treeUri, uriString)
                if (resolved != null) {
                    grantedUri = resolved
                    recoveredUri = resolved
                } else {
                    AppToastHost.showFailureToast(R.string.image_not_in_granted_folder)
                }
            }
        }
    }

    val mediaPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // 权限生效后 file:// 路径直接可读,retrySignal 驱动调用方重载
            mediaPermissionGranted = true
            recoveredUri = uriString
            retrySignal++
        } else {
            AppToastHost.showFailureToast(R.string.grant_permission_manual)
        }
    }

    return FileAccessController(
        uriString = uriString,
        isGuidanceVisible = needsGuidance && persistedGrantChecked,
        isCheckingPersistedGrants = needsGuidance && !persistedGrantChecked,
        resolvedUriString = grantedUri,
        recoveredUriString = recoveredUri,
        retrySignal = retrySignal,
        hasMediaAccessPath = mediaPermission != null,
        requestMediaAccess = { mediaPermission?.let(mediaPermissionLauncher::launch) },
        requestFolderAccess = {
            treeLauncher.launch(resolver.initialFolderTreeUri(uriString))
        },
    )
}

/**
 * 无权限引导面板:说明 + 授权按钮组(主按钮媒体权限 / 次按钮仅授权文件夹)。
 * 一般在深色查看器背景上使用,文案固定白色不随主题走。
 */
@Composable
fun FileAccessGuidance(
    controller: FileAccessController,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(32.dp)
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineError,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(
                if (controller.hasMediaAccessPath) R.string.image_load_no_permission
                else R.string.file_load_no_permission
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        if (controller.hasMediaAccessPath) {
            GlassTonalButton(
                onClick = controller.requestMediaAccess,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Text(stringResource(R.string.grant_media_access))
            }
            Spacer(Modifier.height(4.dp))
            GlassTextButton(
                onClick = controller.requestFolderAccess,
                contentColor = Color.White
            ) {
                Text(stringResource(R.string.grant_folder_access))
            }
        } else {
            GlassTonalButton(
                onClick = controller.requestFolderAccess,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Text(stringResource(R.string.grant_folder_access_single))
            }
        }
    }
}
