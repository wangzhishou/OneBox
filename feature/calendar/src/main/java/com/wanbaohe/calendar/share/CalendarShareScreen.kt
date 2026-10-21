package com.wanbaohe.calendar.share

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMagic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import android.widget.Toast
import com.wanbaohe.calendar.R
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.wanbaohe.core.ui.widget.enhanced.EnhancedTopAppBar
import com.wanbaohe.core.ui.widget.enhanced.EnhancedTopAppBarType
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.glassBackground
import com.wanbaohe.core.ui.widget.other.TopAppBarEmoji
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * 日历分享定制页。
 *
 * 顶部用与 BaseScreen 同一套 EnhancedTopAppBar + TopAppBarEmoji，右上角保留主题快捷切换；
 * 主题一变，卡片配色（跟随主题时）会自动重新渲染。页面本身只负责预览、配色/显示内容、
 * 心语与保存/分享。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalendarShareScreen(component: CalendarShareComponent) {
    val state by component.uiState.collectAsState()
    val context = LocalContext.current

    val saveFailed = stringResource(R.string.calendar_share_save_failed)

    LaunchedEffect(Unit) {
        component.events.collect { event ->
            val message = when (event) {
                CalendarShareEvent.SaveFailed -> saveFailed
                is CalendarShareEvent.Message -> event.text
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val labels = CalendarShareLabels(
        lunarPrefix = stringResource(R.string.calendar_share_lunar_prefix),
        yiSeal = stringResource(R.string.calendar_share_yi_seal),
        jiSeal = stringResource(R.string.calendar_share_ji_seal),
        blessing = stringResource(R.string.calendar_share_blessing),
        brand = stringResource(R.string.calendar_share_brand),
        yearSuffix = stringResource(R.string.calendar_share_year_suffix),
        monthSuffix = stringResource(R.string.calendar_share_month_suffix),
        daySuffix = stringResource(R.string.calendar_share_day_suffix),
        todayPrefix = stringResource(R.string.calendar_share_today_prefix),
        selectedDayPrefix = stringResource(R.string.calendar_share_selected_day_prefix),
        auspiciousTitle = stringResource(R.string.calendar_share_auspicious_title),
        upcomingPrefix = stringResource(R.string.calendar_share_upcoming_prefix),
        noteTitle = stringResource(R.string.calendar_share_note_title),
        airPrefix = stringResource(R.string.calendar_share_air_prefix),
        weatherTitle = stringResource(R.string.calendar_share_weather_title),
    )

    val colorScheme = MaterialTheme.colorScheme
    val palette = remember(state.colorStyle, colorScheme) {
        CalendarSharePalette.from(state.colorStyle, colorScheme)
    }
    val options = remember(
        state.showTodayFestival,
        state.showUpcomingFestivals,
        state.showWeather,
        state.showNote
    ) {
        CalendarShareRenderOptions(
            showTodayFestival = state.showTodayFestival,
            showUpcomingFestivals = state.showUpcomingFestivals,
            showWeather = state.showWeather,
            showNote = state.showNote,
        )
    }

    val shareData = remember(
        state.year,
        state.month,
        state.day,
        state.lunarDate,
        state.yiJi,
        state.luckyHours,
        state.todayFestivals,
        state.upcomingFestivals,
        state.weather,
        state.note,
        labels,
        palette,
    ) {
        state.lunarDate?.let { lunar ->
            buildCalendarShareCardData(
                year = state.year,
                month = state.month,
                day = state.day,
                lunar = lunar,
                yiJi = state.yiJi,
                luckyHours = state.luckyHours,
                todayFestivals = state.todayFestivals,
                upcomingFestivals = state.upcomingFestivals,
                weather = state.weather,
                note = state.note,
                labels = labels,
                palette = palette,
            )
        }
    }

    val fontFamilyResolver = LocalFontFamilyResolver.current
    var preview by remember { mutableStateOf<ImageBitmap?>(null) }
    var isRendering by remember { mutableStateOf(false) }
    var renderFailed by remember { mutableStateOf(false) }
    var renderAttempt by remember { mutableIntStateOf(0) }

    LaunchedEffect(shareData, options, renderAttempt, fontFamilyResolver) {
        val cardData = shareData ?: return@LaunchedEffect
        delay(140)
        isRendering = true
        val result = try {
            withContext(Dispatchers.Default) {
                val textMeasurer = TextMeasurer(
                    defaultFontFamilyResolver = fontFamilyResolver,
                    defaultDensity = Density(1f),
                    defaultLayoutDirection = LayoutDirection.Ltr,
                    cacheSize = 128,
                )
                CalendarShareCardRenderer.render(
                    data = cardData,
                    options = options,
                    textMeasurer = textMeasurer,
                )
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Throwable) {
            null
        }
        renderFailed = result == null
        if (result != null) preview = result
        isRendering = false
    }

    val previewBitmap = preview

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        EnhancedTopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.calendar_share_page_title),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            navigationIcon = {
                IconButton(onClick = { component.onGoBack() }) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = stringResource(com.shifenmiao.core.R.string.back_button)
                    )
                }
            },
            actions = { TopAppBarEmoji() },
            isGlassActive = true,
            drawHorizontalStroke = false,
            type = EnhancedTopAppBarType.Center
        )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val previewAspect = previewBitmap
                    ?.let { it.width.toFloat() / it.height.toFloat() }
                    ?: (1080f / 1600f)

                PreviewCard(
                    image = previewBitmap,
                    isRendering = isRendering,
                    renderFailed = renderFailed,
                    aspectRatio = previewAspect,
                    onRetry = { renderAttempt += 1 }
                )

                previewBitmap?.let { image ->
                    Text(
                        text = stringResource(
                            R.string.calendar_share_preview_size,
                            image.width,
                            image.height
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 6.dp)
                    )
                    Text(
                        text = stringResource(R.string.calendar_share_transparent_background),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 2.dp)
                    )
                }

                SectionTitle(stringResource(R.string.calendar_share_color_title))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalendarShareColorStyle.entries.forEach { style ->
                        val selected = state.colorStyle == style
                        val swatch = remember(style, colorScheme) {
                            CalendarSharePalette.from(style, colorScheme).accent
                        }
                        FilterChip(
                            selected = selected,
                            onClick = { component.setColorStyle(style) },
                            label = { Text(stringResource(style.labelRes)) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(swatch)
                                )
                            },
                            colors = chipColors()
                        )
                    }
                }

                SectionTitle(stringResource(R.string.calendar_share_content_title))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.showTodayFestival,
                        onClick = { component.setShowTodayFestival(!state.showTodayFestival) },
                        label = { Text(stringResource(R.string.calendar_share_toggle_today)) },
                        colors = chipColors()
                    )
                    FilterChip(
                        selected = state.showUpcomingFestivals,
                        onClick = { component.setShowUpcomingFestivals(!state.showUpcomingFestivals) },
                        label = { Text(stringResource(R.string.calendar_share_toggle_upcoming)) },
                        colors = chipColors()
                    )
                    FilterChip(
                        selected = state.showWeather,
                        onClick = { component.setShowWeather(!state.showWeather) },
                        enabled = state.isToday &&
                            (state.weather != null || state.isWeatherLoading),
                        label = {
                            Text(
                                text = if (state.isWeatherLoading) {
                                    stringResource(R.string.calendar_share_weather_loading)
                                } else {
                                    stringResource(R.string.calendar_share_toggle_weather)
                                }
                            )
                        },
                        colors = chipColors()
                    )
                    FilterChip(
                        selected = state.showNote,
                        onClick = { component.setShowNote(!state.showNote) },
                        label = { Text(stringResource(R.string.calendar_share_toggle_note)) },
                        colors = chipColors()
                    )
                }

                if (!state.isToday) {
                    Text(
                        text = stringResource(R.string.calendar_share_weather_today_only),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (state.weather == null && !state.isWeatherLoading) {
                    Text(
                        text = stringResource(R.string.calendar_share_weather_unavailable),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                SectionTitle(stringResource(R.string.calendar_share_note_title_section))
                GlassOutlinedTextField(
                    value = state.note,
                    onValueChange = {
                        if (!state.showNote) component.setShowNote(true)
                        component.setNote(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.calendar_share_note_label)) },
                    placeholder = { Text(stringResource(R.string.calendar_share_note_placeholder)) },
                    supportingText = {
                        Text(
                            text = stringResource(
                                R.string.calendar_share_note_counter,
                                state.note.length,
                                MAX_NOTE_LENGTH
                            )
                        )
                    },
                    minLines = 2,
                    maxLines = 3,
                )
                FilledTonalButton(
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    onClick = {
                        if (!state.showNote) component.setShowNote(true)
                        component.generateNote()
                    },
                    enabled = !state.isGeneratingNote,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 6.dp)
                ) {
                    if (state.isGeneratingNote) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.LineMagic,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (state.isGeneratingNote) {
                            stringResource(R.string.calendar_share_ai_generating)
                        } else {
                            stringResource(R.string.calendar_share_ai_write)
                        }
                    )
                }
                Text(
                    text = stringResource(R.string.calendar_share_ai_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

        BottomActions(
            enabled = previewBitmap != null,
            isSaving = state.isSaving,
            onSave = { previewBitmap?.asAndroidBitmap()?.let(component::saveImage) },
            onShare = { previewBitmap?.asAndroidBitmap()?.let(component::shareBitmap) }
        )
    }
}

@Composable
private fun Checkerboard(modifier: Modifier = Modifier) {
    val light = MaterialTheme.colorScheme.surfaceContainerLow
    val dark = MaterialTheme.colorScheme.surfaceContainer
    Canvas(modifier = modifier) {
        drawRect(color = dark)
        val cell = 10.dp.toPx()
        var row = 0
        var y = 0f
        while (y < size.height) {
            var x = if (row % 2 == 0) 0f else cell
            while (x < size.width) {
                drawRect(
                    color = light,
                    topLeft = Offset(x, y),
                    size = Size(cell, cell)
                )
                x += cell * 2f
            }
            y += cell
            row++
        }
    }
}

@Composable
private fun PreviewCard(
    image: ImageBitmap?,
    isRendering: Boolean,
    renderFailed: Boolean,
    aspectRatio: Float,
    onRetry: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        val cardWidth = minOf(maxWidth * 0.94f, 420.dp)
        Box(
            modifier = Modifier
                .width(cardWidth)
                .aspectRatio(aspectRatio)
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(18.dp),
                    ambientColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.35f),
                    spotColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.4f)
                )
                .clip(RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
        Checkerboard(Modifier.fillMaxSize())
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = stringResource(R.string.calendar_share_page_title),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        } else if (renderFailed) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.calendar_share_render_failed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilledTonalButton(onClick = onRetry, modifier = Modifier.padding(top = 8.dp)) {
                    Text(stringResource(R.string.calendar_share_retry))
                }
            }
        } else {
            CircularProgressIndicator(strokeWidth = 2.dp)
        }

        if (isRendering && image != null) {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun BottomActions(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    isSaving: Boolean,
    onSave: () -> Unit,
    onShare: () -> Unit,
) {
    val glassEnabled = LocalSettingsState.current.isGlassAlphaEnabled
    val shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 2.dp)
            .then(
                if (glassEnabled) {
                    Modifier.glassBackground(
                        style = GlassStyle.Thin,
                        shape = shape,
                        color = MaterialTheme.colorScheme.surface
                    )
                } else {
                    Modifier.background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.18f),
                        shape = shape
                    )
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onSave,
                enabled = enabled && !isSaving,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.LineSave,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.calendar_share_save))
            }
            Button(
                onClick = onShare,
                enabled = enabled,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.LineShare,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.calendar_share_share))
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 18.dp, bottom = 6.dp)
    )
}

@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
    containerColor = MaterialTheme.colorScheme.surfaceContainer,
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
)
