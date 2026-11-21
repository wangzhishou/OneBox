package com.shifenmiao.app.watermarking.presentation.panels

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.app.watermarking.domain.model.PhotoMount
import com.shifenmiao.app.watermarking.domain.model.PhotoMountRatio
import com.shifenmiao.app.watermarking.domain.model.PhotoMountStyle
import com.shifenmiao.app.watermarking.domain.model.WatermarkFrame
import com.shifenmiao.app.watermarking.domain.model.watermarkCanvasLayout
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRemove
import com.wanbaohe.core.ui.widget.enhanced.EnhancedSliderItem
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassFilterChip
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.tintedGlassCardColors
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
import kotlin.math.roundToInt

private val mountOptionStyle = GlassStyle.Thin
private val selectedMountOptionStyle = GlassStyle.Dense

@Composable
fun PhotoMountPanel(
    mount: PhotoMount?,
    onChange: (PhotoMount?, Boolean) -> Unit,
    onBeginWidthEdit: () -> Unit,
    onEndWidthEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PhotoMountStyle.entries.forEach { style ->
                val selected = mount?.style == style
                GlassCard(
                    onClick = {
                        if (!selected) onChange(PhotoMount(style, ratio = mount?.ratio ?: PhotoMountRatio.NATURAL), true)
                    },
                    modifier = Modifier.weight(1f).semantics { this.selected = selected },
                    colors = if (selected) tintedGlassCardColors(colors.primaryContainer)
                        .copy(contentColor = colors.onPrimaryContainer) else null,
                    style = if (selected) selectedMountOptionStyle else mountOptionStyle,
                    borderWidth = 0.dp,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        PhotoMountThumbnail(PhotoMount(style), 54.dp)
                        Text(
                            stringResource(style.titleRes()),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
        if (mount == null) return@Column
        EnhancedSliderItem(
            value = mount.widthRatio.coerceIn(PhotoMount.MIN_WIDTH, PhotoMount.MAX_WIDTH),
            title = stringResource(R.string.watermark_mount_width),
            valueRange = PhotoMount.MIN_WIDTH..PhotoMount.MAX_WIDTH,
            internalStateTransformation = { (it * 100).roundToInt() / 100f },
            onValueChange = {
                onBeginWidthEdit()
                onChange(mount.copy(widthRatio = it), false)
            },
            onValueChangeFinished = { onEndWidthEdit() },
            shape = ShapeDefaults.large,
            containerColor = Color.Unspecified,
        )
        Text(stringResource(R.string.aspect_ratio), style = MaterialTheme.typography.labelLarge)
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            PhotoMountRatio.entries.forEach { ratio ->
                GlassFilterChip(
                    selected = mount.ratio == ratio,
                    onClick = { onChange(mount.copy(ratio = ratio), true) },
                    colors = FilterChipDefaults.filterChipColors(
                        labelColor = colors.onSurfaceVariant,
                        selectedLabelColor = colors.onPrimaryContainer,
                    ),
                    style = mountOptionStyle,
                    selectedSurfaceStyle = selectedMountOptionStyle,
                    glassContainerColor = colors.surfaceContainer,
                    glassSelectedContainerColor = colors.primaryContainer,
                    glassBorderWidth = 0.dp,
                    elevation = null,
                    label = {
                        Text(
                            if (ratio == PhotoMountRatio.NATURAL) stringResource(R.string.watermark_mount_natural_ratio)
                            else "${ratio.width}:${ratio.height}",
                        )
                    },
                )
            }
        }
        OneSecondaryButton(
            text = stringResource(R.string.watermark_mount_remove),
            leadingIcon = Icons.Outlined.LineRemove,
            onClick = { onChange(null, true) },
        )
    }
}

@Composable
internal fun PhotoMountThumbnail(mount: PhotoMount, height: androidx.compose.ui.unit.Dp) {
    val photoWidth = 400
    val photoHeight = 270
    val layout = watermarkCanvasLayout(photoWidth, photoHeight, WatermarkFrame.InPlace, mount)
    BoxWithConstraints(Modifier.fillMaxWidth().height(height)) {
        val density = LocalDensity.current
        val scale = with(density) {
            minOf(maxWidth.toPx() * 0.91f / layout.width, maxHeight.toPx() * 0.94f / layout.height)
        }
        Box(
            Modifier.align(androidx.compose.ui.Alignment.Center)
                .size(with(density) { (layout.width * scale).toDp() }, with(density) { (layout.height * scale).toDp() })
                .drawBehind {
                    drawRect(Color(mount.style.color))
                    mount.style.keylineColor?.let { color ->
                        val line = (layout.keylineWidth * scale).coerceAtLeast(0.5f)
                        drawRect(
                            Color(color),
                            Offset(layout.contentLeft * scale - line, layout.contentTop * scale - line),
                            Size(photoWidth * scale + 2 * line, photoHeight * scale + 2 * line),
                        )
                    }
                },
        ) {
            Image(
                painterResource(R.drawable.filter_preview_source),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.absoluteOffset {
                    androidx.compose.ui.unit.IntOffset(
                        (layout.contentLeft * scale).roundToInt(), (layout.contentTop * scale).roundToInt(),
                    )
                }.size(
                    with(density) { (photoWidth * scale).toDp() },
                    with(density) { (photoHeight * scale).toDp() },
                ),
            )
        }
    }
}

private fun PhotoMountStyle.titleRes(): Int = when (this) {
    PhotoMountStyle.GALLERY -> R.string.watermark_mount_gallery
    PhotoMountStyle.DARKROOM -> R.string.watermark_mount_darkroom
    PhotoMountStyle.INSTANT -> R.string.watermark_mount_instant
}
