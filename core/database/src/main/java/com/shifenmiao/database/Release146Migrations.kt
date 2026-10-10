package com.shifenmiao.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object Release146Migrations {
    const val VERSION = 146

    val feature: Array<Migration> = arrayOf(
        object : Migration(145, VERSION) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `xiangqi_game` ADD COLUMN `origin_json` TEXT NOT NULL DEFAULT '{}'")
            }
        },
    )
}
