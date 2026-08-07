package com.wanbaohe.idphoto.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wanbaohe.idphoto.R
import com.wanbaohe.idphoto.domain.IdPhotoBackground
import com.t8rin.imagetoolbox.core.resources.icons.Check

/**
 * 背景色选择器
 */
@Composable
fun BackgroundSelector(
    backgrounds: List<IdPhotoBackground> = IdPhotoBackground.PRESETS,
    selectedBackground: IdPhotoBackground,
    onBackgroundSelected: (IdPhotoBackground) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.id_photo_background_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        backgrounds.forEach { background ->
            BackgroundColorItem(
                background = background,
                isSelected = background.color == selectedBackground.color,
                onClick = { onBackgroundSelected(background) }
            )
        }
    }
}

/**
 * 单个背景色选项
 */
@Composable
private fun BackgroundColorItem(
    background: IdPhotoBackground,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(background.getColor(), CircleShape)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                contentDescription = stringResource(R.string.id_photo_selected),
                tint = if (background.color == 0xFFFFFFFF) {
                    Color.Black
                } else {
                    Color.White
                },
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

