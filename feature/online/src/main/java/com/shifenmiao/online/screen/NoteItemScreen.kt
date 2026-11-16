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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.provider.LocalDataDraftHelper
import com.shifenmiao.common.components.text.defaultTextSelectionMenuItems
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.markdown.OneBoxMarkdownContent
import com.shifenmiao.common.markdown.rememberOneBoxMarkdownContentState
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.rememberMarkdownCodeActions
import com.shifenmiao.core.R
import com.shifenmiao.model.ListItemType
import com.shifenmiao.online.component.NoteItemComponent
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.widget.enhanced.EnhancedButton
import com.wanbaohe.core.ui.widget.enhanced.EnhancedTopAppBarType
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLock

@Composable
fun NoteItemScreen(
    noteItemComponent: NoteItemComponent,
    appComponent: AppComponent
) {
    val noteUIState by noteItemComponent.noteUIState.collectAsState()
    val navigator = LocalUrlNavigator.current
    val uriHandler = LocalUriHandler.current
    val dataDraftHelper = LocalDataDraftHelper.current
    val selectionMenuItems = defaultTextSelectionMenuItems(
        appComponent = appComponent,
        onNavigate = { screen -> navigator.navigate(screen) },
    )
    val scope = rememberCoroutineScope()
    val markdownState = rememberOneBoxMarkdownContentState()

    // 页面显示时刷新数据，确保显示最新的编辑内容
    LaunchedEffect(Unit) {
        noteItemComponent.refreshData()
    }

    val codeActions = rememberMarkdownCodeActions(appComponent = appComponent)
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    BackHandler { appComponent.onGoBack() }
    BaseScreen(
        title = {
            Text(
                text = noteUIState.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    lineHeight = MaterialTheme.typography.titleLarge.fontSize * 1.2f
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        onGoBack = { appComponent.onGoBack() },
        type = EnhancedTopAppBarType.Medium,
        scrollBehavior = scrollBehavior,
        actions = {
            if (!noteUIState.isLocked) {
                IconButton(
                    onClick = {
                        scope.launch {
                            val draftId = dataDraftHelper.createDraft(
                                draftType = ListItemType.NOTE.id,
                                itemId = noteUIState.itemId
                            )
                            navigator.navigate(Screen.CreateNote(draftId = draftId))
                        }
                    }
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(end = AppTheme.dimens.paddingNormal),
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        onTitleClick = {
            scope.launch { markdownState.scrollToContentStart() }
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
    modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
    state = markdownState,
    codeActions = codeActions,
    selectionItems = selectionMenuItems,
    message = noteUIState.data,
    isLoading = noteUIState.isLoading,
    errorMessage = noteUIState.errorMessage,
    onRetry = { noteItemComponent.refreshData() },
    onGoBack = { appComponent.onGoBack() },
    onLinkClick = { url ->
        if (!navigator.navigate(url)) {
            uriHandler.openUri(url)
        }
    },
    // 阅读态不再展示分类 chip：分类只在保存/设置弹窗里维护，正文直接从标题下方开始。
    // 页面级已由 BaseScreen 补 navigationBarsPadding；正文末尾再留一段可滚动留白
    paddingValues = PaddingValues(
        start = AppTheme.dimens.paddingNormal,
        top = 0.dp,
        end = AppTheme.dimens.paddingNormal,
        bottom = 30.dp
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
