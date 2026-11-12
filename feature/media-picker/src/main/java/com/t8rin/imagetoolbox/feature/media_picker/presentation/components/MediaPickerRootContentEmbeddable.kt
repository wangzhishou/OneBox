/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file has been modified from the original ImageToolbox source.
 *
 * Origin: ImageToolbox derivative, modified
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

package com.t8rin.imagetoolbox.feature.media_picker.presentation.components

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.BrokenImageAlt
import com.shifenmiao.model.event.PermissionRequest
import com.wanbaohe.core.ui.utils.helper.ContextUtils.requestPermissions
import com.wanbaohe.core.ui.utils.permission.PermissionStatus
import com.wanbaohe.core.ui.utils.permission.PermissionUtils.checkPermissions
import com.wanbaohe.core.ui.utils.permission.PermissionUtils.markPermissionRequested
import com.wanbaohe.core.ui.utils.provider.LocalComponentActivity
import com.wanbaohe.core.ui.utils.provider.rememberCurrentLifecycleEvent
import com.wanbaohe.core.ui.widget.enhanced.EnhancedButton
import com.wanbaohe.core.ui.widget.enhanced.EnhancedIconButton
import com.wanbaohe.core.ui.widget.enhanced.EnhancedTopAppBar
import com.wanbaohe.core.ui.widget.text.marquee
import com.t8rin.imagetoolbox.feature.media_picker.domain.model.AllowedMedia
import com.t8rin.imagetoolbox.feature.media_picker.presentation.screenLogic.MediaPickerComponent
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack

@Composable
fun MediaPickerRootContentEmbeddable(
    component: MediaPickerComponent,
    onPicked: (List<Uri>) -> Unit,
    modifier: Modifier = Modifier,
    allowedMedia: AllowedMedia = AllowedMedia.Photos(null),
    allowMultiple: Boolean = true,
    title: String? = null,
    onBack: (() -> Unit)? = null
) {
    val context = LocalComponentActivity.current

    var isPermissionAllowed by remember {
        mutableStateOf(true)
    }

    // 申请权限统一走全局流程: 先弹用途说明(同屏告知目的)再拉起系统权限框。
    // 不能直接 ActivityCompat.requestPermissions: 那样既没有说明, requestCode 也不是
    // 全局在用的那个, 结果没人接。
    fun requestMediaPermission(permission: String) {
        context.requestPermissions(
            permissions = arrayOf(permission),
            permissionRequest = PermissionRequest.ALL,
            onSuccess = {
                isPermissionAllowed = true
                component.init(allowedMedia)
            },
            onFailed = {
                isPermissionAllowed = false
            }
        )
    }

    // 只在进入页面时自动问一次, 避免每次回到前台都弹
    var autoPrompted by remember { mutableStateOf(false) }
    val lifecycleEvent = rememberCurrentLifecycleEvent()
    LaunchedEffect(lifecycleEvent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.READ_MEDIA_IMAGES
            when (context.checkPermissions(listOf(permission)).finalStatus) {
                PermissionStatus.ALLOWED -> {
                    isPermissionAllowed = true
                    component.init(allowedMedia)
                }

                PermissionStatus.NOT_GIVEN -> {
                    if (!autoPrompted) {
                        autoPrompted = true
                        context.markPermissionRequested(permission)
                        requestMediaPermission(permission)
                    }
                }

                PermissionStatus.DENIED_PERMANENTLY -> Unit
            }
        }
    }

    val content: @Composable (PaddingValues) -> Unit = {
        AnimatedContent(
            targetState = isPermissionAllowed,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = it.calculateTopPadding())
        ) { havePermissions ->
            if (havePermissions) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MediaPickerHavePermissions(
                        allowedMedia = allowedMedia,
                        allowMultiple = allowMultiple,
                        component = component,
                        onPicked = onPicked
                    )
                    LaunchedEffect(Unit) {
                        component.init(allowedMedia = allowedMedia)
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.BrokenImageAlt,
                        contentDescription = null,
                        modifier = Modifier.size(108.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.no_permissions),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    EnhancedButton(
                        onClick = {
                            val permission =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    Manifest.permission.READ_MEDIA_IMAGES
                                } else {
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                }
                            requestMediaPermission(permission)
                        }
                    ) {
                        Text(stringResource(id = R.string.request))
                    }
                }
            }
        }
    }

    Box(modifier = modifier) {
        if (onBack == null) {
            content(PaddingValues())
        } else {
            Scaffold(
                topBar = {
                    EnhancedTopAppBar(
                        title = {
                            Text(
                                text = title?.takeIf { it.isNotBlank() } ?: if (allowMultiple) {
                                    stringResource(R.string.pick_multiple_media)
                                } else {
                                    stringResource(R.string.pick_single_media)
                                },
                                modifier = Modifier.marquee()
                            )
                        },
                        navigationIcon = {
                            EnhancedIconButton(
                                onClick = onBack,
                                containerColor = Color.Transparent
                            ) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                                    contentDescription = stringResource(R.string.close)
                                )
                            }
                        },
                        actions = {
                        },
                        drawHorizontalStroke = component.albumsState.collectAsState().value.albums.size <= 1
                    )
                },
                content = content
            )
        }
    }
}