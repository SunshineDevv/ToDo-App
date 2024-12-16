package com.example.todoapp.database.repository

import com.example.todoapp.database.dao.UserWithNotesDao
import com.example.todoapp.database.model.UserWithNotes
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserWithNotesRepository @Inject constructor(private val userWithNotesDao: UserWithNotesDao) {

    fun getUserWithNotes(userId: String): Flow<UserWithNotes?> {
        return userWithNotesDao.getUserWithNotes(userId)
    }
}