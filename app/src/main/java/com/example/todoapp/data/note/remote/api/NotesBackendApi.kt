package com.example.todoapp.data.note.remote.api

import com.example.todoapp.data.note.remote.dto.CreateNoteRequest
import com.example.todoapp.data.note.remote.dto.NoteSingleResponse
import com.example.todoapp.data.note.remote.dto.NotesResponse
import com.example.todoapp.data.note.remote.dto.UpdateNoteRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface NotesBackendApi {

    @GET("api/notes")
    suspend fun getNotes(): NotesResponse

    @POST("api/notes")
    suspend fun createNote(
        @Body request: CreateNoteRequest
    ): NoteSingleResponse

    @PATCH("api/notes/{id}")
    suspend fun updateNote(
        @Path("id") id: String,
        @Body request: UpdateNoteRequest
    ): NoteSingleResponse

    @DELETE("api/notes/{id}")
    suspend fun deleteNote(
        @Path("id") id: String
    ): NoteSingleResponse
}