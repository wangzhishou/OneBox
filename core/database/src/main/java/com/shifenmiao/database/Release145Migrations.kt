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

/**
 * App 1.4.5 (versionCode 145) 的数据库迁移入口。
 *
 * 1.4.1 已发布基线：FeatureDatabase=141（App 1.4.2~1.4.4 无 schema 变更，DB 版本保持 141），
 * 本文件承载 141 → 145 的增量（DB 版本直接对齐发版 versionCode，跳过 142~144）。
 *
 * 本次新增（待办事项改版）：
 * - FeatureDatabase:新增 marktodo_tag 标签表;
 * - marktodo_category 新增 description / color_argb / is_pinned 列;
 * - marktodo_task 新增 priority / completed_at 列。
 * CREATE/ALTER 语句必须与 schemas/.../145.json 的 createSql 完全一致
 * （ALTER 的 DEFAULT 必须与实体的 defaultValue 一致），否则启动时会抛迁移校验异常。
 */
internal object Release145Migrations {
    const val VERSION = 145

    val feature: Array<Migration> = arrayOf(
        migration(141, ::migrateFeature141To145),
    )

    private fun migration(
        fromVersion: Int,
        migrate: (SupportSQLiteDatabase) -> Unit,
    ): Migration = object : Migration(fromVersion, VERSION) {
        override fun migrate(db: SupportSQLiteDatabase) = migrate.invoke(db)
    }

    private fun migrateFeature141To145(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `marktodo_tag` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `color_argb` INTEGER, `sort_order` INTEGER NOT NULL, `is_preset` INTEGER NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("ALTER TABLE `marktodo_category` ADD COLUMN `description` TEXT")
        db.execSQL("ALTER TABLE `marktodo_category` ADD COLUMN `color_argb` INTEGER")
        db.execSQL("ALTER TABLE `marktodo_category` ADD COLUMN `is_pinned` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `marktodo_task` ADD COLUMN `priority` INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE `marktodo_task` ADD COLUMN `completed_at` INTEGER")
    }
}
