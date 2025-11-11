package com.example.sostwareaccountingandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sostwareaccountingandroid.repository.AuthRepository
import com.example.sostwareaccountingandroid.entity.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState: StateFlow<RegistrationState> = _registrationState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun login(login: String, password: String) {
        if (login.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Заполните все поля")
            return
        }

        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val result = authRepository.loginUser(login, password)
            when (result) {
                is AuthRepository.AuthResult.Success -> {
                    _currentUser.value = result.user
                    _loginState.value = LoginState.Success(result.user)
                }
                is AuthRepository.AuthResult.Error -> {
                    _loginState.value = LoginState.Error(result.message)
                }
            }
        }
    }

    fun registerUser(user: User) {
        if (user.firstName.isBlank() || user.lastName.isBlank() ||
            user.login.isBlank() || user.passwordHash.isBlank()) {
            _registrationState.value = RegistrationState.Error("Заполните все обязательные поля")
            return
        }

        if (user.passwordHash.length < 6) {
            _registrationState.value = RegistrationState.Error("Пароль должен содержать минимум 6 символов")
            return
        }

        _registrationState.value = RegistrationState.Loading

        viewModelScope.launch {
            val result = authRepository.registerUser(user)
            when (result) {
                is AuthRepository.AuthResult.Success -> {
                    _currentUser.value = result.user
                    _registrationState.value = RegistrationState.Success(result.user)
                }
                is AuthRepository.AuthResult.Error -> {
                    _registrationState.value = RegistrationState.Error(result.message)
                }
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _loginState.value = LoginState.Idle
        _registrationState.value = RegistrationState.Idle
    }

    fun clearErrors() {
        _loginState.value = LoginState.Idle
        _registrationState.value = RegistrationState.Idle
    }

    // Состояния для логина
    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val user: User) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    // Состояния для регистрации
    sealed class RegistrationState {
        object Idle : RegistrationState()
        object Loading : RegistrationState()
        data class Success(val user: User) : RegistrationState()
        data class Error(val message: String) : RegistrationState()
    }
}