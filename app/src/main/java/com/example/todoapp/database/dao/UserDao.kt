package com.example.todoapp.database.dao

import androidx.room.Dao
import androidx.room.Upsert
import com.example.todoapp.database.model.UserDb

@Dao
interface UserDao {

    @Upsert
    suspend fun upsertUser(userDb: UserDb)

}