package com.wanbaohe.profile.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.GrayHorizontalDivider

@Composable
fun DividerListItem(
    headlineText: String,
    trailingText: String,
    trailingIcon: ImageVector,
    onTrailingIconClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val grayColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    ListItem(
        modifier = Modifier.clickable { onClick() },
        colors = ListItemDefaults.colors(
            containerColor = Color.Unspecified,
            trailingIconColor = grayColor
        ),
        headlineContent = {
            Text(
                text = headlineText,
                textAlign = TextAlign.Start
            )
        },
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    trailingText,
                    textAlign = TextAlign.End
                )
                IconButton(onClick = onTrailingIconClick) {
                    Icon(
                        modifier = Modifier.width(16.dp),
                        imageVector = trailingIcon,
                        contentDescription = null

                    )
                }
            }
        }
    )
    GrayHorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}