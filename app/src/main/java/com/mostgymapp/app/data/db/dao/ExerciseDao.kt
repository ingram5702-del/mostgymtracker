package com.mostgymapp.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mostgymapp.app.data.db.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: ExerciseEntity): Long

    @Query("SELECT * FROM exercise WHERE id = :id")
    suspend fun getById(id: Long): ExerciseEntity?

    @Query("SELECT * FROM exercise WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): ExerciseEntity?

    @Query("SELECT * FROM exercise WHERE isArchived = 0 ORDER BY name ASC")
    fun observeAllActive(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercise WHERE isArchived = 0 AND name LIKE '%' || :query || '%' ORDER BY name ASC LIMIT 50")
    fun observeSearch(query: String): Flow<List<ExerciseEntity>>

    @Query("UPDATE exercise SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)
}
