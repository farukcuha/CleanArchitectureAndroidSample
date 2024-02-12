package com.pandorina.cleanarchitectureandroidsample.data.model.response

import com.pandorina.cleanarchitectureandroidsample.data.model.dto.NoteDto

data class InsertNoteResponse(
    val message: String?,
    val note: NoteDto?
): ApiResponse<Boolean> {

    override fun toDomainModel(): Boolean {
        return note != null
    }
}