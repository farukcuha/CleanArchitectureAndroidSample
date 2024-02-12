package com.pandorina.cleanarchitectureandroidsample.ui.screens.notes

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.pandorina.cleanarchitectureandroidsample.domain.model.Note
import com.pandorina.cleanarchitectureandroidsample.ui.core.SnackBar
import com.pandorina.cleanarchitectureandroidsample.ui.core.ViewState
import com.pandorina.cleanarchitectureandroidsample.ui.core.ViewStateContainer
import com.pandorina.cleanarchitectureandroidsample.ui.screens.notes.components.NoteView
import com.pandorina.cleanarchitectureandroidsample.ui.theme.CleanArchitectureAndroidSampleTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NotesScreen(navController: NavHostController) {
    val viewModel: NotesViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    uiState.error?.localizedMessage ?: "",
                    withDismissAction = true
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onTriggerEvent(NotesUiEvent.GetNotes)
    }

    NotesScreenContent(
        scaffoldHostState = snackBarHostState,
        notesViewState = uiState.notesViewState,
        insertNoteViewState = uiState.insertNoteViewState,
        clearNotesViewState = uiState.clearNotesViewState,
        onClickDeleteNote = {
            viewModel.onTriggerEvent(NotesUiEvent.DeleteNote(it))
        },
        onClickClearNotes = {
            viewModel.onTriggerEvent(NotesUiEvent.ClearNotes)
        },
        title = uiState.inputText,
        onTitleChanged = { viewModel.onTriggerEvent(NotesUiEvent.UpdateInputText(it)) },
        onClickSend = {
            viewModel.onTriggerEvent(NotesUiEvent.InsertNote)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreenContent(
    notesViewState: ViewState<List<Note>> = ViewState.Success(listOf()),
    insertNoteViewState: ViewState<Unit> = ViewState.Idle,
    clearNotesViewState: ViewState<Unit> = ViewState.Idle,
    onClickDeleteNote: (String?) -> Unit = {},
    onClickClearNotes: () -> Unit = {},
    title: String = "",
    onTitleChanged: (String) -> Unit = {},
    onClickSend: () -> Unit = {},
    scaffoldHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val focusManager = LocalFocusManager.current
    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 8.dp
            ) {
                TopAppBar(
                    title = { Text(text = "Notes") },
                    actions = {
                        ViewStateContainer(
                            viewState = clearNotesViewState,
                            loadingView = { CircularProgressIndicator() },
                            contentView = {
                                Button(onClick = onClickClearNotes) {
                                    Text(text = "Clear")
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(scaffoldHostState)}
    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            ViewStateContainer(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1.0f),
                viewState = notesViewState,
                loadingView = { CircularProgressIndicator() },
                contentView = { notes ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                    ) {
                        items(notes?.size ?: 0) { index ->
                            NoteView(note = notes?.get(index)) { noteId ->
                                onClickDeleteNote(noteId)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            )
            Row(
                modifier = Modifier
                    .align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = title,
                    onValueChange = onTitleChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.85f)
                )
                ViewStateContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.15f),
                    viewState = insertNoteViewState,
                    loadingView = { CircularProgressIndicator() },
                    contentView = {
                        IconButton(onClick = {
                            onClickSend()
                            focusManager.clearFocus()
                        }) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "send")
                        }
                    }
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
fun NotesScreenContentPreview() {
    CleanArchitectureAndroidSampleTheme {
         NotesScreenContent()
    }
}


