package com.mostgymapp.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mostgymapp.app.data.db.dao.ExerciseDao
import com.mostgymapp.app.data.db.dao.SetDao
import com.mostgymapp.app.data.db.dao.TemplateDao
import com.mostgymapp.app.data.db.dao.WorkoutDao
import com.mostgymapp.app.data.db.entity.ExerciseEntity
import com.mostgymapp.app.data.db.entity.SetEntity
import com.mostgymapp.app.data.db.entity.TemplateEntity
import com.mostgymapp.app.data.db.entity.TemplateExerciseEntity
import com.mostgymapp.app.data.db.entity.WorkoutEntity
import com.mostgymapp.app.data.db.entity.WorkoutExerciseEntity

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        SetEntity::class,
        TemplateEntity::class,
        TemplateExerciseEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class WorkoutLogDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun setDao(): SetDao
    abstract fun templateDao(): TemplateDao
}
