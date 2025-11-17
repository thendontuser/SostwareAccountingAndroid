package com.example.sostwareaccountingandroid.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.example.sostwareaccountingandroid.DataBase.AppDatabase
import com.example.sostwareaccountingandroid.repository.AuthRepository
import com.example.sostwareaccountingandroid.repository.DepartmentRepository
import com.example.sostwareaccountingandroid.repository.DeviceRepository
import com.example.sostwareaccountingandroid.repository.InstallationRequestRepository
import com.example.sostwareaccountingandroid.repository.SoftwareRepository
import com.example.sostwareaccountingandroid.repository.UserRepository
import com.example.sostwareaccountingandroid.viewmodel.AuthViewModel

object ServiceLocator {
    private var database: AppDatabase? = null
    private var authRepository: AuthRepository? = null
    private var departmentRepository: DepartmentRepository? = null
    private var userRepository: UserRepository? = null
    private var installationRequestRepository: InstallationRequestRepository? = null
    private var deviceRepository: DeviceRepository? = null
    private var softwareRepository: SoftwareRepository? = null

    fun initialize(context: Context) {
        database = AppDatabase.getInstance(context)
        authRepository = AuthRepository(
            database!!.userDao(),
            database!!.departmentDao()
        )
        departmentRepository = DepartmentRepository(database!!.departmentDao())
        userRepository = UserRepository(database!!.userDao())
        installationRequestRepository = InstallationRequestRepository(
            database!!.installationRequestDao())
        deviceRepository = DeviceRepository(database!!.deviceDao())
        softwareRepository = SoftwareRepository(database!!.softwareDao())
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

    fun getInstallationRequestRepository(): InstallationRequestRepository {
        return installationRequestRepository ?: throw java.lang.IllegalStateException("ServiceLocator not initialized")
    }

    fun getDeviceRepository(): DeviceRepository {
        return deviceRepository ?: throw java.lang.IllegalStateException("ServiceLocator not initialized")
    }

    fun getSoftwareRepository(): SoftwareRepository {
        return softwareRepository ?: throw java.lang.IllegalStateException("ServiceLocator not initialized")
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