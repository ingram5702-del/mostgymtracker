package com.mostgymapp.app.di

import com.mostgymapp.app.data.repository.ExerciseRepositoryImpl
import com.mostgymapp.app.data.repository.StatsRepositoryImpl
import com.mostgymapp.app.data.repository.TemplateRepositoryImpl
import com.mostgymapp.app.data.repository.WorkoutRepositoryImpl
import com.mostgymapp.app.domain.repository.ExerciseRepository
import com.mostgymapp.app.domain.repository.StatsRepository
import com.mostgymapp.app.domain.repository.TemplateRepository
import com.mostgymapp.app.domain.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(impl: WorkoutRepositoryImpl): WorkoutRepository

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(impl: ExerciseRepositoryImpl): ExerciseRepository

    @Binds
    @Singleton
    abstract fun bindTemplateRepository(impl: TemplateRepositoryImpl): TemplateRepository

    @Binds
    @Singleton
    abstract fun bindStatsRepository(impl: StatsRepositoryImpl): StatsRepository
}
