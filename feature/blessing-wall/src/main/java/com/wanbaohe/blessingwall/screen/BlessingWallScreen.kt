package com.wanbaohe.blessingwall.screen

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.utils.blessing.BlessingEffectType
import com.t8rin.imagetoolbox.core.ui.utils.capturable.capturable
import com.t8rin.imagetoolbox.core.ui.utils.capturable.rememberCaptureController
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.wanbaohe.blessingwall.R
import com.wanbaohe.blessingwall.component.BlessingWallComponent
import com.wanbaohe.blessingwall.model.BlessingTabCustomization
import com.wanbaohe.blessingwall.model.BlessingType
import com.wanbaohe.blessingwall.model.resolveTabText
import kotlinx.coroutines.launch
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
    val pagerState = rememberPagerState(
        initialPage = component.initialPage,
        pageCount = { 4 },
    )

    val types = BlessingType.entries
    val pageTitles = listOf(
        stringResource(R.string.blessing_tab_wooden_fish),
        stringResource(R.string.blessing_tab_wealth_god),
        stringResource(R.string.blessing_tab_guanyin),
        stringResource(R.string.blessing_tab_incense),
    )
    val pageSubtitles = listOf(
        stringResource(R.string.blessing_subtitle_wooden_fish),
        stringResource(R.string.blessing_subtitle_wealth_god),
        stringResource(R.string.blessing_subtitle_guanyin),
        stringResource(R.string.blessing_subtitle_incense),
    )
    val buttonTexts = listOf(
        stringResource(R.string.blessing_btn_wooden_fish),
        stringResource(R.string.blessing_btn_wealth_god),
        stringResource(R.string.blessing_btn_guanyin),
        stringResource(R.string.blessing_btn_incense),
    )
    val statTitles = listOf(
        stringResource(R.string.blessing_stat_wooden_fish),
        stringResource(R.string.blessing_stat_wealth_god),
        stringResource(R.string.blessing_stat_guanyin),
        stringResource(R.string.blessing_stat_incense),
    )
    val imageResources = listOf(
        R.drawable.blessing_wooden_fish,
        R.drawable.blessing_wealth_god,
        R.drawable.blessing_guanyin,
        R.drawable.blessing_incense,
    )
    val actionIcons = listOf(
        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTouchApp,
        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMagic,
        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFavorite,
        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMeditation,
    )
    val effectTypes = listOf(
        BlessingEffectType.WoodenFish,
        BlessingEffectType.WealthGod,
        BlessingEffectType.Guanyin,
        BlessingEffectType.Incense,
    )
    val isBlessingEffectActive = AppToastHost.blessingEffectState.isActive
    val colorScheme = MaterialTheme.colorScheme
    val palettes = listOf(
        BlessingPalette(
            buttonContainer = colorScheme.primaryContainer,
            buttonContent = colorScheme.onPrimaryContainer,
            accent = colorScheme.primary,
        ),
        BlessingPalette(
            buttonContainer = colorScheme.secondaryContainer,
            buttonContent = colorScheme.onSecondaryContainer,
            accent = colorScheme.secondary,
        ),
        BlessingPalette(
            buttonContainer = colorScheme.tertiaryContainer,
            buttonContent = colorScheme.onTertiaryContainer,
            accent = colorScheme.tertiary,
        ),
        BlessingPalette(
            buttonContainer = colorScheme.surfaceContainer,
            buttonContent = colorScheme.onSurface,
            accent = colorScheme.onSurface,
        ),
    )
    val selectedPalette = palettes[pagerState.currentPage]

    LaunchedEffect(pagerState.currentPage) {
        component.onPageChanged(pagerState.currentPage)
    }

    BaseScreen(
        title = if (isHistoryMode) component.targetDate else "",
        onGoBack = component.onGoBack,
        actions = {
            if (!isHistoryMode) {
                IconButton(
                    onClick = component::navigateToRecord,
                    colors = AppTheme.colors.iconButtonColors()
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                        contentDescription = stringResource(R.string.blessing_record_title)
                    )
                }
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
                val type = types[page]
                val effectType = effectTypes[page]
                val count = uiState.todayCounts[type] ?: 0
                val customization = uiState.tabCustomizations[type] ?: BlessingTabCustomization()
                val remoteText = uiState.remoteTabTexts[type]
                val pageTitle = resolveTabText(customization.title, remoteText?.title, pageTitles[page])
                BlessingPage(
                    imageRes = imageResources[page],
                    title = pageTitle,
                    subtitle = resolveTabText(customization.subtitle, remoteText?.subtitle, pageSubtitles[page]),
                    actionIcon = actionIcons[page],
                    palette = palettes[page],
                    buttonText = resolveTabText(null, remoteText?.buttonText, buttonTexts[page]),
                    statTitle = resolveTabText(null, remoteText?.statTitle, statTitles[page]),
                    count = count,
                    wish = uiState.wishes[type].orEmpty(),
                    customization = customization,
                    isWoodenFish = effectType == BlessingEffectType.WoodenFish,
                    isBlessEnabled = effectType == BlessingEffectType.WoodenFish ||
                            !isBlessingEffectActive,
                    onBless = {
                        AppToastHost.showBlessingEffect(effectType).also { accepted ->
                            if (accepted) component.onBless(type, pageTitle)
                        }
                    },
                    onSaveWish = { component.saveWish(type, it) },
                    onSaveTabText = { title, subtitle ->
                        component.saveTabCustomization(type, title, subtitle)
                    },
                    soundEnabled = soundEnabled,
                    onToggleSound = component::toggleSoundEnabled,
                    onShare = { bitmap -> component.shareBitmap(bitmap) },
                    readOnly = isHistoryMode,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val isSelected = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) {
                                    selectedPalette.accent
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                }
                            )
                    )
                }
            }
        }
    }
}

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
    isBlessEnabled: Boolean,
    onBless: () -> Boolean,
    onSaveWish: (String) -> Unit,
    onSaveTabText: (String, String) -> Unit,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
    onShare: (Bitmap) -> Unit,
    readOnly: Boolean,
) {
    var clickScale by remember { mutableFloatStateOf(1f) }
    var hammerStrikeTrigger by remember { mutableIntStateOf(0) }
    var isWishVisible by remember { mutableStateOf(false) }
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
    val cardRotation by animateFloatAsState(
        targetValue = if (isWishVisible) 180f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "wish_card_rotation",
    )
    val density = LocalDensity.current.density
    val hammerProgress = remember { Animatable(0f) }
    val captureController = rememberCaptureController()
    val shareScope = rememberCoroutineScope()

    LaunchedEffect(hammerStrikeTrigger) {
        if (hammerStrikeTrigger <= 0) return@LaunchedEffect
        hammerProgress.snapTo(0f)
        hammerProgress.animateTo(1f, tween(durationMillis = 75))
        hammerProgress.animateTo(0f, tween(durationMillis = 140))
    }

    fun triggerBlessing() {
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
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Text(stringResource(com.t8rin.imagetoolbox.core.ui.R.string.date_picker_cancel))
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
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Text(stringResource(com.t8rin.imagetoolbox.core.ui.R.string.date_picker_cancel))
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
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            color = palette.accent,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        if (!readOnly) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        titleDraft = customization.title
                        subtitleDraft = customization.subtitle
                        isTabTextEditorVisible = true
                    },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.blessing_tab_text_edit),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f),
                        modifier = Modifier.size(16.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier.size(28.dp),
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (soundEnabled) 0.32f else 0.18f
                        ),
                        modifier = Modifier.size(16.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = {
                        shareScope.launch {
                            runCatching { captureController.bitmap() }
                                .onSuccess(onShare)
                        }
                    },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShare,
                        contentDescription = stringResource(R.string.blessing_share),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .graphicsLayer(
                    scaleX = animatedScale,
                    scaleY = animatedScale,
                )
                .clickable(enabled = isBlessEnabled && !readOnly, onClick = ::triggerBlessing),
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
                enabled = isBlessEnabled,
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

        Spacer(modifier = Modifier.height(16.dp))

        GlassCard(
            onClick = { isWishVisible = !isWishVisible },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .width(ACTION_CARD_WIDTH)
                .height(STAT_CARD_HEIGHT)
                .graphicsLayer {
                    rotationY = cardRotation
                    cameraDistance = 12f * density
                },
            colors = CardDefaults.cardColors(
                containerColor = palette.buttonContainer,
                contentColor = palette.buttonContent,
            ),
            containerAlpha = 0.025f,
            borderWidth = 0.35.dp,
        ) {
            if (cardRotation <= 90f) {
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
            } else {
                Column(
                    modifier = Modifier
                        .graphicsLayer { rotationY = 180f }
                        .fillMaxSize()
                        .padding(vertical = 14.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = wish.ifBlank {
                            stringResource(R.string.blessing_wish_placeholder)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        color = palette.buttonContent.copy(
                            alpha = if (wish.isBlank()) 0.35f else 0.55f
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (!readOnly) {
                        IconButton(
                            onClick = {
                                wishDraft = wish
                                isWishEditorVisible = true
                            },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                                contentDescription = stringResource(R.string.blessing_wish_edit),
                                tint = palette.buttonContent.copy(alpha = 0.45f),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
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

private val ACTION_CARD_WIDTH = 280.dp
private val STAT_CARD_HEIGHT = 116.dp
private const val MAX_WISH_LENGTH = 80
private const val MAX_TITLE_LENGTH = 12
private const val MAX_SUBTITLE_LENGTH = 30
