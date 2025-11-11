package com.example.sostwareaccountingandroid

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sostwareaccountingandroid.databinding.ActivityRegistrationBinding
import com.example.sostwareaccountingandroid.di.ServiceLocator
import com.example.sostwareaccountingandroid.entity.User
import com.example.sostwareaccountingandroid.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private val authViewModel: AuthViewModel by viewModels { ServiceLocator.getAuthViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        setupClickListeners()
    }

    private fun setupUI() {
        // Настройка выпадающего списка для ролей
        val roles = arrayOf("Пользователь", "Администратор")
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        binding.actvRole.setAdapter(roleAdapter)

        // TODO: Загрузить отделы из базы данных
        val departments = arrayOf("Факультет информационных технологий", "Факультет математики")
        val departmentAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, departments)
        binding.actvDepartment.setAdapter(departmentAdapter)
    }

    private fun setupObservers() {
        // Наблюдаем за состоянием регистрации с помощью StateFlow
        lifecycleScope.launch {
            authViewModel.registrationState.collect { state ->
                when (state) {
                    is AuthViewModel.RegistrationState.Loading -> {
                        showLoading(true)
                    }
                    is AuthViewModel.RegistrationState.Success -> {
                        showLoading(false)
                        showSuccess("Регистрация успешна!")
                        // Возвращаемся к логину через 2 секунды
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            finish()
                        }, 2000)
                    }
                    is AuthViewModel.RegistrationState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                    is AuthViewModel.RegistrationState.Idle -> {
                        showLoading(false)
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            attemptRegistration()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun attemptRegistration() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val patronymic = binding.etPatronymic.text.toString().trim()
        val login = binding.etLogin.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val role = binding.actvRole.text.toString().trim()
        val departmentName = binding.actvDepartment.text.toString().trim()

        if (validateInput(firstName, lastName, login, password, role, departmentName)) {
            // TODO: Получить departmentId по имени отдела
            val departmentId = 1L // Временное значение

            val user = User(
                firstName = firstName,
                lastName = lastName,
                patronymic = patronymic.ifEmpty { null },
                login = login,
                passwordHash = password, // Будет захешировано в репозитории
                role = role,
                departmentId = departmentId
            )

            authViewModel.registerUser(user)
        }
    }

    private fun validateInput(
        firstName: String,
        lastName: String,
        login: String,
        password: String,
        role: String,
        department: String
    ): Boolean {
        var isValid = true

        if (firstName.isEmpty()) {
            binding.etFirstName.error = "Введите имя"
            isValid = false
        } else {
            binding.etFirstName.error = null
        }

        if (lastName.isEmpty()) {
            binding.etLastName.error = "Введите фамилию"
            isValid = false
        } else {
            binding.etLastName.error = null
        }

        if (login.isEmpty()) {
            binding.etLogin.error = "Введите логин"
            isValid = false
        } else {
            binding.etLogin.error = null
        }

        if (password.length < 6) {
            binding.etPassword.error = "Пароль должен содержать минимум 6 символов"
            isValid = false
        } else {
            binding.etPassword.error = null
        }

        if (role.isEmpty()) {
            binding.actvRole.error = "Выберите роль"
            isValid = false
        } else {
            binding.actvRole.error = null
        }

        if (department.isEmpty()) {
            binding.actvDepartment.error = "Выберите отдел"
            isValid = false
        } else {
            binding.actvDepartment.error = null
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnRegister.isEnabled = !show
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}