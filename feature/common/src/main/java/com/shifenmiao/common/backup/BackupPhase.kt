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

package com.shifenmiao.common.backup

/**
 * 备份/恢复过程中的阶段标记。
 *
 * 只描述「正在做什么」，具体文案由 UI 层映射到 string resources，
 * 避免在这一层硬编码用户可见文字。
 */
enum class BackupPhase {
    CHECKPOINT,
    DATABASES,
    SETTINGS,
    HISTORIES,
    FILES,
    PACKING,

    VALIDATING,
    SNAPSHOT,
    REMOVING_OLD,
    EXTRACTING_DATABASES,
    EXTRACTING_SETTINGS,
    EXTRACTING_HISTORIES,
    EXTRACTING_FILES,
    ROLLING_BACK,
}
