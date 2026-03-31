package com.mostgymapp.app.data.db.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.mostgymapp.app.data.db.entity.ExerciseEntity
import com.mostgymapp.app.data.db.entity.TemplateEntity
import com.mostgymapp.app.data.db.entity.TemplateExerciseEntity

data class TemplateExerciseWithExercise(
    @Embedded val templateExercise: TemplateExerciseEntity,
    @Relation(parentColumn = "exerciseId", entityColumn = "id")
    val exercise: ExerciseEntity
)

data class TemplateWithExercises(
    @Embedded val template: TemplateEntity,
    @Relation(
        entity = TemplateExerciseEntity::class,
        parentColumn = "id",
        entityColumn = "templateId"
    )
    val exercises: List<TemplateExerciseWithExercise>
)

data class TemplateSummaryRow(
    val id: Long,
    val name: String,
    val note: String?,
    val createdAt: Long,
    val exerciseCount: Int
)
