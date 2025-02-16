package com.example.todoapp.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.todoapp.database.model.UserDb

@Dao
interface UserDao {

    @Upsert
    suspend fun upsertUser(userDb: UserDb)

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE userId = :id)")
    suspend fun isUserExists(id: String): Int
}