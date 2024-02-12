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
    viewState: ViewState<T>,
    loadingView: @Composable () -> Unit = {},
    contentView: @Composable (data: T?) -> Unit = {}
) {

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when(viewState) {
            is ViewState.Loading -> {
                loadingView()
            }
            is ViewState.Success -> {
                contentView(viewState.data)
            }
            is ViewState.Idle -> {
                contentView(null)
            }
            else -> {
                contentView(null)
            }
        }
    }
}

@Composable
fun SnackBar(
    modifier: Modifier = Modifier,
    message: String,
    show: Boolean,
    showSnackBar: (Boolean) -> Unit
) {
    val snackState = remember { SnackbarHostState() }
    val snackScope = rememberCoroutineScope()

    SnackbarHost(
        modifier = modifier,
        hostState = snackState
    ){
        Snackbar(
            snackbarData = it
        )
    }
    if (show){
        LaunchedEffect(Unit) {
            snackScope.launch { snackState.showSnackbar(message) }
            showSnackBar(false)
        }
    }
}