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

package com.wanbaohe.gomoku.screen

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
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.gomoku.R
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.model.GameOrigin
import com.wanbaohe.gomoku.ui.board.GomokuBoard
import com.wanbaohe.boardgame.ui.accentTextButtonColors

@Composable
internal fun GomokuOriginDialog(origin: GameOrigin, onOpenSource: () -> Unit, onDismiss: () -> Unit) {
    EnhancedAlertDialog(
        visible = true,
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
            TextButton(onClick = onOpenSource, colors = accentTextButtonColors()) {
                Text(stringResource(R.string.gomoku_setup_open_source), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, colors = accentTextButtonColors()) { Text(stringResource(R.string.gomoku_close)) } },
    )
}
