package com.wanbaohe.poem.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFavorite
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.poem.R
import com.wanbaohe.poem.component.PoemHistoryComponent
import com.wanbaohe.poem.model.Poem

@Composable
fun PoemHistoryScreen(component: PoemHistoryComponent) {
    val uiState by component.uiState.collectAsState()

    BaseScreen(
        title = stringResource(R.string.poem_history),
        onGoBack = component.onGoBack,
    ) {
        val selectedPoem = uiState.selectedPoem
        if (selectedPoem != null) {
            // 页内详情态
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                GlassTonalButton(onClick = component::closePoem) {
                    Text(text = stringResource(R.string.poem_back_to_list))
                }
                PoemCard(poem = selectedPoem)
                PoemInsightFavoriteRow(
                    poem = selectedPoem,
                    isGeneratingInsight = uiState.isGeneratingInsight,
                    onGenerateInsight = component::generateInsight,
                    onToggleFavorite = component::toggleFavorite,
                )
                GlassTonalButton(
                    onClick = { component.deletePoem(selectedPoem) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp),
                    )
                    Text(text = stringResource(R.string.poem_delete))
                }
                PoemInsightSection(
                    aiInsight = selectedPoem.aiInsight,
                    isGenerating = uiState.isGeneratingInsight,
                    insightError = uiState.insightError,
                )
            }
            return@BaseScreen
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GlassTonalButton(
                onClick = component::toggleFavoritesOnly,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFavorite,
                    contentDescription = null,
                    tint = if (uiState.favoritesOnly) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(end = 6.dp),
                )
                Text(text = stringResource(R.string.poem_favorites_only))
            }

            if (uiState.poems.isEmpty()) {
                Text(
                    text = stringResource(R.string.poem_history_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = uiState.poems,
                        key = { it.id },
                    ) { poem ->
                        PoemHistoryItem(
                            poem = poem,
                            onClick = { component.openPoem(poem) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PoemHistoryItem(
    poem: Poem,
    onClick: () -> Unit,
) {
    GlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = poem.title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = poem.authorWithDynasty,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (poem.isFavorite) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFavorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
