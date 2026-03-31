package com.mostgymapp.app.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mostgymapp.app.data.db.migration.MIGRATION_1_2
import com.mostgymapp.app.data.db.migration.MIGRATION_2_3
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutLogMigrationTest {

    private val testDb = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        WorkoutLogDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate1To2_addsSetColumns() {
        helper.createDatabase(testDb, 1).apply {
            createV1Schema(this)
            close()
        }

        val db = helper.runMigrationsAndValidate(testDb, 2, false, MIGRATION_1_2)
        val cursor = db.query("PRAGMA table_info(set_entry)")
        var hasRpe = false
        var hasNote = false
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            if (name == "rpe") hasRpe = true
            if (name == "note") hasNote = true
        }
        cursor.close()
        assertTrue(hasRpe)
        assertTrue(hasNote)
    }

    @Test
    fun migrate2To3_addsDefaultRestSec() {
        helper.createDatabase(testDb, 2).apply {
            createV2Schema(this)
            close()
        }

        val db = helper.runMigrationsAndValidate(testDb, 3, false, MIGRATION_2_3)
        val cursor = db.query("PRAGMA table_info(template_exercise)")
        var hasDefaultRestSec = false
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            if (name == "defaultRestSec") hasDefaultRestSec = true
        }
        cursor.close()
        assertTrue(hasDefaultRestSec)
    }

    private fun createV1Schema(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `exercise` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `isArchived` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_exercise_name` ON `exercise` (`name`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `workout` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `startTime` INTEGER NOT NULL,
                `endTime` INTEGER,
                `note` TEXT
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_startTime` ON `workout` (`startTime`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_endTime` ON `workout` (`endTime`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `workout_exercise` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `workoutId` INTEGER NOT NULL,
                `exerciseId` INTEGER NOT NULL,
                `orderIndex` INTEGER NOT NULL,
                FOREIGN KEY(`workoutId`) REFERENCES `workout`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`exerciseId`) REFERENCES `exercise`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_exercise_workoutId` ON `workout_exercise` (`workoutId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_exercise_exerciseId` ON `workout_exercise` (`exerciseId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_exercise_workoutId_orderIndex` ON `workout_exercise` (`workoutId`, `orderIndex`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `set_entry` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `workoutExerciseId` INTEGER NOT NULL,
                `orderIndex` INTEGER NOT NULL,
                `weight` REAL NOT NULL,
                `reps` INTEGER NOT NULL,
                `isCompleted` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                FOREIGN KEY(`workoutExerciseId`) REFERENCES `workout_exercise`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_set_entry_workoutExerciseId` ON `set_entry` (`workoutExerciseId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_set_entry_workoutExerciseId_orderIndex` ON `set_entry` (`workoutExerciseId`, `orderIndex`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_set_entry_createdAt` ON `set_entry` (`createdAt`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `template` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `note` TEXT,
                `createdAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_template_name` ON `template` (`name`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `template_exercise` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `templateId` INTEGER NOT NULL,
                `exerciseId` INTEGER NOT NULL,
                `orderIndex` INTEGER NOT NULL,
                FOREIGN KEY(`templateId`) REFERENCES `template`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`exerciseId`) REFERENCES `exercise`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_template_exercise_templateId` ON `template_exercise` (`templateId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_template_exercise_exerciseId` ON `template_exercise` (`exerciseId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_template_exercise_templateId_orderIndex` ON `template_exercise` (`templateId`, `orderIndex`)")
    }

    private fun createV2Schema(db: SupportSQLiteDatabase) {
        createV1Schema(db)
        db.execSQL("ALTER TABLE set_entry ADD COLUMN rpe REAL")
        db.execSQL("ALTER TABLE set_entry ADD COLUMN note TEXT")
    }
}
