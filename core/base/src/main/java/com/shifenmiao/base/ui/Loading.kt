package com.shifenmiao.base.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.core.ui.loading.DotDanceLoading
import com.shifenmiao.core.ui.skeleton.Skeleton
import com.shifenmiao.theme.AppTheme

@Composable
fun AILoadingRow() {
    Row(
        modifier = Modifier.padding(
            horizontal = AppTheme.dimens.paddingNormal,
            vertical = AppTheme.dimens.paddingNormal
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSmall)
    ) {
        DotDanceLoading()
        Text(
            text = stringResource(id = R.string.ai_loading),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CenterLoadingBox() {
    val isActive by remember { mutableStateOf(true) }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(36.dp))
        Row {
            Skeleton.Square(isActive)
            Spacer(modifier = Modifier.padding(4.dp))
            Column {
                Spacer(modifier = Modifier.padding(8.dp))
                Skeleton.RectangleLineLong(isActive)
                Spacer(modifier = Modifier.padding(4.dp))
                Skeleton.RectangleLineShort(isActive)
            }
        }
        Spacer(modifier = Modifier.height(36.dp))
        Row {
            Skeleton.Square(isActive)
            Spacer(modifier = Modifier.padding(4.dp))
            Column {
                Spacer(modifier = Modifier.padding(8.dp))
                Skeleton.RectangleLineLong(isActive)
                Spacer(modifier = Modifier.padding(4.dp))
                Skeleton.RectangleLineShort(isActive)
            }
        }
    }
}
