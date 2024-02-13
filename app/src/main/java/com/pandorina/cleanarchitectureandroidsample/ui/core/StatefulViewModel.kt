package com.pandorina.cleanarchitectureandroidsample.ui.core

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class StatefulViewModel<T: UiState, E: UiEvent>(uiState: T): ViewModel() {
    private val _uiState: MutableStateFlow<T> = MutableStateFlow(uiState)
    val uiState = _uiState.asStateFlow()

    init { observeErrors() }

    abstract fun onTriggerEvent(eventType: E)

    protected fun updateState(updateState: (T) -> T) = safeLaunch {
        _uiState.update { updateState(it) }
    }

    private fun logError(exception: Throwable?) {
        Log.e(this::class.java.simpleName, exception?.localizedMessage ?: "Unknown error")
    }

    private fun observeErrors() = safeLaunch {
        _uiState.collect {
            logError(it.error)
        }
    }

    protected fun safeLaunch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(CoroutineExceptionHandler { _, exception ->
            logError(exception)
        }, block = block)
    }
}