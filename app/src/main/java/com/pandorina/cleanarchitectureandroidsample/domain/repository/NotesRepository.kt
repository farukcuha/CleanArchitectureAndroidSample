package com.pandorina.cleanarchitectureandroidsample.domain.repository

import com.pandorina.cleanarchitectureandroidsample.domain.model.Note
import com.pandorina.cleanarchitectureandroidsample.domain.use_case.DeleteNoteUseCase
import com.pandorina.cleanarchitectureandroidsample.domain.use_case.InsertNoteUseCase

interface NotesRepository {
    suspend fun getNotes(): Result<List<Note>?>

    suspend fun insertNote(
        params: InsertNoteUseCase.Params
    ): Result<Unit>

    suspend fun deleteNote(
        params: DeleteNoteUseCase.Params
    ): Result<Unit>

    suspend fun clearNotes(): Result<Unit>
}