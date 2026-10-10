package com.shifenmiao.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object Release147Migrations {
    const val VERSION = 147

    val feature: Array<Migration> = arrayOf(
        object : Migration(146, VERSION) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `gomoku_game` ADD COLUMN `origin_json` TEXT NOT NULL DEFAULT '{}'")
                db.execSQL("ALTER TABLE `chess_game` ADD COLUMN `origin_json` TEXT NOT NULL DEFAULT '{}'")
            }
        },
    )
}
