package com.pandorina.cleanarchitectureandroidsample.ui.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch

@Composable
inline fun <reified T> ViewStateContainer(
    modifier: Modifier = Modifier,
    viewState: ViewState<T> = ViewState.Idle,
    loadingView: @Composable () -> Unit = {},
    contentView: @Composable (data: T?) -> Unit = {}
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when(viewState) {
            is ViewState.Loading -> { loadingView() }
            is ViewState.Success -> { contentView(viewState.data) }
            else -> { contentView(null) }
        }
    }
}