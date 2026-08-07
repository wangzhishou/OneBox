package com.wanbaohe.profile.settingItem

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.other.ExpandableItem
import com.t8rin.imagetoolbox.core.ui.widget.text.TitleItem

@Composable
fun ProfileGroupItem(
    icon: ImageVector,
    text: String,
    initialState: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val pressed by interactionSource.collectIsPressedAsState()

    val cornerSize by animateDpAsState(
        if (pressed) 8.dp
        else 20.dp, label = "cornerSize"
    )
    ExpandableItem(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        visibleContent = {
            TitleItem(
                modifier = Modifier.padding(start = 8.dp),
                icon = icon,
                text = text
            )
        },
        shape = RoundedCornerShape(cornerSize),
        expandableContent = { content() },
        initialState = initialState,
        interactionSource = interactionSource
    )
}