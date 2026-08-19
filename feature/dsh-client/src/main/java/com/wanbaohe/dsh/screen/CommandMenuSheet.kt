package com.wanbaohe.dsh.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanbaohe.dsh.R
import com.wanbaohe.dsh.session.CommandMenu
import com.wanbaohe.dsh.session.CommandMenuItem
import com.wanbaohe.dsh.session.filterMenu

/**
 * 斜杠命令菜单(底部 sheet,对齐 Flutter command_menu_sheet.dart):
 * 顶部搜索框常显 + 分组列表(命令 / 技能);fuzzy 过滤(前缀优先 + 子序列)。
 *
 * 点击 = onPick(CommandMenuItem),派发决策由调用方定(leadingInput 命令回填输入框,
 * 裸命令直接 execute,skill 回填 '/name ' 纯文本)。
 * 命令目录降级(agent-busy/失败)→ 内联错误位 + 重试,菜单降级 skill-only。
 */

/** 菜单加载态(加载中 / 失败 / 就绪) */
private sealed interface MenuLoadState {
    data object Loading : MenuLoadState
    data object Failed : MenuLoadState
    data class Ready(val menu: CommandMenu) : MenuLoadState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandMenuSheet(
    sessionId: String,
    loadMenu: suspend (sessionId: String, force: Boolean) -> CommandMenu?,
    onPick: (CommandMenuItem) -> Unit,
    onDismiss: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var forceReload by remember { mutableStateOf(0) }
    val state by produceState<MenuLoadState>(MenuLoadState.Loading, sessionId, forceReload) {
        value = MenuLoadState.Loading
        val menu = loadMenu(sessionId, forceReload > 0)
        value = if (menu != null) MenuLoadState.Ready(menu) else MenuLoadState.Failed
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dsh_command_menu_title),
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.dsh_close))
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                placeholder = {
                    Text(stringResource(R.string.dsh_command_search_hint), fontSize = 13.sp)
                },
                singleLine = true
            )
            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
            when (val current = state) {
                MenuLoadState.Loading -> Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                }

                MenuLoadState.Failed -> Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.dsh_command_load_failed),
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = { forceReload++ }) {
                        Text(stringResource(R.string.dsh_retry_action))
                    }
                }

                is MenuLoadState.Ready -> CommandMenuContent(
                    menu = current.menu,
                    query = query,
                    onRetry = { forceReload++ },
                    onPick = onPick
                )
            }
        }
    }
}

/** 已就绪菜单:降级错误位 + 命令组 + 技能组(fuzzy 过滤) */
@Composable
private fun CommandMenuContent(
    menu: CommandMenu,
    query: String,
    onRetry: () -> Unit,
    onPick: (CommandMenuItem) -> Unit
) {
    val commands = filterMenu(menu.commands, query)
    val skills = filterMenu(menu.skills, query)
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 420.dp)
    ) {
        if (menu.degraded) {
            item(key = "degraded") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (menu.errorCode == "agent-busy") {
                            stringResource(R.string.dsh_command_agent_busy)
                        } else {
                            stringResource(R.string.dsh_command_load_failed) +
                                (menu.errorCode?.let { " ($it)" }.orEmpty()) +
                                (menu.errorMessage?.takeIf { it.isNotBlank() }
                                    ?.let { " · $it" }.orEmpty())
                        },
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = onRetry) {
                        Text(stringResource(R.string.dsh_retry_action), fontSize = 12.sp)
                    }
                }
            }
        }
        if (commands.isNotEmpty()) {
            item(key = "header_commands") {
                GroupHeader(stringResource(R.string.dsh_command_group_commands))
            }
            items(commands.size, key = { "cmd_${commands[it].name}" }) { index ->
                CommandMenuRow(commands[index], onPick)
            }
        }
        if (skills.isNotEmpty()) {
            item(key = "header_skills") {
                GroupHeader(stringResource(R.string.dsh_command_group_skills))
            }
            items(skills.size, key = { "skill_${skills[it].name}" }) { index ->
                CommandMenuRow(skills[index], onPick)
            }
        }
        if (commands.isEmpty() && skills.isEmpty()) {
            item(key = "empty") {
                Text(
                    text = stringResource(
                        if (query.isBlank()) R.string.dsh_command_empty
                        else R.string.dsh_command_no_match
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GroupHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

/** 单个菜单行:类型图标 + /name + description · 输入提示 */
@Composable
private fun CommandMenuRow(item: CommandMenuItem, onPick: (CommandMenuItem) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPick(item) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when {
                item.isCommand -> Icons.Outlined.Terminal
                item.skillModelInvocable == false -> Icons.Outlined.Lock
                else -> Icons.Outlined.AutoAwesome
            },
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.slash,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val subtitle = buildString {
                if (item.description.isNotEmpty()) append(item.description)
                item.hint?.takeIf { it.isNotEmpty() }?.let {
                    if (isNotEmpty()) append(" · ")
                    append(stringResource(R.string.dsh_command_hint_prefix)).append(it)
                }
            }
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
