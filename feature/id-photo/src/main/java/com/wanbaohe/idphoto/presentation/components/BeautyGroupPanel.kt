package com.wanbaohe.idphoto.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.model.imageprocess.RetouchParams
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMinus
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalIconButton
import com.wanbaohe.idphoto.domain.BeautyParamGroup
import com.wanbaohe.idphoto.domain.BeautyParamSpec
import java.util.Locale
import kotlin.math.roundToInt

/** 单项步进调节的步长 */
private const val DRAFT_STEP = 0.1f

/** 参数卡片宽度 */
private val CARD_WIDTH = 168.dp

/**
 * 美化参数分组面板(与「一键美化」平级的 tab):
 * 参数项横向滚动,每项用 +/- 步进调节草稿,卡片小字说明数值含义。
 * 草稿只改本地状态;「应用」/「重置」在底部操作栏,统一对所有分组生效。
 */
@Composable
fun BeautyGroupPanel(
    group: BeautyParamGroup,
    draft: RetouchParams,
    isProcessing: Boolean,
    onValueChange: (BeautyParamSpec, Float?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(group.specs, key = { it.key }) { spec ->
            BeautyParamCard(
                spec = spec,
                value = draft[spec.key],
                enabled = !isProcessing,
                onValueChange = { onValueChange(spec, it) }
            )
        }
    }
}

@Composable
private fun BeautyParamCard(
    spec: BeautyParamSpec,
    value: Float?,
    enabled: Boolean,
    onValueChange: (Float?) -> Unit
) {
    GlassSurface(
        modifier = Modifier.width(CARD_WIDTH),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(spec.labelRes),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(spec.descRes),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
            if (spec.isSwitch) {
                Row(
                    modifier = Modifier
                        .width(CARD_WIDTH - 20.dp)
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    GlassSwitch(
                        checked = value == 1f,
                        onCheckedChange = { onValueChange(if (it) 1f else null) },
                        enabled = enabled
                    )
                }
            } else {
                StepperRow(
                    value = value ?: 0f,
                    min = spec.min,
                    max = spec.max,
                    enabled = enabled,
                    onValueChange = onValueChange,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun StepperRow(
    value: Float,
    min: Float,
    max: Float,
    enabled: Boolean,
    onValueChange: (Float?) -> Unit,
    modifier: Modifier = Modifier
) {
    fun stepped(delta: Float) {
        val next = ((value + delta) * 100).roundToInt() / 100f
        // 回到 0 视为不启用该参数
        onValueChange(if (next == 0f) null else next.coerceIn(min, max))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.width(CARD_WIDTH - 20.dp)
    ) {
        GlassTonalIconButton(
            onClick = { stepped(-DRAFT_STEP) },
            enabled = enabled && value > min,
            modifier = Modifier.size(26.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.LineMinus,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = String.format(Locale.US, "%.1f", value),
            style = MaterialTheme.typography.labelMedium,
            color = if (value == 0f) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
        GlassTonalIconButton(
            onClick = { stepped(DRAFT_STEP) },
            enabled = enabled && value < max,
            modifier = Modifier.size(26.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
