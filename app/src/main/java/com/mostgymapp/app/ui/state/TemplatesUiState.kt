package com.mostgymapp.app.ui.state

import com.mostgymapp.app.domain.model.TemplateSummary

data class TemplatesUiState(
    val isLoading: Boolean = true,
    val templates: List<TemplateSummary> = emptyList(),
    val errorMessage: String? = null
)
