package com.example.todoapp.data.legacy.repository

import com.example.todoapp.data.local.database.dao.UserDao
import com.example.todoapp.data.local.database.entity.UserDb
import javax.inject.Inject

class UserRepository @Inject constructor(private val userDao: UserDao){

    suspend fun upsert(user: UserDb) {
        userDao.upsertUser(user)
    }
}