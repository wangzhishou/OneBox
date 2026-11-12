/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.chess.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
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
import com.wanbaohe.boardgame.ui.accentTextButtonColors
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog

@Composable
internal fun ChessOriginDialog(origin: GameOrigin, onContinueSource: () -> Unit, onDismiss: () -> Unit) {
    EnhancedAlertDialog(
        visible = true,
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
            TextButton(onClick = onContinueSource, colors = accentTextButtonColors()) { Text(stringResource(R.string.chess_setup_continue_source)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = accentTextButtonColors()) { Text(stringResource(R.string.chess_close)) }
        },
    )
}
