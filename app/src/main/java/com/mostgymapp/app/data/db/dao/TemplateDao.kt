package com.mostgymapp.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.mostgymapp.app.data.db.entity.TemplateEntity
import com.mostgymapp.app.data.db.entity.TemplateExerciseEntity
import com.mostgymapp.app.data.db.relation.TemplateSummaryRow
import com.mostgymapp.app.data.db.relation.TemplateWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {

    @Insert
    suspend fun insertTemplate(entity: TemplateEntity): Long

    @Insert
    suspend fun insertTemplateExercise(entity: TemplateExerciseEntity): Long

    @Query("SELECT * FROM template ORDER BY createdAt DESC")
    fun observeTemplates(): Flow<List<TemplateEntity>>

    @Query(
        """
        SELECT t.id, t.name, t.note, t.createdAt,
               COUNT(te.id) AS exerciseCount
        FROM template t
        LEFT JOIN template_exercise te ON te.templateId = t.id
        GROUP BY t.id
        ORDER BY t.createdAt DESC
        """
    )
    fun observeTemplateSummaries(): Flow<List<TemplateSummaryRow>>

    @Transaction
    @Query("SELECT * FROM template WHERE id = :templateId")
    fun observeTemplateDetails(templateId: Long): Flow<TemplateWithExercises?>

    @Transaction
    @Query("SELECT * FROM template WHERE id = :templateId")
    suspend fun getTemplateDetails(templateId: Long): TemplateWithExercises?

    @Query("DELETE FROM template WHERE id = :templateId")
    suspend fun deleteTemplate(templateId: Long)

    @Query("SELECT COALESCE(MAX(orderIndex), -1) FROM template_exercise WHERE templateId = :templateId")
    suspend fun getMaxTemplateExerciseOrder(templateId: Long): Int
}
