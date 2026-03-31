package com.mostgymapp.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mostgymapp.app.domain.model.TemplateDetail
import com.mostgymapp.app.domain.repository.TemplateRepository
import com.mostgymapp.app.domain.usecase.CreateTemplateFromWorkoutUseCase
import com.mostgymapp.app.domain.usecase.StartWorkoutFromTemplateUseCase
import com.mostgymapp.app.ui.state.TemplatesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TemplatesViewModel @Inject constructor(
    private val templateRepository: TemplateRepository,
    private val createTemplateFromWorkoutUseCase: CreateTemplateFromWorkoutUseCase,
    private val startWorkoutFromTemplateUseCase: StartWorkoutFromTemplateUseCase
) : ViewModel() {

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private val _startWorkoutEvents = MutableSharedFlow<Long>(extraBufferCapacity = 1)
    val startWorkoutEvents = _startWorkoutEvents.asSharedFlow()

    val uiState: StateFlow<TemplatesUiState> = templateRepository.observeTemplates()
        .map { templates ->
            TemplatesUiState(
                isLoading = false,
                templates = templates
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TemplatesUiState(isLoading = true)
        )

    fun observeTemplateDetails(templateId: Long): Flow<TemplateDetail?> =
        templateRepository.observeTemplateDetails(templateId)

    fun createFromWorkout(workoutId: Long, name: String, note: String?) {
        viewModelScope.launch {
            runCatching {
                createTemplateFromWorkoutUseCase(workoutId, name, note)
                _events.emit("Template created")
            }.onFailure {
                _events.emit(it.message ?: "Failed to create template")
            }
        }
    }

    fun deleteTemplate(templateId: Long) {
        viewModelScope.launch {
            runCatching {
                templateRepository.deleteTemplate(templateId)
                _events.emit("Template deleted")
            }.onFailure {
                _events.emit(it.message ?: "Failed to delete template")
            }
        }
    }

    fun startTemplate(templateId: Long) {
        viewModelScope.launch {
            runCatching {
                val workoutId = startWorkoutFromTemplateUseCase(templateId)
                _startWorkoutEvents.emit(workoutId)
            }.onFailure {
                _events.emit(it.message ?: "Failed to start template")
            }
        }
    }
}
