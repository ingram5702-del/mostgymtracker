package com.mostgymapp.app.domain.repository

import com.mostgymapp.app.domain.model.Exercise
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun observeAll(): Flow<List<Exercise>>
    fun observeSearch(query: String): Flow<List<Exercise>>
}
