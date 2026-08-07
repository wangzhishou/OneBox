package com.wanbaohe.minesweeper.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassThin
import com.wanbaohe.minesweeper.component.MinesweeperComponent
import com.wanbaohe.minesweeper.component.MinesweeperUiState
import com.wanbaohe.minesweeper.logic.Cell
import com.wanbaohe.minesweeper.logic.Difficulty
import com.wanbaohe.minesweeper.logic.GameState
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFlag

@Composable
fun MinesweeperScreen(
    component: MinesweeperComponent
) {
    val state by component.uiState.collectAsState()

    BaseScreen(
        title = "扫雷", // Can use stringResource if needed
        onGoBack = component.onGoBack,
        actions = {
            IconButton(
                onClick = { component.resetGame(state.difficulty) },
                colors = AppTheme.colors.iconButtonColors()
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                    contentDescription = "Restart"
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Timer and Mines Left
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "⏱ ${state.timer}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "💣 ${state.minesLeft}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Difficulty Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Difficulty.values().forEach { diff ->
                    val isSelected = state.difficulty == diff
                    Button(
                        onClick = { component.resetGame(diff) },
                        colors = if (isSelected) ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) else ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                    ) {
                        Text(text = diff.name)
                    }
                }
            }

            // Game Board
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    for (row in state.board) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            for (cell in row) {
                                CellView(
                                    cell = cell,
                                    onClick = { component.onCellClicked(cell.row, cell.col) },
                                    onLongClick = { component.onCellLongClicked(cell.row, cell.col) }
                                )
                            }
                        }
                    }
                }
            }

            // Game State Message
            if (state.gameState == GameState.WON) {
                Text(
                    text = "🎉 You Won! 🎉",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(16.dp)
                )
            } else if (state.gameState == GameState.LOST) {
                Text(
                    text = "💥 Game Over! 💥",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun CellView(
    cell: Cell,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val backgroundColor = if (cell.isRevealed) {
        if (cell.isMine) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (cell.isRevealed) {
            if (cell.isMine) {
                Text(text = "💣", fontSize = 16.sp)
            } else if (cell.neighborMines > 0) {
                val color = when (cell.neighborMines) {
                    1 -> Color.Blue
                    2 -> Color(0xFF388E3C)
                    3 -> Color.Red
                    4 -> Color(0xFF7B1FA2)
                    5 -> Color(0xFFD32F2F)
                    else -> Color.Black
                }
                Text(
                    text = cell.neighborMines.toString(),
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        } else if (cell.isFlagged) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFlag,
                contentDescription = "Flag",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
