package com.mostgymapp.app.data.repository

import com.mostgymapp.app.data.db.dao.ExerciseDao
import com.mostgymapp.app.domain.model.Exercise
import com.mostgymapp.app.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {

    override fun observeAll(): Flow<List<Exercise>> =
        exerciseDao.observeAllActive().map { entities ->
            entities.map { Exercise(id = it.id, name = it.name) }
        }

    override fun observeSearch(query: String): Flow<List<Exercise>> {
        val source = if (query.isBlank()) exerciseDao.observeAllActive() else exerciseDao.observeSearch(query)
        return source.map { entities -> entities.map { Exercise(id = it.id, name = it.name) } }
    }
}
