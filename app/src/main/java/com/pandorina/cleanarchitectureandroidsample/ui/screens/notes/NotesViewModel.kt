package com.pandorina.cleanarchitectureandroidsample.ui.screens.notes

import com.pandorina.cleanarchitectureandroidsample.domain.use_case.ClearNotesUseCase
import com.pandorina.cleanarchitectureandroidsample.domain.use_case.DeleteNoteUseCase
import com.pandorina.cleanarchitectureandroidsample.domain.use_case.GetNotesUseCase
import com.pandorina.cleanarchitectureandroidsample.domain.use_case.InsertNoteUseCase
import com.pandorina.cleanarchitectureandroidsample.ui.core.StatefulViewModel
import com.pandorina.cleanarchitectureandroidsample.ui.core.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val getNotesUseCase: GetNotesUseCase,
    private val insertNoteUseCase: InsertNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val clearNotesUseCase: ClearNotesUseCase,
): StatefulViewModel<NotesUiState, NotesUiEvent>(NotesUiState()) {

    override fun onTriggerEvent(eventType: NotesUiEvent) {
        when(eventType) {
            is NotesUiEvent.GetNotes -> getNotes()
            is NotesUiEvent.RefreshNotes -> refreshNotes()
            is NotesUiEvent.InsertNote -> insertNote()
            is NotesUiEvent.DeleteNote -> deleteNote(eventType.noteId)
            is NotesUiEvent.ClearNotes -> clearNotes()
            is NotesUiEvent.UpdateInputText -> updateTitleToInsert(eventType.text)
        }
    }

    private fun updateTitleToInsert(title: String) = updateState { it.copy(inputText = title) }

    private fun getNotes() = safeLaunch {
        updateState { it.copy(notesViewState = ViewState.Loading) }
        onTriggerEvent(NotesUiEvent.RefreshNotes)
    }

    private fun refreshNotes() = safeLaunch {
        getNotesUseCase(Unit).onSuccess { notes ->
            updateState { it.copy(notesViewState = ViewState.Success(notes)) }
        }.onFailure { error ->
            updateState {
                it.copy(notesViewState = ViewState.Error(error), error = error)
            }
        }
    }

    private fun insertNote() = safeLaunch {
        if (uiState.value.ableToInsertNote().not()) return@safeLaunch
        updateState { it.copy(insertNoteViewState = ViewState.Loading) }
        insertNoteUseCase(InsertNoteUseCase.Params(uiState.value.inputText)).onSuccess {
            updateState { it.copy(insertNoteViewState = ViewState.Success(), inputText = "") }
            onTriggerEvent(NotesUiEvent.RefreshNotes)
        }.onFailure { error ->
            updateState {
                it.copy(insertNoteViewState = ViewState.Error(error), error = error)
            }
        }
    }

    private fun deleteNote(noteId: String?) = safeLaunch {
        updateState { it.copy(deleteNoteViewState = ViewState.Loading) }
        deleteNoteUseCase(DeleteNoteUseCase.Params(noteId)).onSuccess {
            updateState { it.copy(deleteNoteViewState = ViewState.Success()) }
            onTriggerEvent(NotesUiEvent.RefreshNotes)
        }.onFailure { error ->
            updateState {
                it.copy(deleteNoteViewState = ViewState.Error(error), error = error)
            }
        }
    }

    private fun clearNotes() = safeLaunch {
        updateState { it.copy(clearNotesViewState = ViewState.Loading) }
        clearNotesUseCase(Unit).onSuccess {
            updateState { it.copy(clearNotesViewState = ViewState.Success()) }
            onTriggerEvent(NotesUiEvent.RefreshNotes)
        }.onFailure { error ->
            updateState { it.copy(clearNotesViewState = ViewState.Error(error), error = error) }
        }
    }
}