package com.pandorina.cleanarchitectureandroidsample.ui.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class BaseViewModel<T: UiState, E: UiEvent>(uiState: T): ViewModel() {
    private val _uiState: MutableStateFlow<T> = MutableStateFlow(uiState)
    val uiState = _uiState.asStateFlow()

    init {
        observeErrors()
    }

    abstract fun onTriggerEvent(eventType: E)

    protected fun updateState(updateState: (T) -> T) = safeLaunch {
        _uiState.update { updateState(it) }
    }

    private fun logError(exception: Throwable?) {
        Timber.tag(this::class.java.simpleName).e(exception)
    }

    private fun observeErrors() = safeLaunch {
        _uiState.collectLatest { logError(it.error) }
    }

    protected fun safeLaunch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(CoroutineExceptionHandler { _, exception ->
            logError(exception)
        }, block = block)
    }
}