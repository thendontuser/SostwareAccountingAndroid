package com.example.sostwareaccountingandroid.repository

import com.example.sostwareaccountingandroid.dao.DepartmentDao
import com.example.sostwareaccountingandroid.dao.UserDao
import com.example.sostwareaccountingandroid.entity.User
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest

class AuthRepository(private val userDao: UserDao, private val departmentDao: DepartmentDao) {

    suspend fun registerUser(user: User): AuthResult {
        return try {
            // Проверяем, существует ли логин
            val existingUser = userDao.getUserByLogin(user.login)
            if (existingUser != null) {
                AuthResult.Error("Пользователь с таким логином уже существует")
            } else {
                // Хэшируем пароль и сохраняем пользователя
                val userWithHashedPassword = user.copy(
                    passwordHash = hashPassword(user.passwordHash)
                )
                val userId = userDao.insert(userWithHashedPassword)
                AuthResult.Success(userWithHashedPassword.copy(id = userId))
            }
        } catch (e: Exception) {
            AuthResult.Error("Ошибка при регистрации: ${e.message}")
        }
    }

    suspend fun loginUser(login: String, password: String): AuthResult {
        return try {
            val user = userDao.getUserByLogin(login)
            if (user != null && verifyPassword(password, user.passwordHash)) {
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Неверный логин или пароль")
            }
        } catch (e: Exception) {
            AuthResult.Error("Ошибка при входе: ${e.message}")
        }
    }

    suspend fun updateUser(user: User): AuthResult {
        return try {
            userDao.update(user)
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error("Ошибка при обновлении пользователя: ${e.message}")
        }
    }

    fun getUserById(userId: Long): Flow<User?> {
        // Для наблюдения за изменениями пользователя
        // В реальном приложении нужно было бы создать Flow запрос
        // Сейчас вернем пустой Flow, реализуем позже при необходимости
        return kotlinx.coroutines.flow.flow {
            emit(userDao.getUserById(userId))
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    private fun verifyPassword(password: String, hash: String): Boolean {
        return hashPassword(password) == hash
    }

    sealed class AuthResult {
        data class Success(val user: User) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }
}