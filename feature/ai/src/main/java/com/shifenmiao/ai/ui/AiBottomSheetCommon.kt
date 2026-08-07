package com.shifenmiao.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTextFieldVisualPreset
import com.t8rin.imagetoolbox.core.resources.icons.Search
import com.t8rin.imagetoolbox.core.resources.icons.Close

internal object AiBottomSheetDefaults {
    val HorizontalPadding = 20.dp
    val SearchShape = RoundedCornerShape(18.dp)
    val ChipShape = RoundedCornerShape(14.dp)
    val ContentMinHeight = 420.dp
    val ContentMaxHeight = 720.dp
}

@Composable
internal fun AiBottomSheetHeader(
    title: String,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.size(40.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = onClose) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun AiBottomSheetSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    GlassOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        shape = AiBottomSheetDefaults.SearchShape,
        visualPreset = GlassTextFieldVisualPreset.Expressive,
        leadingIcon = {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        label = {
            Text(text = placeholder)
        },
        colors = AppTheme.colors.getOutlinedTextFieldColors()
    )
}

@Composable
internal fun AiBottomSheetFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(AiBottomSheetDefaults.ChipShape)
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
