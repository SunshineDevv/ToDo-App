package com.example.todoapp.database.repository

import com.example.todoapp.database.dao.UserDao
import com.example.todoapp.database.model.UserDb
import javax.inject.Inject

class UserRepository @Inject constructor(private val userDao: UserDao){

    suspend fun upsert(user: UserDb) {
        userDao.upsertUser(user)
    }
}