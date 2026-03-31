package com.mostgymapp.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import com.mostgymapp.app.data.db.WorkoutLogDatabase
import com.mostgymapp.app.data.db.dao.ExerciseDao
import com.mostgymapp.app.data.db.dao.SetDao
import com.mostgymapp.app.data.db.dao.TemplateDao
import com.mostgymapp.app.data.db.dao.WorkoutDao
import com.mostgymapp.app.data.db.migration.MIGRATION_1_2
import com.mostgymapp.app.data.db.migration.MIGRATION_2_3
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WorkoutLogDatabase =
        Room.databaseBuilder(context, WorkoutLogDatabase::class.java, "workout_log.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    @Provides
    fun provideWorkoutDao(database: WorkoutLogDatabase): WorkoutDao = database.workoutDao()

    @Provides
    fun provideExerciseDao(database: WorkoutLogDatabase): ExerciseDao = database.exerciseDao()

    @Provides
    fun provideSetDao(database: WorkoutLogDatabase): SetDao = database.setDao()

    @Provides
    fun provideTemplateDao(database: WorkoutLogDatabase): TemplateDao = database.templateDao()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("workout_log.preferences_pb") }
        )
}
