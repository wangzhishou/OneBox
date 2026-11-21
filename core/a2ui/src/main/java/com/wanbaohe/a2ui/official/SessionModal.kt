package com.wanbaohe.a2ui.official

import androidx.a2ui.compose.runtime.A2uiComponentScope
import androidx.a2ui.compose.runtime.ProvideActionInterceptor
import androidx.a2ui.compose.ui.catalog.A2uiBasicCatalogV1
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.shifenmiao.core.R
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.glass.GlassTextButton

internal object SessionModal : A2uiBasicCatalogV1.Modal {
    @Composable
    override fun A2uiComponentScope.TypedContent(
        triggerId: String,
        contentId: String,
        accessibility: A2uiBasicCatalogV1.AccessibilityAttributes?,
        modifier: Modifier,
    ) {
        var open by rememberSaveable { mutableStateOf(false) }
        ProvideActionInterceptor(onIntercept = { open = true; true }) {
            SessionChild(triggerId, modifier.clickable(role = Role.Button) { open = true })
        }
        if (open) EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { open = false },
            modifier = Modifier.a2uiHostSemantics(accessibility),
            confirmButton = {
                GlassTextButton(onClick = { open = false }) { Text(stringResource(R.string.close)) }
            },
            text = {
                ProvideActionInterceptor(onIntercept = { open = false; false }) {
                    SessionChild(contentId)
                }
            },
        )
    }
}
