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

package com.t8rin.imagetoolbox.core.crash.presentation.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.model.wechat.Wechat
import com.t8rin.imagetoolbox.core.crash.presentation.screenLogic.CrashComponent
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.settings.presentation.model.toUiState
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppActivityClass
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.ui.utils.provider.ImageToolboxCompositionLocals
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalComponentActivity
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedFloatingActionButton
import com.t8rin.imagetoolbox.core.ui.widget.modifier.alertDialogBorder
import com.t8rin.imagetoolbox.core.ui.widget.other.ExpandableItem
import com.t8rin.imagetoolbox.core.ui.widget.text.AutoSizeText
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBugReport
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRestartAlt

@Composable
internal fun CrashRootContent(component: CrashComponent) {
    val context = LocalComponentActivity.current
    val crashInfo = component.crashInfo

    ImageToolboxCompositionLocals(
        settingsState = component.settingsState.toUiState(),
        settingsManager = component.settingsManager
    ) {
        val copyCrashInfo: () -> Unit = {
            Clipboard.copy(crashInfo.textToSend)
            AppToastHost.showToast(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                message = context.getString(R.string.copied),
            )
        }
        val showWechatTipsDialog = remember { mutableStateOf(false) }

        Box {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .displayCutoutPadding(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Icon(
                    painter = painterResource(id = com.shifenmiao.core.R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.height(80.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.something_went_wrong_emphasis),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (Wechat.isEnabled) {
                        Button(
                            modifier = Modifier
                                .requiredHeight(56.dp),
                            onClick = {
                                showWechatTipsDialog.value = true
                            },
                            colors = ButtonColors(
                                containerColor = Color(0xFF07c160),
                                contentColor = Color(0xFFFFFFFF),
                                disabledContainerColor = MaterialTheme.colorScheme.inverseSurface,
                                disabledContentColor = MaterialTheme.colorScheme.inverseSurface
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = com.shifenmiao.core.R.drawable.wechat),
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = com.shifenmiao.core.R.string.profile_item_wechat),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    Button(
                        modifier = Modifier
                            .requiredHeight(56.dp),
                        onClick = {
                            copyCrashInfo()
                            if (Wechat.isEnabled) {
                                Wechat.launchCustomerService()
                            }
                        },
                        colors = ButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.inversePrimary,
                            disabledContentColor = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = com.shifenmiao.core.R.string.profile_item_wechat_service),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                val interactionSource = remember {
                    MutableInteractionSource()
                }
                val pressed by interactionSource.collectIsPressedAsState()

                val cornerSize by animateDpAsState(
                    if (pressed) 8.dp
                    else 24.dp
                )
                ExpandableItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .navigationBarsPadding(),
                    shape = RoundedCornerShape(cornerSize),
                    interactionSource = interactionSource,
                    visibleContent = {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBugReport,
                            contentDescription = null,
                            modifier = Modifier.padding(
                                start = 16.dp,
                                top = 16.dp,
                                bottom = 16.dp
                            ),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        AutoSizeText(
                            text = crashInfo.exceptionName,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .padding(16.dp)
                                .weight(1f)
                        )
                    },
                    expandableContent = {
                        AnimatedVisibility(visible = it) {
                            SelectionContainer {
                                Text(
                                    text = crashInfo.stackTrace,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    },
                    color = MaterialTheme.colorScheme.surfaceContainer
                )
                Spacer(modifier = Modifier.height(80.dp))
            }
            Row(
                Modifier
                    .padding(8.dp)
                    .navigationBarsPadding()
                    .displayCutoutPadding()
                    .align(Alignment.BottomCenter)
            ) {
                EnhancedFloatingActionButton(
                    modifier = Modifier
                        .weight(1f, false),
                    onClick = {
                        context.startActivity(
                            Intent(context, AppActivityClass)
                        )
                    },
                    content = {
                        Spacer(Modifier.width(16.dp))
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRestartAlt,
                            contentDescription = stringResource(R.string.restart_app)
                        )
                        Spacer(Modifier.width(8.dp))
                        AutoSizeText(
                            text = stringResource(R.string.restart_app),
                            maxLines = 1
                        )
                        Spacer(Modifier.width(16.dp))
                    }
                )
                Spacer(Modifier.width(8.dp))
                EnhancedFloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = copyCrashInfo
                ) {
                    Spacer(Modifier.width(16.dp))
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                        contentDescription = stringResource(com.shifenmiao.core.R.string.copy_errors)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(com.shifenmiao.core.R.string.copy_errors),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.width(16.dp))
                }
            }
        }

        if (showWechatTipsDialog.value) {
            Clipboard.copy(crashInfo.textToSend)
            AlertDialog(
                modifier = Modifier.alertDialogBorder(),
                onDismissRequest = {
                    showWechatTipsDialog.value = false
                },
                confirmButton = {
                    EnhancedButton(onClick = {
                        showWechatTipsDialog.value = false
                        Wechat.launch()
                    }) {
                        Text(stringResource(com.shifenmiao.core.R.string.button_confirm))
                    }
                },
                title = { Text(stringResource(com.shifenmiao.core.R.string.dialog_title)) },
                text = {
                    Text(
                        stringResource(com.shifenmiao.core.R.string.profile_item_wechat_tips),
                        textAlign = TextAlign.Center
                    )
                }
            )
        }

    }
}