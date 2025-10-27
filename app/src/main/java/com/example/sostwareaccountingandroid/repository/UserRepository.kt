package com.example.sostwareaccountingandroid.repository

import com.example.sostwareaccountingandroid.DataBase.PasswordHasher
import com.example.sostwareaccountingandroid.dao.UserDao
import com.example.sostwareaccountingandroid.entity.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    suspend fun registerUser(user: User): Result<Long> {
        return try {
            // Проверка уникальности логина
            val existingUser = userDao.getUserByLogin(user.login)
            if (existingUser != null) {
                Result.failure(Exception("Пользователь с таким логином уже существует"))
            } else {
                val userId = userDao.insert(user.copy(passwordHash = PasswordHasher.hashPassword(user.passwordHash)))
                Result.success(userId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(login: String, password: String): Result<User> {
        return try {
            val user = userDao.getUserByLogin(login)
            if (user != null && PasswordHasher.verifyPassword(password, user.passwordHash)) {
                Result.success(user)
            } else {
                Result.failure(Exception("Неверный логин или пароль"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUsersByDepartment(departmentId: Long): Flow<List<User>> {
        return userDao.getUsersByDepartment(departmentId)
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            userDao.update(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}