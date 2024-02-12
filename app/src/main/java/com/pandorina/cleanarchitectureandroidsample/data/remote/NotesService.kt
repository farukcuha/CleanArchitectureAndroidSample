package com.pandorina.cleanarchitectureandroidsample.data.remote

import com.pandorina.cleanarchitectureandroidsample.data.model.request.InsertNoteRequest
import com.pandorina.cleanarchitectureandroidsample.data.model.response.GetNotesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NotesService {
    @GET("notes")
    suspend fun getNotes(): Response<GetNotesResponse?>

    @POST("notes")
    suspend fun insertNote(
        @Body insertNoteRequest: InsertNoteRequest
    ): Response<Unit>

    @DELETE("notes/{noteId}")
    suspend fun deleteNote(
        @Path("noteId") noteId: String?
    ): Response<String>

    @POST("notes/clear")
    suspend fun clearNotes(): Response<String>
}