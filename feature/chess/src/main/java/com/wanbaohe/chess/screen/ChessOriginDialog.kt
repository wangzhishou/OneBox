package com.wanbaohe.chess.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wanbaohe.chess.R
import com.wanbaohe.chess.domain.FenCodec
import com.wanbaohe.chess.domain.model.GameOrigin
import com.wanbaohe.chess.ui.board.ChessBoard

@Composable
internal fun ChessOriginDialog(origin: GameOrigin, onContinueSource: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.chess_setup_source)) },
        text = {
            Column {
                Text(origin.title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(stringResource(R.string.chess_setup_source_ply, origin.ply))
                Box(Modifier.heightIn(max = 320.dp), contentAlignment = Alignment.Center) {
                    ChessBoard(FenCodec.parse(origin.fen), null, emptySet(), { _, _ -> })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onContinueSource) { Text(stringResource(R.string.chess_setup_continue_source)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.chess_close)) }
        },
    )
}
