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

package com.wanbaohe.blessingwall.screen

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.enhanced.longPress
import com.wanbaohe.core.ui.widget.glass.GlassButton
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.utils.blessing.BlessingEffectType
import com.wanbaohe.core.ui.utils.capturable.capturable
import com.wanbaohe.core.ui.utils.capturable.rememberCaptureController
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.blessingwall.R
import com.wanbaohe.blessingwall.component.BlessingWallComponent
import com.wanbaohe.blessingwall.model.BlessingTabCustomization
import com.wanbaohe.blessingwall.model.BlessingType
import com.wanbaohe.blessingwall.model.BlessingWallOrder
import com.wanbaohe.blessingwall.model.resolveTabText
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.VolumeOff
import com.t8rin.imagetoolbox.core.resources.icons.VolumeUp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMagic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTouchApp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFavorite
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMeditation

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BlessingWallScreen(component: BlessingWallComponent) {
    val uiState by component.uiState.collectAsState()
    val soundEnabled by component.soundEnabled.collectAsState()
    val isHistoryMode = component.isHistoryMode
    val pageCount = BlessingType.entries.size
    val pagerState = rememberPagerState(
        initialPage = component.initialPage.coerceIn(0, pageCount - 1),
        pageCount = { pageCount },
    )
    val pagerScope = rememberCoroutineScope()

    val allPageConfigs = listOf(
        BlessingPageConfig(
            type = BlessingType.WOODEN_FISH,
            imageRes = R.drawable.blessing_wooden_fish,
            tabIconRes = R.drawable.blessing_tab_wooden_fish,
            title = R.string.blessing_tab_wooden_fish,
            subtitle = R.string.blessing_subtitle_wooden_fish,
            buttonText = R.string.blessing_btn_wooden_fish,
            statTitle = R.string.blessing_stat_wooden_fish,
            effectType = BlessingEffectType.WoodenFish,
            actionIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTouchApp,
        ),
        BlessingPageConfig(
            type = BlessingType.WEALTH_GOD,
            imageRes = R.drawable.blessing_wealth_god,
            tabIconRes = R.drawable.blessing_tab_wealth_god,
            title = R.string.blessing_tab_wealth_god,
            subtitle = R.string.blessing_subtitle_wealth_god,
            buttonText = R.string.blessing_btn_wealth_god,
            statTitle = R.string.blessing_stat_wealth_god,
            effectType = BlessingEffectType.WealthGod,
            actionIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMagic,
        ),
        BlessingPageConfig(
            type = BlessingType.GUANYIN,
            imageRes = R.drawable.blessing_guanyin,
            tabIconRes = R.drawable.blessing_tab_guanyin,
            title = R.string.blessing_tab_guanyin,
            subtitle = R.string.blessing_subtitle_guanyin,
            buttonText = R.string.blessing_btn_guanyin,
            statTitle = R.string.blessing_stat_guanyin,
            effectType = BlessingEffectType.Guanyin,
            actionIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFavorite,
        ),
        BlessingPageConfig(
            type = BlessingType.INCENSE,
            imageRes = R.drawable.blessing_incense,
            tabIconRes = R.drawable.blessing_tab_incense,
            title = R.string.blessing_tab_incense,
            subtitle = R.string.blessing_subtitle_incense,
            buttonText = R.string.blessing_btn_incense,
            statTitle = R.string.blessing_stat_incense,
            effectType = BlessingEffectType.Incense,
            actionIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMeditation,
        ),
        BlessingPageConfig(
            type = BlessingType.HAMSA,
            imageRes = R.drawable.blessing_hamsa,
            tabIconRes = R.drawable.blessing_tab_hamsa,
            title = R.string.blessing_tab_hamsa,
            subtitle = R.string.blessing_subtitle_hamsa,
            buttonText = R.string.blessing_btn_hamsa,
            statTitle = R.string.blessing_stat_hamsa,
            effectType = BlessingEffectType.Hamsa,
            actionIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFavorite,
        ),
        BlessingPageConfig(
            type = BlessingType.CUPID,
            imageRes = R.drawable.blessing_cupid,
            tabIconRes = R.drawable.blessing_tab_cupid,
            title = R.string.blessing_tab_cupid,
            subtitle = R.string.blessing_subtitle_cupid,
            buttonText = R.string.blessing_btn_cupid,
            statTitle = R.string.blessing_stat_cupid,
            effectType = BlessingEffectType.Cupid,
            actionIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFavorite,
        ),
        BlessingPageConfig(
            type = BlessingType.CLOVER,
            imageRes = R.drawable.blessing_clover,
            tabIconRes = R.drawable.blessing_tab_clover,
            title = R.string.blessing_tab_clover,
            subtitle = R.string.blessing_subtitle_clover,
            buttonText = R.string.blessing_btn_clover,
            statTitle = R.string.blessing_stat_clover,
            effectType = BlessingEffectType.Clover,
            actionIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMagic,
        ),
        BlessingPageConfig(
            type = BlessingType.ANGEL,
            imageRes = R.drawable.blessing_angel,
            tabIconRes = R.drawable.blessing_tab_angel,
            title = R.string.blessing_tab_angel,
            subtitle = R.string.blessing_subtitle_angel,
            buttonText = R.string.blessing_btn_angel,
            statTitle = R.string.blessing_stat_angel,
            effectType = BlessingEffectType.Angel,
            actionIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFavorite,
        ),
        BlessingPageConfig(
            type = BlessingType.LUCKY_CAT,
            imageRes = R.drawable.blessing_lucky_cat,
            tabIconRes = R.drawable.blessing_tab_lucky_cat,
            title = R.string.blessing_tab_lucky_cat,
            subtitle = R.string.blessing_subtitle_lucky_cat,
            buttonText = R.string.blessing_btn_lucky_cat,
            statTitle = R.string.blessing_stat_lucky_cat,
            effectType = BlessingEffectType.LuckyCat,
            actionIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMagic,
        ),
        BlessingPageConfig(
            type = BlessingType.DREAMCATCHER,
            imageRes = R.drawable.blessing_dreamcatcher,
            tabIconRes = R.drawable.blessing_tab_dreamcatcher,
            title = R.string.blessing_tab_dreamcatcher,
            subtitle = R.string.blessing_subtitle_dreamcatcher,
            buttonText = R.string.blessing_btn_dreamcatcher,
            statTitle = R.string.blessing_stat_dreamcatcher,
            effectType = BlessingEffectType.Dreamcatcher,
            actionIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTouchApp,
        ),
        BlessingPageConfig(
            type = BlessingType.WISHBONE,
            imageRes = R.drawable.blessing_wishbone,
            tabIconRes = R.drawable.blessing_tab_wishbone,
            title = R.string.blessing_tab_wishbone,
            subtitle = R.string.blessing_subtitle_wishbone,
            buttonText = R.string.blessing_btn_wishbone,
            statTitle = R.string.blessing_stat_wishbone,
            effectType = BlessingEffectType.Wishbone,
            actionIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMagic,
        ),
        BlessingPageConfig(
            type = BlessingType.HORSESHOE,
            imageRes = R.drawable.blessing_horseshoe,
            tabIconRes = R.drawable.blessing_tab_horseshoe,
            title = R.string.blessing_tab_horseshoe,
            subtitle = R.string.blessing_subtitle_horseshoe,
            buttonText = R.string.blessing_btn_horseshoe,
            statTitle = R.string.blessing_stat_horseshoe,
            effectType = BlessingEffectType.Horseshoe,
            actionIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTouchApp,
        ),
    )
    val pageConfigs = remember(allPageConfigs) {
        val orderedTypes = BlessingWallOrder.types()
        allPageConfigs.sortedBy { orderedTypes.indexOf(it.type) }
    }
    val colorScheme = MaterialTheme.colorScheme
    val basePalettes = listOf(
        BlessingPalette(
            buttonContainer = MaterialTheme.colorScheme.primaryContainer,
            buttonContent = MaterialTheme.colorScheme.onPrimaryContainer,
            accent = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        BlessingPalette(
            buttonContainer = colorScheme.secondaryContainer,
            buttonContent = colorScheme.onSecondaryContainer,
            accent = colorScheme.onSecondaryContainer,
        ),
        BlessingPalette(
            buttonContainer = colorScheme.tertiaryContainer,
            buttonContent = colorScheme.onTertiaryContainer,
            accent = colorScheme.onTertiaryContainer,
        ),
        BlessingPalette(
            buttonContainer = colorScheme.surfaceContainerLow,
            buttonContent = colorScheme.onSurfaceVariant,
            accent = colorScheme.onSurface,
        ),
    )
    val palettes = pageConfigs.mapIndexed { index, _ -> basePalettes[index % basePalettes.size] }
    val selectedPalette = palettes[pagerState.currentPage]

    val shareActions = remember { mutableStateMapOf<Int, () -> Unit>() }
    val meritCycle = stringResource(R.string.blessing_merit_cycle)
    val historyTitle = remember(isHistoryMode, component.targetDate) {
        if (!isHistoryMode) {
            ""
        } else {
            runCatching {
                LocalDate.parse(component.targetDate).format(
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        .withLocale(Locale.getDefault()),
                )
            }.getOrDefault(component.targetDate)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        component.onPageChanged(pagerState.currentPage)
    }

    BaseScreen(
        title = historyTitle,
        onGoBack = component.onGoBack,
        actions = {
            if (!isHistoryMode) {
                IconButton(
                    onClick = component::toggleSoundEnabled,
                    colors = AppTheme.colors.iconButtonColors(),
                ) {
                    Icon(
                        imageVector = if (soundEnabled) {
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.VolumeUp
                        } else {
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.VolumeOff
                        },
                        contentDescription = stringResource(
                            if (soundEnabled) R.string.blessing_sound_on
                            else R.string.blessing_sound_off
                        ),
                    )
                }
                IconButton(
                    onClick = component::navigateToRecord,
                    colors = AppTheme.colors.iconButtonColors(),
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                        contentDescription = stringResource(R.string.blessing_record_title),
                    )
                }
            }
            IconButton(
                onClick = { shareActions[pagerState.currentPage]?.invoke() },
                enabled = shareActions[pagerState.currentPage] != null,
                colors = AppTheme.colors.iconButtonColors(),
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShare,
                    contentDescription = stringResource(R.string.blessing_share),
                )
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                val config = pageConfigs[page]
                val type = config.type
                val effectType = config.effectType
                val count = uiState.todayCounts[type] ?: 0
                val customization = uiState.tabCustomizations[type] ?: BlessingTabCustomization()
                val remoteText = uiState.remoteTabTexts[type]
                val pageTitle = resolveTabText(
                    customization.title,
                    remoteText?.title,
                    stringResource(config.title),
                )
                val pageSubtitle = resolveTabText(
                    customization.subtitle,
                    remoteText?.subtitle,
                    stringResource(config.subtitle),
                )
                val pageButtonText = resolveTabText(
                    null,
                    remoteText?.buttonText,
                    stringResource(config.buttonText),
                )
                val pageStatTitle = resolveTabText(
                    null,
                    remoteText?.statTitle,
                    stringResource(config.statTitle),
                )
                BlessingPage(
                    imageRes = config.imageRes,
                    title = pageTitle,
                    subtitle = pageSubtitle,
                    actionIcon = config.actionIcon,
                    palette = palettes[page],
                    buttonText = pageButtonText,
                    statTitle = pageStatTitle,
                    count = count,
                    wish = uiState.wishes[type].orEmpty(),
                    customization = customization,
                    isWoodenFish = effectType == BlessingEffectType.WoodenFish,
                    onBless = {
                        val message = if (
                            effectType == BlessingEffectType.WoodenFish &&
                            (count + 1) % WOODEN_FISH_CYCLE == 0
                        ) {
                            meritCycle
                        } else {
                            null
                        }
                        AppToastHost.showBlessingEffect(effectType, message)
                        component.onBless(type, pageTitle)
                        true
                    },
                    onSaveWish = { component.saveWish(type, it) },
                    onSaveTabText = { title, subtitle ->
                        component.saveTabCustomization(type, title, subtitle)
                    },
                    onShare = { bitmap -> component.shareBitmap(bitmap) },
                    isShareSource = page == pagerState.currentPage,
                    onShareActionChanged = { action ->
                        if (action == null) {
                            shareActions.remove(page)
                        } else {
                            shareActions[page] = action
                        }
                    },
                    readOnly = isHistoryMode,
                )
            }

            BlessingBottomTabBar(
                pageConfigs = pageConfigs,
                selectedIndex = pagerState.currentPage,
                palette = selectedPalette,
                tabTitle = { index ->
                    val config = pageConfigs[index]
                    val type = config.type
                    resolveTabText(
                        uiState.tabCustomizations[type]?.title,
                        uiState.remoteTabTexts[type]?.title,
                        stringResource(config.title),
                    )
                },
                tabCount = { index ->
                    uiState.todayCounts[pageConfigs[index].type] ?: 0
                },
                onTabSelected = { index ->
                    pagerScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
            )
        }
    }
}

@Composable
private fun BlessingBottomTabBar(
    pageConfigs: List<BlessingPageConfig>,
    selectedIndex: Int,
    palette: BlessingPalette,
    tabTitle: @Composable (Int) -> String,
    tabCount: (Int) -> Int,
    onTabSelected: (Int) -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem(selectedIndex)
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(pageConfigs.size) { index ->
            val isSelected = index == selectedIndex
            val title = tabTitle(index)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(72.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 2.dp, vertical = 4.dp),
            ) {
                Box {
                    Image(
                        painter = painterResource(pageConfigs[index].tabIconRes),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(30.dp)
                            .graphicsLayer {
                                alpha = if (isSelected) 1f else 0.55f
                            },
                    )
                    if (tabCount(index) > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-1).dp)
                                .size(6.dp)
                                .background(
                                    color = if (isSelected) {
                                        palette.accent
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    },
                                    shape = CircleShape,
                                ),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    color = if (isSelected) {
                        palette.accent
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                    },
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    softWrap = false,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 8.sp,
                        maxFontSize = 12.sp,
                        stepSize = 0.5.sp,
                    ),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(3.dp)
                        .background(
                            color = if (isSelected) {
                                palette.accent
                            } else {
                                Color.Transparent
                            },
                            shape = RoundedCornerShape(50),
                        ),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BlessingPage(
    imageRes: Int,
    title: String,
    subtitle: String,
    actionIcon: ImageVector,
    palette: BlessingPalette,
    buttonText: String,
    statTitle: String,
    count: Int,
    wish: String,
    customization: BlessingTabCustomization,
    isWoodenFish: Boolean,
    onBless: () -> Boolean,
    onSaveWish: (String) -> Unit,
    onSaveTabText: (String, String) -> Unit,
    onShare: (Bitmap) -> Unit,
    isShareSource: Boolean,
    onShareActionChanged: ((() -> Unit)?) -> Unit,
    readOnly: Boolean,
) {
    var clickScale by remember { mutableFloatStateOf(1f) }
    var hammerStrikeTrigger by remember { mutableIntStateOf(0) }
    var isWishEditorVisible by remember { mutableStateOf(false) }
    var wishDraft by remember(wish) { mutableStateOf(wish) }
    var isTabTextEditorVisible by remember { mutableStateOf(false) }
    var titleDraft by remember { mutableStateOf("") }
    var subtitleDraft by remember { mutableStateOf("") }
    val animatedScale by animateFloatAsState(
        targetValue = clickScale,
        animationSpec = tween(durationMillis = 100),
        finishedListener = { clickScale = 1f },
        label = "bless_scale"
    )
    val hammerProgress = remember { Animatable(0f) }
    val captureController = rememberCaptureController()
    val shareScope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val onShareUpdated = rememberUpdatedState(onShare)
    val onShareActionChangedUpdated = rememberUpdatedState(onShareActionChanged)
    val titleInteraction = remember { MutableInteractionSource() }

    LaunchedEffect(hammerStrikeTrigger) {
        if (hammerStrikeTrigger <= 0) return@LaunchedEffect
        hammerProgress.snapTo(0f)
        hammerProgress.animateTo(1f, tween(durationMillis = 75))
        hammerProgress.animateTo(0f, tween(durationMillis = 140))
    }

    DisposableEffect(isShareSource) {
        if (isShareSource) {
            onShareActionChangedUpdated.value {
                shareScope.launch {
                    runCatching { captureController.bitmap() }
                        .onSuccess { onShareUpdated.value(it) }
                }
            }
        }
        onDispose {
            if (isShareSource) onShareActionChangedUpdated.value(null)
        }
    }

    fun openTabTextEditor() {
        titleDraft = customization.title
        subtitleDraft = customization.subtitle
        isTabTextEditorVisible = true
    }

    fun openWishEditor() {
        wishDraft = wish
        isWishEditorVisible = true
    }

    fun triggerBlessing() {
        if (readOnly) return
        haptics.longPress()
        if (onBless()) {
            clickScale = 0.92f
            if (isWoodenFish) hammerStrikeTrigger += 1
        }
    }

    if (isWishEditorVisible) {
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { isWishEditorVisible = false },
            enableGlass = true,
            containerColor = AppTheme.colors.getContainerSurfaceColor(),
            title = {
                Text(
                    text = stringResource(R.string.blessing_wish_edit),
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                GlassOutlinedTextField(
                    value = wishDraft,
                    onValueChange = { if (it.length <= MAX_WISH_LENGTH) wishDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.blessing_wish_input_label)) },
                    minLines = 2,
                    maxLines = 4,
                    colors = AppTheme.colors.getOutlinedTextFieldColors(),
                )
            },
            confirmButton = {
                GlassButton(
                    onClick = {
                        onSaveWish(wishDraft)
                        isWishEditorVisible = false
                    },
                    color = palette.buttonContainer,
                    contentColor = palette.buttonContent,
                ) {
                    Text(stringResource(R.string.blessing_wish_save))
                }
            },
            dismissButton = {
                GlassButton(
                    onClick = { isWishEditorVisible = false },
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Text(stringResource(com.wanbaohe.core.ui.R.string.date_picker_cancel))
                }
            },
        )
    }

    if (isTabTextEditorVisible) {
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { isTabTextEditorVisible = false },
            enableGlass = true,
            containerColor = AppTheme.colors.getContainerSurfaceColor(),
            title = {
                Text(
                    text = stringResource(R.string.blessing_tab_text_edit),
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    GlassOutlinedTextField(
                        value = titleDraft,
                        onValueChange = { if (it.length <= MAX_TITLE_LENGTH) titleDraft = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.blessing_tab_title_label)) },
                        maxLines = 1,
                        colors = AppTheme.colors.getOutlinedTextFieldColors(),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassOutlinedTextField(
                        value = subtitleDraft,
                        onValueChange = { if (it.length <= MAX_SUBTITLE_LENGTH) subtitleDraft = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.blessing_tab_subtitle_label)) },
                        maxLines = 1,
                        colors = AppTheme.colors.getOutlinedTextFieldColors(),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.blessing_tab_text_reset_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                GlassButton(
                    onClick = {
                        onSaveTabText(titleDraft, subtitleDraft)
                        isTabTextEditorVisible = false
                    },
                    color = palette.buttonContainer,
                    contentColor = palette.buttonContent,
                ) {
                    Text(stringResource(R.string.blessing_wish_save))
                }
            },
            dismissButton = {
                GlassButton(
                    onClick = { isTabTextEditorVisible = false },
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Text(stringResource(com.wanbaohe.core.ui.R.string.date_picker_cancel))
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .capturable(captureController)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            color = palette.accent,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .then(
                    if (readOnly) {
                        Modifier
                    } else {
                        Modifier.combinedClickable(
                            interactionSource = titleInteraction,
                            indication = null,
                            onClick = {},
                            onLongClick = ::openTabTextEditor,
                            onLongClickLabel = stringResource(R.string.blessing_tab_text_edit),
                        )
                    }
                ),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .graphicsLayer(
                    scaleX = animatedScale,
                    scaleY = animatedScale,
                )
                .clickable(enabled = !readOnly, onClick = ::triggerBlessing),
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxSize(),
            )
            if (isWoodenFish) {
                Image(
                    painter = painterResource(R.drawable.blessing_wooden_fish_hammer),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 24.dp, end = 20.dp)
                        .fillMaxSize(0.52f)
                        .graphicsLayer {
                            transformOrigin = TransformOrigin(0.2f, 0.85f)
                            rotationZ = 22f - hammerProgress.value * 34f
                            translationX = -hammerProgress.value * 10.dp.toPx()
                            translationY = hammerProgress.value * 16.dp.toPx()
                        },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (readOnly) {
            Box(
                modifier = Modifier.height(52.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.blessing_history_readonly_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            GlassButton(
                onClick = ::triggerBlessing,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .width(ACTION_CARD_WIDTH)
                    .height(52.dp),
                color = palette.buttonContainer,
                contentColor = palette.buttonContent,
                containerAlpha = 0.42f,
                borderWidth = 0.75.dp,
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp),
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(20.dp),
                )
                Text(
                    text = buttonText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        GlassCard(
            onClick = ::openWishEditor,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.width(ACTION_CARD_WIDTH),
            colors = CardDefaults.cardColors(
                containerColor = palette.buttonContainer,
                contentColor = palette.buttonContent,
            ),
            borderWidth = 0.35.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = wish.ifBlank { stringResource(R.string.blessing_wish_placeholder) },
                    modifier = Modifier.weight(1f),
                    color = palette.buttonContent.copy(alpha = if (wish.isBlank()) 0.45f else 0.82f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.blessing_wish_edit),
                    tint = palette.buttonContent.copy(alpha = 0.55f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        GlassCard(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .width(ACTION_CARD_WIDTH)
                .height(STAT_CARD_HEIGHT),
            colors = CardDefaults.cardColors(
                containerColor = palette.buttonContainer,
                contentColor = palette.buttonContent,
            ),
            borderWidth = 0.35.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 14.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = statTitle,
                    modifier = Modifier.fillMaxWidth(),
                    color = palette.buttonContent.copy(alpha = 0.45f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = count.toString(),
                        color = palette.accent.copy(alpha = 0.6f),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.blessing_count_unit),
                        color = palette.buttonContent.copy(alpha = 0.45f),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
        }
    }
}

private data class BlessingPalette(
    val buttonContainer: Color,
    val buttonContent: Color,
    val accent: Color,
)

private data class BlessingPageConfig(
    val type: BlessingType,
    val imageRes: Int,
    val tabIconRes: Int,
    val title: Int,
    val subtitle: Int,
    val buttonText: Int,
    val statTitle: Int,
    val effectType: BlessingEffectType,
    val actionIcon: ImageVector,
)

private val ACTION_CARD_WIDTH = 280.dp
private val STAT_CARD_HEIGHT = 116.dp
private const val WOODEN_FISH_CYCLE = 108
private const val MAX_WISH_LENGTH = 80
private const val MAX_TITLE_LENGTH = 12
private const val MAX_SUBTITLE_LENGTH = 30
