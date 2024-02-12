package com.pandorina.cleanarchitectureandroidsample.domain.use_case

import com.pandorina.cleanarchitectureandroidsample.domain.core.BaseUseCase
import com.pandorina.cleanarchitectureandroidsample.domain.model.Note
import com.pandorina.cleanarchitectureandroidsample.domain.repository.NotesRepository
import com.pandorina.cleanarchitectureandroidsample.ui.core.ViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetNotesUseCase(
    private val notesRepository: NotesRepository
): BaseUseCase<Unit, List<Note>?> {

    override suspend fun invoke(params: Unit): Result<List<Note>?>  {
        return notesRepository.getNotes()
    }
}