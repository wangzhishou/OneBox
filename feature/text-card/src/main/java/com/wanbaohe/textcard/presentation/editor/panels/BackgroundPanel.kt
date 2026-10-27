package com.wanbaohe.textcard.presentation.editor.panels

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.utils.content_pickers.rememberImagePicker
import com.wanbaohe.core.ui.widget.enhanced.EnhancedSliderItem
import com.wanbaohe.core.ui.widget.image.Picture
import com.wanbaohe.core.ui.widget.modifier.meshGradient
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.BackgroundSpec
import com.wanbaohe.textcard.domain.model.GradientPresets
import com.wanbaohe.textcard.domain.model.PaperKind
import com.wanbaohe.textcard.domain.render.MESH_RESOLUTION
import com.wanbaohe.textcard.domain.render.toPointPairs
import com.wanbaohe.textcard.presentation.editor.drawPaperTexture
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent
import kotlin.math.roundToInt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Refresh

@Composable
fun BackgroundPanel(component: TextCardComponent) {
    val background = component.background
    val imagePicker = rememberImagePicker { uri: Uri ->
        component.updateBackground(BackgroundSpec.Image(uri.toString()))
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        PanelTitle(R.string.textcard_paper_background, modifier = Modifier.weight(1f))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable(enabled = !component.remotePapersRefreshing) {
                    component.refreshRemotePapers()
                }
        ) {
            if (component.remotePapersRefreshing) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
            } else {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = stringResource(R.string.textcard_refresh_papers),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        item(key = "none") {
            PaperCell(
                label = stringResource(R.string.textcard_paper_none),
                selected = background is BackgroundSpec.None,
                onClick = { component.updateBackground(BackgroundSpec.None) }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Block,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(PaperKind.entries, key = { "builtin_$it" }) { kind ->
            PaperCell(
                label = stringResource(kind.labelRes),
                selected = background == BackgroundSpec.Paper(kind),
                onClick = { component.updateBackground(BackgroundSpec.Paper(kind)) }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) { drawPaperTexture(kind) }
            }
        }
        items(component.remotePapers, key = { "remote_${it.localPath}" }) { paper ->
            PaperCell(
                label = paper.title,
                selected = (background as? BackgroundSpec.Image)?.uri == paper.localPath,
                onClick = { component.updateBackground(BackgroundSpec.Image(paper.localPath)) }
            ) {
                Picture(
                    model = paper.localPath,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    showTransparencyChecker = false,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    PanelTitle(R.string.textcard_gradient, modifier = Modifier.padding(top = 16.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        GradientPresets.all.forEach { preset ->
            SelectableCell(
                selected = background == preset,
                onClick = { component.updateBackground(preset) },
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .meshGradient(
                            points = preset.toPointPairs(),
                            resolutionX = MESH_RESOLUTION,
                            resolutionY = MESH_RESOLUTION
                        )
                )
            }
        }
    }

    PanelTitle(R.string.textcard_custom_background, modifier = Modifier.padding(top = 16.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        PaperCell(
            label = stringResource(R.string.textcard_pick_from_gallery),
            selected = background is BackgroundSpec.Image,
            onClick = { imagePicker.pickImage() }
        ) {
            if (background is BackgroundSpec.Image) {
                Picture(
                    model = background.uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    showTransparencyChecker = false,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        PaperCell(
            label = stringResource(R.string.textcard_edit_background_advanced),
            selected = false,
            onClick = component::startBackgroundHandoff,
            labelMaxLines = 2
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    EnhancedSliderItem(
        value = component.backgroundOpacity,
        title = stringResource(R.string.textcard_background_opacity),
        valueRange = 0f..1f,
        onValueChange = component::updateBackgroundOpacity,
        onValueChangeFinished = { component.endContinuousEdit() },
        internalStateTransformation = { (it * 100).roundToInt() },
        valueSuffix = "%",
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
internal fun PanelTitle(titleRes: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.titleSmall,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun PaperCell(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    labelMaxLines: Int = 1,
    content: @Composable () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(96.dp)) {
        SelectableCell(selected = selected, onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = labelMaxLines,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun SelectableCell(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.aspectRatio(4f / 3f)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(onClick = onClick)
        ) { content() }
        if (selected) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(18.dp)
            )
        }
    }
}
