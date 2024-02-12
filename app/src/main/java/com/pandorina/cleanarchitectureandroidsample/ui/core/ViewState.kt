package com.pandorina.cleanarchitectureandroidsample.ui.core

sealed class ViewState<out T> {
    object Idle : ViewState<Nothing>()
    object Loading : ViewState<Nothing>()
    data class Success<out T>(val data: T? = null) : ViewState<T>()
    data class Error(val error: Throwable? = null) : ViewState<Nothing>()

    fun isLoading(): Boolean {
        return this is Loading
    }

    fun isSuccessful(): Boolean {
        return this is Success
    }

    fun onSuccess(process: () -> Unit) {
        if (this is Success) process()
    }

    fun onError(process: () -> Unit) {
        if (this is Error) process()
    }
}