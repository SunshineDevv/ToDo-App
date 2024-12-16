package com.example.todoapp.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.todoapp.database.model.UserWithNotes
import kotlinx.coroutines.flow.Flow

@Dao
interface UserWithNotesDao {
    @Transaction
    @Query("SELECT * FROM users WHERE userId = :userId")
    fun getUserWithNotes(userId: String): Flow<UserWithNotes?>
}