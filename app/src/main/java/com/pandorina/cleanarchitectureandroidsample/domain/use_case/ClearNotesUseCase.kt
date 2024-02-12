package com.pandorina.cleanarchitectureandroidsample.domain.use_case

import com.pandorina.cleanarchitectureandroidsample.domain.core.BaseUseCase
import com.pandorina.cleanarchitectureandroidsample.domain.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ClearNotesUseCase(
    private val repository: NotesRepository
): BaseUseCase<Unit, Unit> {

    override suspend fun invoke(params: Unit): Result<Unit> {
        return repository.clearNotes()
    }
}
