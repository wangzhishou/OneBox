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

package com.shifenmiao.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object Release142Migrations {
    const val VERSION = 142

    val app: Array<Migration> = arrayOf(
        object : Migration(141, VERSION) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateApp141To142(db)
            }
        },
    )

    private fun migrateApp141To142(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `ai_models` ADD COLUMN `remote_id` INTEGER")

        val renamedModels = listOf(
            "k3.1-pro" to "kimi-k3.1-pro",
            "k3.1-pro-highspeed" to "kimi-k3.1-pro-highspeed",
        )
        renamedModels.forEach { (oldName, newName) ->
            mergeRenamedRemoteModel(db, oldName, newName)
        }

        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_ai_models_engine_name_remote_id` ON `ai_models` (`engine_name`, `remote_id`)")
    }

    private fun mergeRenamedRemoteModel(
        db: SupportSQLiteDatabase,
        oldName: String,
        newName: String,
    ) {
        val oldRowExists = """
            EXISTS (
                SELECT 1 FROM `ai_models` AS `old`
                WHERE `old`.`name` = '$oldName'
                    AND `old`.`engine_name` = 'kimi'
                    AND `old`.`source` = 'REMOTE'
            )
        """.trimIndent()
        val newRowExists = """
            EXISTS (
                SELECT 1 FROM `ai_models` AS `new`
                WHERE `new`.`name` = '$newName'
                    AND `new`.`engine_name` = 'kimi'
                    AND `new`.`source` = 'REMOTE'
            )
        """.trimIndent()

        val overrideAssignments = overrideColumns.joinToString { column ->
            "`$column` = COALESCE(`$column`, (SELECT `old`.`$column` FROM `ai_models` AS `old` WHERE `old`.`name` = '$oldName' AND `old`.`engine_name` = 'kimi' AND `old`.`source` = 'REMOTE' LIMIT 1))"
        }
        db.execSQL(
            """
            UPDATE `ai_models`
            SET $overrideAssignments
            WHERE `name` = '$newName'
                AND `engine_name` = 'kimi'
                AND `source` = 'REMOTE'
                AND $oldRowExists
            """.trimIndent()
        )
        db.execSQL(
            """
            DELETE FROM `ai_models`
            WHERE `name` = '$oldName'
                AND `engine_name` = 'kimi'
                AND `source` = 'REMOTE'
                AND $newRowExists
            """.trimIndent()
        )
        db.execSQL(
            """
            UPDATE `ai_models`
            SET `name` = '$newName'
            WHERE `name` = '$oldName'
                AND `engine_name` = 'kimi'
                AND `source` = 'REMOTE'
                AND NOT $newRowExists
            """.trimIndent()
        )
        db.execSQL(
            "UPDATE `ai_engines` SET `selected_model_name` = '$newName' WHERE `name` = 'kimi' AND `selected_model_name` = '$oldName'"
        )
    }

    private val overrideColumns = listOf(
        "override_can_upload_file",
        "override_can_network",
        "override_temperature",
        "override_top_p",
        "override_max_tokens",
        "override_can_reasoning",
        "override_can_video",
        "override_can_image",
        "override_can_use_temp_api",
        "override_is_fast",
        "override_is_code",
        "override_support_tool_calls",
    )
}
