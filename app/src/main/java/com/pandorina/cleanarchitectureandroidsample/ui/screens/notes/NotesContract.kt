package com.pandorina.cleanarchitectureandroidsample.ui.screens.notes

import com.pandorina.cleanarchitectureandroidsample.domain.model.Note
import com.pandorina.cleanarchitectureandroidsample.ui.core.UiEvent
import com.pandorina.cleanarchitectureandroidsample.ui.core.UiState
import com.pandorina.cleanarchitectureandroidsample.ui.core.ViewState

sealed class NotesUiEvent: UiEvent {
    object GetNotes: NotesUiEvent()
    object RefreshNotes: NotesUiEvent()
    object InsertNote: NotesUiEvent()
    data class DeleteNote(val noteId: String?): NotesUiEvent()
    object ClearNotes: NotesUiEvent()
    data class UpdateInputText(val text: String): NotesUiEvent()
}

data class NotesUiState(
    override val error: Throwable? = null,
    val notesViewState: ViewState<List<Note>> = ViewState.Idle,
    val insertNoteViewState: ViewState<Unit> = ViewState.Idle,
    val deleteNoteViewState: ViewState<Unit> = ViewState.Idle,
    val clearNotesViewState: ViewState<Unit> = ViewState.Idle,
    val inputText: String = ""
): UiState {

    fun ableToInsertNote(): Boolean {
        return inputText.isNotEmpty()
    }
}