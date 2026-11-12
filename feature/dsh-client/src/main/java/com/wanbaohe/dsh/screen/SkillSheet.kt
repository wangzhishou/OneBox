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

package com.wanbaohe.dsh.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.sp
import com.wanbaohe.dsh.R
import com.wanbaohe.dsh.component.DshRootComponent
import com.wanbaohe.dsh.wire.model.SkillEntry

/**
 * skill 快捷菜单(对齐 Flutter goal_skill_widgets.dart 的 showSkillSheet):
 * 底部弹层列出 skill.list 目录;点选即把 "/name " 填入输入框
 * (斜杠命令 = 内容恰好是单个 "/" 开头文本块的普通 prompt,无专线)。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillSheet(
    sessionId: String,
    component: DshRootComponent,
    onDismiss: () -> Unit
) {
    var skills by remember { mutableStateOf<List<SkillEntry>?>(null) }
    var failed by remember { mutableStateOf(false) }
    LaunchedEffect(sessionId) {
        val loaded = component.loadSkills(sessionId)
        if (loaded == null) failed = true else skills = loaded
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(R.string.dsh_skill_title),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        when {
            failed -> Text(
                text = stringResource(R.string.dsh_skill_load_failed),
                modifier = Modifier.padding(16.dp),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            skills == null -> Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }

            skills!!.isEmpty() -> Text(
                text = stringResource(R.string.dsh_skill_empty),
                modifier = Modifier.padding(16.dp),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            else -> LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                items(skills!!, key = { it.name }) { skill ->
                    SkillRow(
                        skill = skill,
                        onClick = {
                            component.fillSkillPrompt(skill.name)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

/** 单个技能行:/name + 描述;modelInvocable 决定图标(可模型调用 / 仅人工) */
@Composable
private fun SkillRow(skill: SkillEntry, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (skill.modelInvocable) {
                Icons.Outlined.AutoAwesome
            } else {
                Icons.Outlined.Lock
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 12.dp)
        )
        Column {
            Text(text = "/${skill.name}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(
                text = skill.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
