package com.pandorina.cleanarchitectureandroidsample.data.model.response

import com.pandorina.cleanarchitectureandroidsample.data.model.dto.NoteDto
import com.pandorina.cleanarchitectureandroidsample.domain.model.Note

data class GetNotesResponse(
    val size: Int?,
    val data: List<NoteDto>?
): ApiResponse<List<Note>> {
    override fun toDomainModel(): List<Note>? {
        return data?.map {
            Note(
                id = it._id,
                title = it.title,
                time = it.createdTime
            )
        }
    }
}