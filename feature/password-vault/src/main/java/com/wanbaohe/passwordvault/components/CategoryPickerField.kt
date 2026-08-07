package com.wanbaohe.passwordvault.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassFilterChip
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassIconButton
import com.wanbaohe.passwordvault.R
import com.wanbaohe.passwordvault.model.PasswordVaultCategoryUi
import com.wanbaohe.passwordvault.util.localizedCategoryName
import com.t8rin.imagetoolbox.core.resources.icons.Tune

/**
 * 分类选择器：
 * - 顶部一行：标签 + "管理" 图标按钮（点击打开 [com.shifenmiao.common.components.category.CategoryManagementDialog]）
 * - 主体：FlowRow chip 列表，已选中高亮
 *
 * 选中态使用 [MaterialTheme.colorScheme.primaryContainer]，
 * 未选中使用 [MaterialTheme.colorScheme.surfaceContainerHighest]，对比稳定。
 */
@Composable
fun CategoryPickerField(
    selectedCategoryId: String?,
    categories: List<PasswordVaultCategoryUi>,
    onSelect: (String) -> Unit,
    onManage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.password_vault_entry_category),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            GlassIconButton(
                onClick = onManage,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Tune,
                    contentDescription = stringResource(R.string.password_vault_category_manage),
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        if (categories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.password_vault_category_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                categories.forEach { category ->
                    val selected = category.id == selectedCategoryId
                    GlassFilterChip(
                        selected = selected,
                        onClick = { onSelect(category.id) },
                        label = {
                            Text(
                                text = localizedCategoryName(category.id, category.name),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                    )
                }
            }
        }
    }
}
