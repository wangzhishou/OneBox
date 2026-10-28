package com.shifenmiao.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object Release151Migrations {
    const val VERSION = 151

    val feature: Array<Migration> = arrayOf(
        object : Migration(148, VERSION) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `marktodo_task_new` (
                        `id` TEXT NOT NULL,
                        `category_id` TEXT,
                        `title` TEXT NOT NULL,
                        `note` TEXT,
                        `start_date` INTEGER NOT NULL,
                        `due_date` INTEGER,
                        `has_due_time` INTEGER NOT NULL DEFAULT 0,
                        `tags` TEXT NOT NULL,
                        `is_completed` INTEGER NOT NULL,
                        `is_starred` INTEGER NOT NULL,
                        `priority` INTEGER NOT NULL DEFAULT 1,
                        `completed_at` INTEGER,
                        `sort_order` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `deleted_at` INTEGER,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`category_id`) REFERENCES `marktodo_category`(`id`)
                            ON UPDATE CASCADE ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `marktodo_task_new` (
                        `id`, `category_id`, `title`, `note`, `start_date`, `due_date`,
                        `has_due_time`, `tags`, `is_completed`, `is_starred`, `priority`,
                        `completed_at`, `sort_order`, `created_at`, `updated_at`, `deleted_at`
                    )
                    SELECT
                        `id`,
                        CASE WHEN `category_id` IN (SELECT `id` FROM `marktodo_category`)
                            THEN `category_id` ELSE NULL END,
                        `title`, `note`, `start_date`, `due_date`,
                        0, `tags`, `is_completed`, `is_starred`, `priority`,
                        `completed_at`, `sort_order`, `created_at`, `updated_at`, NULL
                    FROM `marktodo_task`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `marktodo_task`")
                db.execSQL("ALTER TABLE `marktodo_task_new` RENAME TO `marktodo_task`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_marktodo_task_category_id` ON `marktodo_task` (`category_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_marktodo_task_start_date` ON `marktodo_task` (`start_date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_marktodo_task_updated_at` ON `marktodo_task` (`updated_at`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_marktodo_task_deleted_at` ON `marktodo_task` (`deleted_at`)")
            }
        },
    )
}
