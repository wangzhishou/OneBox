package com.wanbaohe.textcard.presentation.editor.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassSegmentedButtonRow
import com.wanbaohe.core.ui.widget.glass.GlassSwitch
import com.wanbaohe.core.ui.widget.glass.tintedGlassCardColors
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.TextCardOutputMode
import com.wanbaohe.textcard.presentation.editor.CardCanvasPreview
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent

@Composable
fun PagesPanel(component: TextCardComponent) {
    val layout = component.layout
    Text(
        text = stringResource(R.string.textcard_output_mode),
        style = MaterialTheme.typography.titleSmall
    )
    GlassSegmentedButtonRow(
        options = TextCardOutputMode.entries,
        selectedOption = component.document.outputMode,
        onOptionSelected = component::updateOutputMode,
        label = { mode ->
            Text(
                text = stringResource(
                    when (mode) {
                        TextCardOutputMode.Pages -> R.string.textcard_output_pages
                        TextCardOutputMode.LongImage -> R.string.textcard_output_long_image
                    }
                ),
                textAlign = TextAlign.Center
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = stringResource(R.string.textcard_page_number),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        GlassSwitch(
            checked = component.document.showPageNumber,
            onCheckedChange = component::updateShowPageNumber
        )
    }
    if (component.document.outputMode == TextCardOutputMode.LongImage &&
        layout?.longImage == null
    ) {
        Text(
            text = stringResource(
                R.string.textcard_long_image_too_large,
                layout?.longImageRequiredHeight ?: 0
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
    val pages = layout?.pages.orEmpty()
    Text(
        text = stringResource(R.string.textcard_page_count, pages.size),
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        itemsIndexed(pages, key = { index, _ -> index }) { index, page ->
            val selected = component.currentPage == index
            GlassCard(
                onClick = { component.setCurrentPage(index) },
                colors = if (selected) {
                    tintedGlassCardColors(MaterialTheme.colorScheme.primaryContainer)
                } else null,
                modifier = Modifier.width(92.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CardCanvasPreview(
                        state = page,
                        cornerRadius = 6.dp,
                        interactive = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}
