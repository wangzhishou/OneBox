package com.shifenmiao.ai.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet

@Composable
fun RecentQuestionsBottomSheet(
    visible: Boolean,
    questions: List<String>,
    onQuestionSelected: (String) -> Unit,
    onDismiss: (Boolean) -> Unit,
) {
    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = onDismiss,
        dragHandle = {
            AiBottomSheetHeader(
                title = stringResource(R.string.ai_input_recent),
                onClose = { onDismiss(true) }
            )
        }
    ) {
        var searchQuery by remember { mutableStateOf("") }
        val filteredQuestions by remember(questions, searchQuery) {
            derivedStateOf {
                if (searchQuery.isBlank()) questions
                else questions.filter { it.contains(searchQuery, ignoreCase = true) }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(
                    min = AiBottomSheetDefaults.ContentMinHeight,
                    max = AiBottomSheetDefaults.ContentMaxHeight
                )
                .navigationBarsPadding()
                .padding(horizontal = AiBottomSheetDefaults.HorizontalPadding)
        ) {
            AiBottomSheetSearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = stringResource(R.string.search_recent_hint)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredQuestions.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_recent_questions),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        vertical = AppTheme.dimens.paddingSmall
                    ),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredQuestions) { question ->
                        RecentQuestionCard(
                            question = question,
                            onClick = { onQuestionSelected(question) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentQuestionCard(
    question: String,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 内容
            Text(
                text = question,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            // 底部：USE 按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                ) {
                    Text(
                        text = stringResource(R.string.use),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
