package com.shifenmiao.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * App 1.4.1 (versionCode 141) 的数据库迁移入口。
 *
 * 1.4.0 已发布基线（以 tag 1.4.0 为准）：FeatureDatabase=140。因此 140 之前的变更
 * 保留在 [Release140Migrations] 中不再改动，本文件只承载 140 → 141 的增量。
 *
 * 本次新增：五子棋 / 国际象棋各三张表（game / ply / ai_task），表结构与象棋三表一致，
 * 仅换表名；Room 会自行把低版本(4..9)经 4..140 → 140..141 链式迁移过来。
 */
internal object Release141Migrations {
    const val VERSION = 141

    val feature: Array<Migration> = arrayOf(
        migration(140, ::migrateFeature140To141),
    )

    private fun migration(
        fromVersion: Int,
        migrate: (SupportSQLiteDatabase) -> Unit,
    ): Migration = object : Migration(fromVersion, VERSION) {
        override fun migrate(db: SupportSQLiteDatabase) = migrate.invoke(db)
    }

    private fun migrateFeature140To141(db: SupportSQLiteDatabase) {
        // 五子棋三表（与象棋三表同构，仅表名不同）。
        db.execSQL("CREATE TABLE IF NOT EXISTS `gomoku_game` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `mode` TEXT NOT NULL, `red_player_type` TEXT NOT NULL, `black_player_type` TEXT NOT NULL, `red_player_config_json` TEXT NOT NULL DEFAULT '{}', `black_player_config_json` TEXT NOT NULL DEFAULT '{}', `initial_fen` TEXT NOT NULL, `current_fen` TEXT NOT NULL, `current_ply` INTEGER NOT NULL DEFAULT 0, `status` TEXT NOT NULL, `result` TEXT NOT NULL DEFAULT '', `winner_side` TEXT NOT NULL DEFAULT '', `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, `last_played_at` INTEGER NOT NULL, `started_at` INTEGER NOT NULL DEFAULT 0, `last_move_at` INTEGER NOT NULL DEFAULT 0, `archived` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`id`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `gomoku_ply` (`id` TEXT NOT NULL, `game_id` TEXT NOT NULL, `ply` INTEGER NOT NULL, `move_ucci` TEXT NOT NULL, `move_cn` TEXT NOT NULL, `mover_side` TEXT NOT NULL, `before_fen` TEXT NOT NULL, `after_fen` TEXT NOT NULL, `is_capture` INTEGER NOT NULL, `is_check` INTEGER NOT NULL, `is_checkmate` INTEGER NOT NULL, `ai_reason` TEXT NOT NULL DEFAULT '', `ai_raw_response` TEXT NOT NULL DEFAULT '', `think_duration_ms` INTEGER NOT NULL DEFAULT 0, `created_at` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_gomoku_ply_game_id_ply` ON `gomoku_ply` (`game_id`, `ply`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_gomoku_ply_game_id` ON `gomoku_ply` (`game_id`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `gomoku_ai_task` (`id` TEXT NOT NULL, `game_id` TEXT NOT NULL, `target_ply` INTEGER NOT NULL, `request_json` TEXT NOT NULL, `status` TEXT NOT NULL, `retry_count` INTEGER NOT NULL DEFAULT 0, `response_json` TEXT NOT NULL DEFAULT '', `validated_move` TEXT NOT NULL DEFAULT '', `error_message` TEXT NOT NULL DEFAULT '', `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_gomoku_ai_task_game_id_status` ON `gomoku_ai_task` (`game_id`, `status`)")

        // 国际象棋三表（与象棋三表同构，仅表名不同）。
        db.execSQL("CREATE TABLE IF NOT EXISTS `chess_game` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `mode` TEXT NOT NULL, `red_player_type` TEXT NOT NULL, `black_player_type` TEXT NOT NULL, `red_player_config_json` TEXT NOT NULL DEFAULT '{}', `black_player_config_json` TEXT NOT NULL DEFAULT '{}', `initial_fen` TEXT NOT NULL, `current_fen` TEXT NOT NULL, `current_ply` INTEGER NOT NULL DEFAULT 0, `status` TEXT NOT NULL, `result` TEXT NOT NULL DEFAULT '', `winner_side` TEXT NOT NULL DEFAULT '', `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, `last_played_at` INTEGER NOT NULL, `started_at` INTEGER NOT NULL DEFAULT 0, `last_move_at` INTEGER NOT NULL DEFAULT 0, `archived` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`id`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `chess_ply` (`id` TEXT NOT NULL, `game_id` TEXT NOT NULL, `ply` INTEGER NOT NULL, `move_ucci` TEXT NOT NULL, `move_cn` TEXT NOT NULL, `mover_side` TEXT NOT NULL, `before_fen` TEXT NOT NULL, `after_fen` TEXT NOT NULL, `is_capture` INTEGER NOT NULL, `is_check` INTEGER NOT NULL, `is_checkmate` INTEGER NOT NULL, `ai_reason` TEXT NOT NULL DEFAULT '', `ai_raw_response` TEXT NOT NULL DEFAULT '', `think_duration_ms` INTEGER NOT NULL DEFAULT 0, `created_at` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_chess_ply_game_id_ply` ON `chess_ply` (`game_id`, `ply`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_chess_ply_game_id` ON `chess_ply` (`game_id`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `chess_ai_task` (`id` TEXT NOT NULL, `game_id` TEXT NOT NULL, `target_ply` INTEGER NOT NULL, `request_json` TEXT NOT NULL, `status` TEXT NOT NULL, `retry_count` INTEGER NOT NULL DEFAULT 0, `response_json` TEXT NOT NULL DEFAULT '', `validated_move` TEXT NOT NULL DEFAULT '', `error_message` TEXT NOT NULL DEFAULT '', `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_chess_ai_task_game_id_status` ON `chess_ai_task` (`game_id`, `status`)")
    }
}
