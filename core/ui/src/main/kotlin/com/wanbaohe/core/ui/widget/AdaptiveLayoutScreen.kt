/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
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

package com.wanbaohe.core.ui.widget

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import com.t8rin.imagetoolbox.core.resources.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.wanbaohe.core.ui.utils.animation.fancySlideTransition
import com.wanbaohe.core.ui.utils.helper.isPortraitOrientationAsState
import com.wanbaohe.core.ui.utils.provider.LocalScreenSize
import com.wanbaohe.core.ui.widget.dialogs.ExitBackHandler
import com.wanbaohe.core.ui.widget.enhanced.EnhancedIconButton
import com.wanbaohe.core.ui.widget.enhanced.EnhancedTopAppBar
import com.wanbaohe.core.ui.widget.enhanced.EnhancedTopAppBarDefaults
import com.wanbaohe.core.ui.widget.enhanced.EnhancedTopAppBarType
import com.wanbaohe.core.ui.widget.enhanced.enhancedFlingBehavior
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.glassBackground
import com.wanbaohe.core.ui.widget.image.imageStickyHeader
import com.wanbaohe.core.ui.widget.modifier.clearFocusOnTap
import com.wanbaohe.core.ui.widget.modifier.container
import com.wanbaohe.core.ui.widget.utils.isExpanded
import com.wanbaohe.core.ui.widget.utils.rememberAvailableHeight
import com.wanbaohe.core.ui.widget.utils.rememberImageState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AdaptiveLayoutScreen(
    title: @Composable () -> Unit,
    onGoBack: () -> Unit,
    shouldDisableBackHandler: Boolean,
    actions: @Composable RowScope.() -> Unit,
    topAppBarPersistentActions: @Composable RowScope.() -> Unit = {},
    imagePreview: @Composable () -> Unit,
    controls: (@Composable ColumnScope.(LazyListState) -> Unit)?,
    buttons: @Composable (actions: @Composable RowScope.() -> Unit) -> Unit,
    noDataControls: @Composable () -> Unit = {},
    canShowScreenData: Boolean,
    forceImagePreviewToMax: Boolean = false,
    contentPadding: Dp = 20.dp,
    showImagePreviewAsStickyHeader: Boolean = true,
    autoClearFocus: Boolean = true,
    placeImagePreview: Boolean = true,
    useRegularStickyHeader: Boolean = false,
    addHorizontalCutoutPaddingIfNoPreview: Boolean = true,
    showActionsInTopAppBar: Boolean = true,
    underTopAppBarContent: (@Composable ColumnScope.() -> Unit)? = null,
    insetsForNoData: WindowInsets = WindowInsets.navigationBars.union(
        WindowInsets.displayCutout.only(
            WindowInsetsSides.Horizontal
        )
    ),
    listState: LazyListState = rememberLazyListState(),
    placeControlsSeparately: Boolean = false,
    portraitTopPadding: Dp = 0.dp,
    imagePreviewBackgroundColor: Color = Color.Unspecified,
) {
    val isPortrait by isPortraitOrientationAsState()
    val settingsState = LocalSettingsState.current

    // 底部操作栏横跨屏幕底边。实测高度后把列表视口收到栏的上沿，
    // 滚动内容停在栏外，不会从栏底下穿过去。
    // 实测而非写死：多语言下按钮会换行，栏高并不固定。
    val density = LocalDensity.current
    var actionsBarHeightPx by rememberSaveable { mutableStateOf(0) }
    val actionsBarHeight = with(density) { actionsBarHeightPx.toDp() }
    val actionsBarMeasureModifier = Modifier.onSizeChanged { actionsBarHeightPx = it.height }

    var imageState by rememberImageState()

    val isGlassActive = settingsState.isGlassAlphaEnabled
    val topAppBarColors = if (isGlassActive) {
        EnhancedTopAppBarDefaults.colors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        )
    } else {
        EnhancedTopAppBarDefaults.colors()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clearFocusOnTap(autoClearFocus)
    ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                topBar = {
                    Column {
                        EnhancedTopAppBar(
                            type = EnhancedTopAppBarType.Center,
                            title = title,
                            colors = topAppBarColors,
                            drawHorizontalStroke = !isGlassActive && underTopAppBarContent == null,
                            navigationIcon = {
                                EnhancedIconButton(
                                    onClick = onGoBack
                                ) {
                                    Icon(
                                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                                        contentDescription = stringResource(R.string.exit)
                                    )
                                }
                            },
                            actions = {
                                if (!isPortrait && canShowScreenData && showActionsInTopAppBar) actions()
                                topAppBarPersistentActions()
                            }
                        )
                        underTopAppBarContent?.invoke(this)
                    }
                },
                contentWindowInsets = WindowInsets()
            ) { scaffoldPadding ->
                val screenWidthPx = LocalScreenSize.current.widthPx
                AnimatedContent(
                    targetState = canShowScreenData,
                    transitionSpec = {
                        fancySlideTransition(
                            isForward = targetState,
                            screenWidthPx = screenWidthPx
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(scaffoldPadding)
                ) { canShowScreenData ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        val direction = LocalLayoutDirection.current
                        if (!isPortrait && canShowScreenData && placeImagePreview) {
                            Box(
                                modifier = Modifier
                                    .then(
                                        if (controls != null) {
                                            Modifier.container(
                                                shape = RectangleShape,
                                                color = MaterialTheme.colorScheme.surfaceContainerLow
                                            )
                                        } else Modifier
                                    )
                                    .fillMaxHeight()
                                    .padding(
                                        start = WindowInsets
                                            .displayCutout
                                            .asPaddingValues()
                                            .calculateStartPadding(direction)
                                    )
                                    .weight(1.2f)
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                imagePreview()
                            }
                        }

                        // 竖屏栏贴在屏幕底边：列表视口收到栏的上沿。键盘比栏更高时再收到键盘上沿。
                        // 首帧还没测到高度时先按 100dp，避免内容闪到栏底下。
                        val bottomOverlay = if (isPortrait || !canShowScreenData) {
                            if (actionsBarHeight > 0.dp) actionsBarHeight else 100.dp
                        } else {
                            0.dp
                        }
                        val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
                        val navBottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding()
                        val bottomInset = if (isPortrait || !canShowScreenData) {
                            maxOf(bottomOverlay, imeBottom)
                        } else {
                            maxOf(navBottom, imeBottom) + contentPadding
                        }

                        if (placeControlsSeparately && controls != null && canShowScreenData) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(bottom = bottomInset)
                                    .clipToBounds()
                            ) {
                                controls(listState)
                            }
                        } else {
                            val internalHeight = rememberAvailableHeight(
                                imageState = imageState,
                                expanded = forceImagePreviewToMax
                            )
                            val cutout =
                                if (!placeImagePreview && addHorizontalCutoutPaddingIfNoPreview) {
                                    WindowInsets
                                        .displayCutout
                                        .asPaddingValues()
                                        .calculateStartPadding(direction)
                                } else 0.dp

                            var isScrolled by rememberSaveable(canShowScreenData) {
                                mutableStateOf(false)
                            }
                            val scope = rememberCoroutineScope {
                                Dispatchers.Main.immediate
                            }

                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(
                                    bottom = 0.dp,
                                    top = if (!canShowScreenData || !isPortrait) contentPadding else portraitTopPadding,
                                    start = contentPadding + cutout,
                                    end = contentPadding
                                ),
                                modifier = Modifier
                                    .weight(
                                        if (controls == null) 0.01f
                                        else 1f
                                    )
                                    .fillMaxHeight()
                                    .padding(bottom = bottomInset)
                                    .clipToBounds(),
                                flingBehavior = enhancedFlingBehavior()
                            ) {
                                if (useRegularStickyHeader && isPortrait && canShowScreenData && showImagePreviewAsStickyHeader && placeImagePreview) {
                                    stickyHeader {
                                        imagePreview()
                                    }
                                } else {
                                    imageStickyHeader(
                                        visible = isPortrait && canShowScreenData && showImagePreviewAsStickyHeader && placeImagePreview,
                                        internalHeight = internalHeight,
                                        imageState = imageState,
                                        onStateChange = { imageState = it },
                                        backgroundColor = imagePreviewBackgroundColor,
                                        imageBlock = imagePreview,
                                        onGloballyPositioned = {
                                            if (!isScrolled) {
                                                scope.launch {
                                                    delay(200)
                                                    listState.animateScrollToItem(0)
                                                    isScrolled = true
                                                }
                                            }
                                        }
                                    )
                                }
                                item {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        if (canShowScreenData) {
                                            AnimatedVisibility(
                                                visible = !showImagePreviewAsStickyHeader && isPortrait && placeImagePreview
                                            ) {
                                                imagePreview()
                                            }
                                            if (controls != null) controls(listState)
                                        } else {
                                            Box(
                                                modifier = Modifier.windowInsetsPadding(
                                                    insetsForNoData
                                                )
                                            ) {
                                                noDataControls()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        AnimatedVisibility(
                            !isPortrait && canShowScreenData,
                            modifier = actionsBarMeasureModifier
                        ) {
                            buttons(actions)
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isPortrait || !canShowScreenData,
                modifier = Modifier
                    .align(settingsState.fabAlignment)
                    .then(actionsBarMeasureModifier)
            ) {
                buttons(actions)
            }

            ExitBackHandler(
                enabled = !shouldDisableBackHandler,
                onBack = onGoBack
            )
    }
}