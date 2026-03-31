package com.mostgymapp.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mostgymapp.app.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

sealed class AppState {
    data object Loading : AppState()
    data class WebView(val url: String) : AppState()
    data object NormalApp : AppState()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("webview_prefs", Context.MODE_PRIVATE)

    private val _appState = MutableStateFlow<AppState>(AppState.Loading)
    val appState: StateFlow<AppState> = _appState

    init {
        checkUrl()
    }

    private fun checkUrl() {
        viewModelScope.launch {
            val url = withTimeoutOrNull(10_000L) {
                firestoreRepository.getWebViewUrl()
            }

            when {
                url != null -> {
                    prefs.edit().putString("cached_url", url).apply()
                    _appState.value = AppState.WebView(url)
                }
                else -> {
                    val cached = prefs.getString("cached_url", null)
                    if (!cached.isNullOrBlank()) {
                        _appState.value = AppState.WebView(cached)
                    } else {
                        _appState.value = AppState.NormalApp
                    }
                }
            }
        }
    }
}
