/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.core.ui.utils.helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import com.t8rin.imagetoolbox.core.domain.BackupFileExtension
import com.wanbaohe.core.ui.utils.content.ContentRouter
import com.t8rin.imagetoolbox.core.domain.model.ExtraDataType
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.core.ui.utils.helper.ContextUtils.getScreenExtra
import com.wanbaohe.core.ui.utils.helper.ContextUtils.getScreenOpeningShortcut
import com.wanbaohe.core.ui.utils.helper.IntentUtils.parcelable
import com.wanbaohe.core.ui.utils.helper.IntentUtils.parcelableArrayList
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.utils.appContext
import com.t8rin.imagetoolbox.core.resources.icons.line.LineError

/**
 * 处理外部传入的 Deeplink / Share Intent。
 *
 * 单文件场景（ACTION_VIEW 单文件、ACTION_SEND 单文件）优先委托给 [ContentRouter]
 * 做统一路由决策，确保外部打开文件和文件浏览器内点击文件的行为一致。
 *
 * 多文件/混合类型场景保持原有处理逻辑不变。
 */
fun Intent?.handleDeeplinks(
    context: Context,
    onStart: () -> Unit,
    onColdStart: () -> Unit,
    onShowToast: (message: String, icon: ImageVector) -> Unit,
    onNavigate: (Screen) -> Unit,
    onGetUris: (List<Uri>) -> Unit,
    onHasExtraDataType: (ExtraDataType) -> Unit,
    isHasUris: Boolean,
    onWantGithubReview: () -> Unit,
    isOpenEditInsteadOfPreview: Boolean,
    contentRouter: ContentRouter? = null,
) {
    if (this == null) return

    val intent = this

    onStart()
    if (intent.type != null && !isHasUris) onColdStart()

    if (intent.action == Intent.ACTION_BUG_REPORT) {
        onWantGithubReview()
        return
    }

    if (intent.getScreenOpeningShortcut(onNavigate)) return

    runCatching {
        val startsWithImage = intent.type?.startsWith("image/") == true
        val hasExtraFormats = intent.clipData?.clipList()
            ?.any {
                it.toString().endsWith(".jxl") || it.toString().endsWith(".qoi")
            } == true
        val dataHasExtraFormats = intent.data.toString().let {
            it.endsWith(".jxl") || it.endsWith(".qoi")
        }

        if ((startsWithImage || hasExtraFormats || dataHasExtraFormats)) {
            when (intent.action) {
                Intent.ACTION_VIEW, Intent.ACTION_EDIT -> {
                    val data = intent.data
                    val clipData = intent.clipData
                    val uris =
                        clipData?.clipList() ?: data?.let { listOf(it) } ?: return@runCatching

                    // 系统"打开/编辑图片"默认进图片创作(该 Screen 支持外部传入图片 Uri)
                    onNavigate(Screen.MarkupLayers(uris.firstOrNull()))
                }

                Intent.ACTION_SEND -> {
                    intent.parcelable<Uri>(Intent.EXTRA_STREAM)?.let {
                        when (intent.getScreenExtra()) {
                            is Screen.PickColorFromImage -> onNavigate(Screen.PickColorFromImage(it))
                            is Screen.PaletteTools -> onNavigate(Screen.PaletteTools(it))
                            else -> {
                                if (intent.type?.contains("gif") == true) {
                                    onHasExtraDataType(ExtraDataType.Gif)
                                }
                                onGetUris(listOf(it))
                            }
                        }
                    }
                }

                Intent.ACTION_SEND_MULTIPLE -> {
                    intent.parcelableArrayList<Uri>(Intent.EXTRA_STREAM)?.let {
                        if (intent.type?.contains("gif") == true) {
                            onHasExtraDataType(ExtraDataType.Gif)
                            it.firstOrNull()?.let { uri ->
                                onGetUris(listOf(uri))
                            }
                        } else onGetUris(it)
                    }
                }

                else -> {
                    intent.data?.let {
                        if (intent.type?.contains("gif") == true) {
                            onHasExtraDataType(ExtraDataType.Gif)
                        }
                        onGetUris(listOf(it))
                    }
                }
            }
        } else if (intent.type != null) {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            val multiplePdfs = intent.parcelableArrayList<Uri>(Intent.EXTRA_STREAM) != null

            if (
                intent.type?.contains("pdf") == true && !multiplePdfs
            ) {
                val uri = intent.data ?: intent.parcelable<Uri>(Intent.EXTRA_STREAM)
                uri?.let {
                    if (intent.action == Intent.ACTION_VIEW) {
                        // 单文件场景：优先委托给 ContentRouter
                        if (contentRouter != null) {
                            contentRouter.route(
                                uri = it,
                                context = context,
                                onNavigate = onNavigate,
                                allSiblings = emptyList(),
                                fallbackToExternal = false,
                                mimeTypeHint = intent.type
                            )
                        } else {
                            onNavigate(Screen.PdfTools(Screen.PdfTools.Type.Preview(it)))
                        }
                    } else {
                        onHasExtraDataType(ExtraDataType.Pdf)
                        onGetUris(listOf(it))
                    }
                }
            } else if (text != null) {
                onHasExtraDataType(ExtraDataType.Text(text))
                onGetUris(listOf())
            } else {
                val isAudio = intent.type?.startsWith("audio/") == true

                when (intent.action) {
                    Intent.ACTION_SEND_MULTIPLE -> {
                        intent.parcelableArrayList<Uri>(Intent.EXTRA_STREAM)?.let { uris ->
                            // 多选音视频也进播放器（队列=选中的全部），和单条分享保持一致
                            val isVideo = intent.type?.startsWith("video/") == true
                            if (isAudio || isVideo) {
                                val handled = uris.firstOrNull()?.let { first ->
                                    contentRouter?.route(
                                        uri = first,
                                        context = context,
                                        onNavigate = onNavigate,
                                        allSiblings = uris,
                                        fallbackToExternal = false
                                    )
                                } == true
                                if (handled) return
                                onHasExtraDataType(ExtraDataType.Audio)
                                onGetUris(uris)
                            } else {
                                onNavigate(Screen.Zip(uris))
                            }
                        }
                    }

                    Intent.ACTION_SEND -> {
                        intent.parcelable<Uri>(Intent.EXTRA_STREAM)?.let {
                            if (it.toString().contains(BackupFileExtension, true)) {
                                onHasExtraDataType(ExtraDataType.Backup(it.toString()))
                                return
                            }
                            // 分享进来的 zip 直接打开压缩包浏览页，不再落到"选择工具"页。
                            // 只认 application/zip 精确类型、.zip 后缀或真实探测结果；
                            // 不能用 contains("zip")，application/gzip 也会误命中。
                            if (isZipShare(it, context, intent.type) && contentRouter != null) {
                                val handled = contentRouter.route(
                                    uri = it,
                                    context = context,
                                    onNavigate = onNavigate,
                                    allSiblings = emptyList(),
                                    fallbackToExternal = false,
                                    mimeTypeHint = intent.type
                                )
                                if (handled) return
                            }
                            // 分享进来的音视频直接进播放器，不再落到"选择工具"页
                            val isVideo = intent.type?.startsWith("video/") == true
                            if ((isAudio || isVideo) && contentRouter != null) {
                                val handled = contentRouter.route(
                                    uri = it,
                                    context = context,
                                    onNavigate = onNavigate,
                                    allSiblings = emptyList(),
                                    fallbackToExternal = false,
                                    mimeTypeHint = intent.type
                                )
                                if (handled) return
                            }
                            if (isAudio) {
                                onHasExtraDataType(ExtraDataType.Audio)
                            } else {
                                onHasExtraDataType(ExtraDataType.File)
                            }

                            onGetUris(listOf(it))
                        }
                    }

                    Intent.ACTION_VIEW,
                    Intent.ACTION_EDIT -> {
                        val data = intent.data
                        val clipData = intent.clipData
                        val uris =
                            clipData?.clipList() ?: data?.let { listOf(it) } ?: emptyList()

                        if (uris.size == 1) {
                            val uri = uris.first()

                            if (uri.toString().contains(BackupFileExtension, true)) {
                                onHasExtraDataType(ExtraDataType.Backup(uri.toString()))
                                return
                            }

                            if (isAudio) {
                                // 音频也要走统一路由：ContentRouter 会落到播放器，
                                // 不再进旧的"音频工具"选择页
                                val handled = contentRouter?.route(
                                    uri = uri,
                                    context = context,
                                    onNavigate = onNavigate,
                                    allSiblings = uris,
                                    fallbackToExternal = false
                                ) == true
                                if (handled) return@runCatching
                                onHasExtraDataType(ExtraDataType.Audio)
                            } else {
                                // 单文件场景：优先委托给 ContentRouter 做统一路由
                                if (contentRouter != null) {
                                    val handled = contentRouter.route(
                                        uri = uri,
                                        context = context,
                                        onNavigate = onNavigate,
                                        allSiblings = emptyList(),
                                        // 外部打开入口：打不开的类型回退文件浏览器，
                                        // 不外抛 chooser（本 App 在候选里会死循环）
                                        fallbackToExternal = false,
                                        mimeTypeHint = intent.type
                                    )
                                    if (handled) return@runCatching
                                }
                                onHasExtraDataType(ExtraDataType.File)
                            }

                            onGetUris(uris)
                        } else if (uris.isNotEmpty()) {
                            if (isAudio) {
                                onHasExtraDataType(ExtraDataType.Audio)
                                onGetUris(uris)
                            } else {
                                onNavigate(Screen.Zip(uris))
                            }
                        } else {
                            Unit
                        }
                    }

                    else -> null
                } ?: onShowToast(
                    appContext.getString(R.string.unsupported_type, intent.type),
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineError
                )
            }
        } else Unit
    }.getOrNull() ?: onShowToast(
        appContext.getString(R.string.something_went_wrong),
        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineError
    )
}

private fun isZipShare(
    uri: Uri,
    context: Context,
    mimeType: String?
): Boolean {
    if (mimeType.isZipMime()) return true
    if (uri.toString().substringBefore('?').endsWith(".zip", ignoreCase = true)) return true

    val displayName = runCatching {
        context.contentResolver.query(
            uri,
            arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }.getOrNull()
    if (displayName?.endsWith(".zip", ignoreCase = true) == true) return true

    return runCatching { context.contentResolver.getType(uri) }
        .getOrNull()
        .isZipMime()
}

private fun String?.isZipMime(): Boolean =
    this == "application/zip" || this == "application/x-zip-compressed"
