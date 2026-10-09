package com.shifenmiao.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * App 1.4.0 (versionCode 140) 的统一迁移入口；发布前的数据库变更继续合并到这里。
 * 1.3.9 已发布基线：AppDatabase=2，FeatureDatabase=4（以 tag 1.3.9 为准）。
 * 额外起点仅兼容已安装的中间开发版，复用同一套 SQL，不再按功能递增数据库版本。
 */
internal object Release140Migrations {
    const val VERSION = 140

    val app: Array<Migration> = (2..4).map { fromVersion ->
        migration(fromVersion, ::migrateApp)
    }.toTypedArray()

    val feature: Array<Migration> = (4..9).map { fromVersion ->
        migration(fromVersion, ::migrateFeature)
    }.toTypedArray()

    private fun migration(
        fromVersion: Int,
        migrate: (SupportSQLiteDatabase) -> Unit,
    ): Migration = object : Migration(fromVersion, VERSION) {
        override fun migrate(db: SupportSQLiteDatabase) = migrate.invoke(db)
    }

    private fun migrateApp(db: SupportSQLiteDatabase) {
        // AI 记忆与技能。IF NOT EXISTS 同时兼容已创建这些表的开发版。
        db.execSQL("CREATE TABLE IF NOT EXISTS `memory_entry` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `kind` TEXT NOT NULL, `content` TEXT NOT NULL, `source_conversation_id` TEXT, `enabled` INTEGER NOT NULL DEFAULT 1, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_memory_entry_kind_enabled_created_at` ON `memory_entry` (`kind`, `enabled`, `created_at`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `skill` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `body` TEXT NOT NULL, `version` TEXT NOT NULL DEFAULT '1.0.0', `source` TEXT NOT NULL, `document_id` TEXT, `enabled` INTEGER NOT NULL DEFAULT 1, `use_count` REAL NOT NULL DEFAULT 0, `installed_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `conversation_memory_policy` (`conversation_id` TEXT NOT NULL, `memory_enabled` INTEGER NOT NULL DEFAULT 1, `skills_enabled` INTEGER NOT NULL DEFAULT 1, `updated_at` INTEGER NOT NULL, PRIMARY KEY(`conversation_id`))")

        // 主题玻璃边界。DDL 默认值必须冻结在字面量 0.17、与 ThemePresetEntity 的
        // @ColumnInfo(defaultValue = "0.17") 完全一致——Room 迁移后会逐列比对默认值,
        // 写 0.2 会抛 Migration didn't properly handle 让老用户升级即崩溃。
        // 此默认值运行时不会被读到(INSERT 全字段显式写入), 业务默认值仍以
        // ThemeDefaults.DEFAULT_GLASS_BORDER_ALPHA 为准。
        db.addColumnIfMissing(
            "theme_preset",
            "glass_border_alpha",
            "REAL NOT NULL DEFAULT 0.17",
        )
    }

    private fun migrateFeature(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `ai_detect_record` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type` TEXT NOT NULL, `inputSummary` TEXT NOT NULL, `probability` REAL NOT NULL, `verdict` TEXT NOT NULL, `detailJson` TEXT NOT NULL DEFAULT '', `createdAt` INTEGER NOT NULL)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ai_detect_record_createdAt` ON `ai_detect_record` (`createdAt`)")

        db.execSQL("CREATE TABLE IF NOT EXISTS `health_record` (`id` TEXT NOT NULL, `type` TEXT NOT NULL, `happened_at` INTEGER NOT NULL, `fields_json` TEXT NOT NULL, `note` TEXT, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_health_record_happened_at` ON `health_record` (`happened_at`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_health_record_type` ON `health_record` (`type`)")

        // 新建表直接包含最终字段；旧开发版缺失的列在下面补齐。
        db.execSQL("CREATE TABLE IF NOT EXISTS `household_location` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `parent_id` TEXT, `sort_order` INTEGER NOT NULL, `created_at` INTEGER NOT NULL, `icon_key` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`parent_id`) REFERENCES `household_location`(`id`) ON UPDATE CASCADE ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_household_location_parent_id` ON `household_location` (`parent_id`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `household_item` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `category` TEXT, `location_id` TEXT, `expire_at` INTEGER, `photo_path` TEXT, `note` TEXT, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`location_id`) REFERENCES `household_location`(`id`) ON UPDATE CASCADE ON DELETE SET NULL)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_household_item_location_id` ON `household_item` (`location_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_household_item_name` ON `household_item` (`name`)")
        db.addColumnIfMissing("household_location", "icon_key", "TEXT")

        db.execSQL("CREATE TABLE IF NOT EXISTS `period_record` (`id` TEXT NOT NULL, `record_date` INTEGER NOT NULL, `is_period_start` INTEGER NOT NULL, `is_period_end` INTEGER NOT NULL, `flow_intensity` TEXT NOT NULL, `symptoms` TEXT NOT NULL, `mood` TEXT NOT NULL, `note` TEXT, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_period_record_record_date` ON `period_record` (`record_date`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_period_record_is_period_start` ON `period_record` (`is_period_start`)")

        db.addColumnIfMissing("wheel_options", "weight", "REAL NOT NULL DEFAULT 1.0")
        db.addColumnIfMissing("wheel_options", "enabled", "INTEGER NOT NULL DEFAULT 1")
        if (db.addColumnIfMissing("wheel_history", "wheelTitle", "TEXT NOT NULL DEFAULT ''")) {
            // 只在首次加列时回填，不能覆盖开发版已保存的历史标题（转盘可能已删除/改名）。
            db.execSQL("UPDATE `wheel_history` SET `wheelTitle` = COALESCE((SELECT `title` FROM `decision_wheels` WHERE `decision_wheels`.`id` = `wheel_history`.`wheelId`), '')")
        }
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_wheel_options_wheelId` ON `wheel_options` (`wheelId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_wheel_history_wheelId` ON `wheel_history` (`wheelId`)")
    }

    /** 仅用于上面的固定表名/列名；不接受用户输入。返回是否实际新增了列。 */
    private fun SupportSQLiteDatabase.addColumnIfMissing(
        table: String,
        column: String,
        definition: String,
    ): Boolean {
        val exists = query("PRAGMA table_info(`$table`)").use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            var found = false
            while (cursor.moveToNext()) {
                if (cursor.getString(nameIndex) == column) {
                    found = true
                    break
                }
            }
            found
        }
        if (exists) return false
        execSQL("ALTER TABLE `$table` ADD COLUMN `$column` $definition")
        return true
    }
}

