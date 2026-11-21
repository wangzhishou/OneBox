package com.wanbaohe.a2ui.official

import androidx.a2ui.compose.runtime.A2uiComponentScope
import androidx.a2ui.compose.runtime.A2uiComponentState
import androidx.a2ui.compose.ui.A2uiComponent
import androidx.a2ui.compose.ui.catalog.A2uiBasicCatalogV1
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.a2ui.A2uiSurfaceDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface

internal object SessionCard : A2uiBasicCatalogV1.Card {
    @Composable
    override fun A2uiComponentScope.TypedContent(
        childId: String,
        accessibility: A2uiBasicCatalogV1.AccessibilityAttributes?,
        modifier: Modifier,
    ) {
        GlassSurface(
            modifier = modifier.a2uiHostSemantics(accessibility),
            color = MaterialTheme.colorScheme.surfaceContainer,
            style = GlassStyle.Medium,
        ) {
            Box(Modifier.padding(16.dp)) {
                SessionChild(childId)
            }
        }
    }
}

@Composable
internal fun A2uiComponentScope.SessionChild(childId: String, modifier: Modifier = Modifier) {
    when (val child = observeA2uiComponentState(childId)) {
        is A2uiComponentState.Loading -> Box(modifier) { A2uiSurfaceDefaults.LoadingIndicator() }
        is A2uiComponentState.Error -> Box(modifier) { A2uiSurfaceDefaults.ErrorFallback(child.exception) }
        is A2uiComponentState.Success -> A2uiComponent(component = child.component, modifier = modifier)
    }
}

internal fun Modifier.a2uiHostSemantics(attributes: A2uiBasicCatalogV1.AccessibilityAttributes?): Modifier {
    val description = listOfNotNull(attributes?.label, attributes?.description)
        .filter(String::isNotBlank).joinToString(" - ")
    return if (description.isBlank()) this else semantics { contentDescription = description }
}
