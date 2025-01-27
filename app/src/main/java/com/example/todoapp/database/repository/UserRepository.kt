package com.example.todoapp.database.repository

import com.example.todoapp.database.dao.UserDao
import com.example.todoapp.database.model.UserDb
import javax.inject.Inject

class UserRepository @Inject constructor(private val userDao: UserDao){

    suspend fun upsert(user: UserDb) {
        userDao.upsertUser(user)
    }

    suspend fun markUserAsSecure(id: String){
        userDao.markUserAsSecure(id)
    }

    suspend fun markUserAsNotSecure(id: String){
        userDao.markUserAsNotSecure(id)
    }

    suspend fun isUserSecure(id: String) : Int {
        return userDao.isUserSecure(id)
    }

    suspend fun isUserExists(id: String) : Int {
        return userDao.isUserExists(id)
    }
}