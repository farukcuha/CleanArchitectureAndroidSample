package com.pandorina.cleanarchitectureandroidsample.domain.use_case

import com.pandorina.cleanarchitectureandroidsample.domain.core.BaseUseCase
import com.pandorina.cleanarchitectureandroidsample.domain.repository.NotesRepository
import com.pandorina.cleanarchitectureandroidsample.ui.core.ViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteNoteUseCase(
    private val repository: NotesRepository
) : BaseUseCase<DeleteNoteUseCase.Params, Unit> {

    data class Params(
        val noteId: String?
    )

    override suspend fun invoke(params: Params): Result<Unit> {
        return repository.deleteNote(params)
    }
}