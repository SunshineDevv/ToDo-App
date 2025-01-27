package com.example.todoapp.database.dao

import android.util.Log
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.todoapp.database.model.UserDb

@Dao
interface UserDao {

    @Upsert
    suspend fun upsertUser(userDb: UserDb)

    @Query("UPDATE users SET security_enabled = 1 WHERE userId = :id")
    suspend fun markUserAsSecure(id: String)

    @Query("UPDATE users SET security_enabled = 0 WHERE userId = :id")
    suspend fun markUserAsNotSecure(id: String)

    @Query("SELECT security_enabled FROM users WHERE userId = :id")
    suspend fun isUserSecure(id: String): Int

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE userId = :id)")
    suspend fun isUserExists(id: String): Int
}