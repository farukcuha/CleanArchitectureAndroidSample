package com.pandorina.cleanarchitectureandroidsample.ui.core

sealed class ViewState<out T> {
    object Idle : ViewState<Nothing>()
    object Loading : ViewState<Nothing>()
    data class Success<out T>(val data: T? = null) : ViewState<T>()
    data class Error(val error: Throwable? = null) : ViewState<Nothing>()
}