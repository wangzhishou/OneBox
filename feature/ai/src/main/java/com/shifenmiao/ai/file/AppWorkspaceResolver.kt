package com.shifenmiao.ai.file

import android.os.Environment
import androidx.core.net.toUri
import com.t8rin.imagetoolbox.core.data.utils.SafUriUtils
import com.t8rin.imagetoolbox.core.settings.domain.SettingsProvider
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppWorkspaceResolver @Inject constructor(
    private val settingsProvider: SettingsProvider,
) {

    fun resolve(): AppWorkspaceRoot {
        val configuredRoot = resolveConfiguredWorkspaceRootFile()
        return if (configuredRoot != null) {
            AppWorkspaceRoot(
                name = APP_WORKSPACE_NAME,
                file = configuredRoot,
                description = "Configured app save folder",
            )
        } else {
            AppWorkspaceRoot(
                name = APP_WORKSPACE_NAME,
                file = defaultWorkspaceRootFile(),
                description = "Default Documents/OneBox workspace",
            )
        }
    }

    private fun resolveConfiguredWorkspaceRootFile(): File? {
        val rawUri = settingsProvider.settingsState.value.saveFolderUri ?: return null
        return runCatching {
            val parsed = rawUri.toInputUri()
            val localUri = when (parsed.scheme) {
                null, "", "file" -> parsed
                "content" -> SafUriUtils.treeUriToFileUri(parsed) ?: SafUriUtils.documentUriToFileUri(parsed)
                else -> null
            } ?: return null
            val path = localUri.path ?: return null
            File(path).canonicalFile
        }.getOrNull()
    }

    @Suppress("DEPRECATION")
    private fun defaultWorkspaceRootFile(): File {
        return File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            DEFAULT_WORKSPACE_FOLDER_NAME,
        ).canonicalFile
    }

    private fun String.toInputUri() = toUri().let { uri ->
        if (uri.scheme.isNullOrBlank()) android.net.Uri.fromFile(File(this)) else uri
    }

    companion object {
        private const val APP_WORKSPACE_NAME = "app_workspace"
        private const val DEFAULT_WORKSPACE_FOLDER_NAME = "OneBox"
    }
}

data class AppWorkspaceRoot(
    val name: String,
    val file: File,
    val description: String,
)

