package com.wanbaohe.textcard.presentation.selection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.button.ConfirmButton
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.CanvasSpec
import com.wanbaohe.textcard.domain.model.ElementLayer
import com.wanbaohe.textcard.domain.model.GradientPresets
import com.wanbaohe.textcard.domain.model.TextBlock
import com.wanbaohe.textcard.domain.model.TextCardRenderState
import com.wanbaohe.textcard.domain.render.CardLayout
import com.wanbaohe.textcard.presentation.editor.CardCanvasPreview
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent

/**
 * 选择画布页(设计稿 00):两个平台卡片(带 mini 预览)+ 底部「开始制作」。
 */
@Composable
fun CanvasSelectContent(
    component: TextCardComponent,
) {
    BaseScreen(
        title = stringResource(R.string.textcard_select_canvas_title),
        onGoBack = component.onGoBack,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.textcard_select_canvas_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 16.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    CanvasSpec.entries.forEach { spec ->
                        CanvasCard(
                            spec = spec,
                            selected = component.pendingCanvas == spec,
                            onClick = { component.selectPendingCanvas(spec) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            ConfirmButton(
                text = stringResource(R.string.textcard_start_create),
                onClick = component::startEditing,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }
    )
}

@Composable
private fun CanvasCard(
    spec: CanvasSpec,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassCard(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
        Text(
            text = stringResource(spec.titleRes),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(spec.descRes),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )
        // mini 预览:默认渐变 + 默认文案,与进入编辑后的初始卡片一致
        val previewBlocks = listOf(
            TextBlock(
                content = stringResource(R.string.textcard_default_title),
                baseSizeRatio = CardLayout.TITLE_BASE_SIZE_RATIO,
                baseTopRatio = CardLayout.CONTENT_PADDING_RATIO,
                isBold = true
            ),
            TextBlock(
                content = stringResource(R.string.textcard_default_body),
                baseSizeRatio = CardLayout.BODY_BASE_SIZE_RATIO,
                baseTopRatio = CardLayout.BODY_BASE_TOP_RATIO
            )
        )
        CardCanvasPreview(
            state = TextCardRenderState(
                canvas = spec,
                background = GradientPresets.default,
                textBlocks = previewBlocks,
                layers = previewBlocks.map {
                    ElementLayer(it.id, ElementLayer.Kind.Text)
                }
            ),
            cornerRadius = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(spec.aspectRatio)
        )
        Text(
            text = spec.ratioLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
        )
        }
    }
}
