package com.mostgymapp.app.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE set_entry ADD COLUMN rpe REAL")
        db.execSQL("ALTER TABLE set_entry ADD COLUMN note TEXT")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE template_exercise ADD COLUMN defaultRestSec INTEGER")
    }
}
