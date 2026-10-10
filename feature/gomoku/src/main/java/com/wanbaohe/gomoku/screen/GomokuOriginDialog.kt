package com.wanbaohe.gomoku.screen

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
import com.wanbaohe.gomoku.R
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.model.GameOrigin
import com.wanbaohe.gomoku.ui.board.GomokuBoard

@Composable
internal fun GomokuOriginDialog(origin: GameOrigin, onOpenSource: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.gomoku_setup_source)) },
        text = {
            Column {
                Text(origin.title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(stringResource(R.string.gomoku_setup_source_ply, origin.ply))
                Box(Modifier.heightIn(max = 300.dp), contentAlignment = Alignment.Center) {
                    GomokuBoard(FenCodec.parse(origin.fen), null, emptySet(), { _, _ -> })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onOpenSource) {
                Text(stringResource(R.string.gomoku_setup_open_source), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.gomoku_close)) } },
    )
}
