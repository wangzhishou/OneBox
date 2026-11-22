/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
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

package com.shifenmiao.online.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.provider.LocalDataDraftHelper
import com.shifenmiao.common.components.text.defaultTextSelectionMenuItems
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.markdown.OneBoxMarkdownContent
import com.shifenmiao.common.markdown.rememberInAppLinkHandler
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.rememberMarkdownCodeActions
import com.shifenmiao.core.R
import com.shifenmiao.model.ListItemType
import com.shifenmiao.online.component.NoteItemComponent
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.widget.enhanced.EnhancedButton
import com.wanbaohe.core.ui.widget.enhanced.EnhancedFloatingActionButton
import com.wanbaohe.core.ui.widget.enhanced.EnhancedFloatingActionButtonType
import com.wanbaohe.core.ui.widget.enhanced.EnhancedTopAppBarType
import kotlinx.coroutines.launch
import com.wanbaohe.icons.LineEditorIcons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLock

private val NoteEditFabSize = 56.dp
private val NoteEditFabBottomMargin = 16.dp

@Composable
fun NoteItemScreen(
    noteItemComponent: NoteItemComponent,
    appComponent: AppComponent
) {
    val noteUIState by noteItemComponent.noteUIState.collectAsState()
    val navigator = LocalUrlNavigator.current
    val onLinkClick = rememberInAppLinkHandler()
    val dataDraftHelper = LocalDataDraftHelper.current
    val selectionMenuItems = defaultTextSelectionMenuItems(
        appComponent = appComponent,
        onNavigate = { screen -> navigator.navigate(screen) },
    )
    val scope = rememberCoroutineScope()

    // 页面显示时刷新数据，确保显示最新的编辑内容
    LaunchedEffect(Unit) {
        noteItemComponent.refreshData()
    }

    val codeActions = rememberMarkdownCodeActions(appComponent = appComponent)
    val canEdit = !noteUIState.isLocked && !noteUIState.isLoading &&
        noteUIState.errorMessage == null && noteUIState.itemId > 0
    BaseScreen(
        title = {},
        onGoBack = { appComponent.onGoBack() },
        type = EnhancedTopAppBarType.Normal,
        foreground = {
            if (canEdit) {
                EnhancedFloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = AppTheme.dimens.pageHorizontal, bottom = NoteEditFabBottomMargin),
                    type = EnhancedFloatingActionButtonType.Custom(NoteEditFabSize, CircleShape),
                    onClick = {
                        scope.launch {
                            val draftId = dataDraftHelper.createDraft(
                                draftType = ListItemType.NOTE.id,
                                itemId = noteUIState.itemId
                            )
                            navigator.navigate(Screen.CreateNote(draftId = draftId))
                        }
                    },
                ) {
                    Icon(
                        imageVector = LineEditorIcons.Edit,
                        contentDescription = stringResource(R.string.edit),
                    )
                }
            }
        },
        showNavigationBarsPadding = true
    ) {
        if (noteUIState.isLocked) {
            LockedNoteContent(
                onUnlock = noteItemComponent::unlock,
                modifier = Modifier.fillMaxSize()
            )
            return@BaseScreen
        }
        OneBoxMarkdownContent(
            modifier = Modifier.fillMaxSize(),
            codeActions = codeActions,
            selectionItems = selectionMenuItems,
            message = noteUIState.data,
            isLoading = noteUIState.isLoading,
            errorMessage = noteUIState.errorMessage,
            onRetry = { noteItemComponent.refreshData() },
            onGoBack = { appComponent.onGoBack() },
            onLinkClick = onLinkClick,
            paddingValues = PaddingValues(
                start = AppTheme.dimens.pageHorizontal,
                top = 0.dp,
                end = AppTheme.dimens.pageHorizontal,
                bottom = 8.dp
            ),
        )
    }
}

/**
 * 加锁笔记的占位内容:不渲染正文,点击后拉起全局授权码,解锁成功自动加载。
 */
@Composable
private fun LockedNoteContent(
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(AppTheme.dimens.paddingNormal),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceNormal)
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = stringResource(R.string.item_locked_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.item_locked_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            EnhancedButton(
                onClick = onUnlock,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(top = AppTheme.dimens.spaceNormal)
            ) {
                Text(stringResource(R.string.item_locked_unlock))
            }
        }
    }
}
