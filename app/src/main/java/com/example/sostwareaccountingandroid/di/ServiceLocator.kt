package com.example.sostwareaccountingandroid.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.example.sostwareaccountingandroid.DataBase.AppDatabase
import com.example.sostwareaccountingandroid.repository.AuthRepository
import com.example.sostwareaccountingandroid.repository.DepartmentRepository
import com.example.sostwareaccountingandroid.repository.UserRepository
import com.example.sostwareaccountingandroid.viewmodel.AuthViewModel

object ServiceLocator {
    private var database: AppDatabase? = null
    private var authRepository: AuthRepository? = null
    private var departmentRepository: DepartmentRepository? = null
    private var userRepository: UserRepository? = null

    fun initialize(context: Context) {
        database = AppDatabase.getInstance(context)
        authRepository = AuthRepository(
            database!!.userDao(),
            database!!.departmentDao()
        )
        departmentRepository = DepartmentRepository(database!!.departmentDao())
        userRepository = UserRepository(database!!.userDao())
    }

    fun getAuthRepository(): AuthRepository {
        return authRepository ?: throw IllegalStateException("ServiceLocator not initialized")
    }

    fun getAuthViewModelFactory(): ViewModelProvider.Factory {
        return AuthViewModelFactory(getAuthRepository())
    }

    fun getDepartmentRepository(): DepartmentRepository {
        return departmentRepository ?: throw IllegalStateException("ServiceLocator not initialized")
    }

    fun getUserRepository(): UserRepository {
        return userRepository ?: throw IllegalStateException("ServiceLocator not initialized")
    }
}

class AuthViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}