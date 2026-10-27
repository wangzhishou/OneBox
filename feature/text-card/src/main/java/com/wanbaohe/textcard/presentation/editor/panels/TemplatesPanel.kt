package com.wanbaohe.textcard.presentation.editor.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wanbaohe.textcard.domain.model.TextCardTemplate
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent
import com.wanbaohe.textcard.presentation.selection.TemplateCard

@Composable
fun TemplatesPanel(component: TextCardComponent) {
    TextCardTemplate.all.chunked(2).forEach { row ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            row.forEach { template ->
                TemplateCard(
                    template = template,
                    selected = component.document.templateId == template.id,
                    onClick = { component.applyTemplate(template) },
                    canvas = component.document.canvas,
                    document = component.document,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
