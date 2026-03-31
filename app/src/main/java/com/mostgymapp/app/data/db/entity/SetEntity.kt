package com.mostgymapp.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "set_entry",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("workoutExerciseId"),
        Index(value = ["workoutExerciseId", "orderIndex"]),
        Index("createdAt")
    ]
)
data class SetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutExerciseId: Long,
    val orderIndex: Int,
    val weight: Double,
    val reps: Int,
    val rpe: Double? = null,
    val note: String? = null,
    val isCompleted: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
