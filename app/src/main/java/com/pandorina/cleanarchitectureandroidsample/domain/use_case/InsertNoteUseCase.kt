package com.pandorina.cleanarchitectureandroidsample.domain.use_case

import com.pandorina.cleanarchitectureandroidsample.domain.core.BaseUseCase
import com.pandorina.cleanarchitectureandroidsample.domain.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class InsertNoteUseCase(
    private val notesRepository: NotesRepository
): BaseUseCase<InsertNoteUseCase.Params, Unit> {

    data class Params(
        val title: String?
    )

    override suspend fun invoke(params: Params): Result<Unit> {
        return notesRepository.insertNote(params)
    }
}