package com.shifenmiao.base.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.model.search.SuggestionModel
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassRegular
import com.t8rin.imagetoolbox.core.resources.icons.Close


@Composable
fun CancelableChip(
    modifier: Modifier = Modifier,
    suggestion: SuggestionModel,
    @DrawableRes drawableRes: Int = -1,
    onClick: ((SuggestionModel) -> Unit)? = null,
    onCancel: ((SuggestionModel) -> Unit)? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh
) {
    val shape = MaterialTheme.shapes.large
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(shape)
            .glassRegular(
                shape = shape,
                color = backgroundColor,
            )
            .clickable {
                onClick?.run {
                    invoke(suggestion)
                }
            }
            .padding(
                vertical = 12.dp,
                horizontal = AppTheme.dimens.paddingNormal
            )
    ) {

        if (drawableRes != -1) {
            Image(
                painter = painterResource(drawableRes),
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(16.dp)
                    .clip(CircleShape),
                contentDescription = null
            )
        }

        Text(
            text = suggestion.tag,
            maxLines = 1,
            minLines = 1,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .padding(end = 12.dp)
                .wrapContentWidth(),
            color = textColor
        )
        Spacer(modifier = Modifier.width(AppTheme.dimens.paddingNormal))
        IconButton(
            onClick = {
                onCancel?.run {
                    invoke(suggestion)
                }
            },
            modifier = Modifier
                .size(16.dp)
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                tint = textColor,
                contentDescription = null
            )
        }
    }
}