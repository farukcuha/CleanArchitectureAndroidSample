package com.pandorina.cleanarchitectureandroidsample.data.repository

import com.pandorina.cleanarchitectureandroidsample.data.model.request.InsertNoteRequest
import com.pandorina.cleanarchitectureandroidsample.data.remote.NotesService
import com.pandorina.cleanarchitectureandroidsample.domain.model.Note
import com.pandorina.cleanarchitectureandroidsample.domain.repository.NotesRepository
import com.pandorina.cleanarchitectureandroidsample.domain.use_case.DeleteNoteUseCase
import com.pandorina.cleanarchitectureandroidsample.domain.use_case.InsertNoteUseCase
import com.pandorina.cleanarchitectureandroidsample.extensions.performHttpRequest

class NotesRepositoryImpl(
    private val notesService: NotesService
): NotesRepository {

    override suspend fun getNotes(): Result<List<Note>?> {
        return performHttpRequest(
            process = { notesService.getNotes() },
            transform = {
                it?.toDomainModel()
            }
        )
    }

    override suspend fun insertNote(params: InsertNoteUseCase.Params): Result<Unit> {
        return performHttpRequest(
            process = {
                notesService.insertNote(InsertNoteRequest(title = params.title))
            },
            transform = {}
        )
    }

    override suspend fun deleteNote(params: DeleteNoteUseCase.Params): Result<Unit> {
        return performHttpRequest(
            process = { notesService.deleteNote(
                params.noteId
            )},
            transform = {}
        )
    }

    override suspend fun clearNotes(): Result<Unit> {
        return performHttpRequest(
            process = { notesService.clearNotes() },
            transform = {}
        )
    }
}