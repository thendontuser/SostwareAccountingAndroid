package com.example.sostwareaccountingandroid

import android.os.Bundle
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sostwareaccountingandroid.databinding.ActivityRegistrationBinding
import com.example.sostwareaccountingandroid.di.ServiceLocator
import com.example.sostwareaccountingandroid.entity.Department
import com.example.sostwareaccountingandroid.entity.User
import com.example.sostwareaccountingandroid.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import android.os.Handler

class RegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding
    private val authViewModel: AuthViewModel by viewModels { ServiceLocator.getAuthViewModelFactory() }
    private lateinit var departmentAdapter: ArrayAdapter<String>
    private var departments = listOf<Department>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        setupClickListeners()
        loadDepartments()
    }

    private fun setupUI() {
        // Настройка выпадающего списка для ролей
        val roles = arrayOf("Пользователь", "Администратор")
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        binding.actvRole.setAdapter(roleAdapter)

        // Инициализация адаптера для отделов
        departmentAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mutableListOf())
        binding.actvDepartment.setAdapter(departmentAdapter)
    }

    private fun setupObservers() {
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
                        Handler(Looper.getMainLooper()).postDelayed({
                            setResult(RESULT_OK)
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

    private fun loadDepartments() {
        lifecycleScope.launch {
            // Загружаем отделы из базы данных
            val departmentRepo = ServiceLocator.getDepartmentRepository()
            departments = departmentRepo.getAllDepartments()
            val departmentNames = departments.map { it.name }
            departmentAdapter.clear()
            departmentAdapter.addAll(departmentNames)

            // Если отделы загружены, выбираем первый по умолчанию
            if (departmentNames.isNotEmpty()) {
                binding.actvDepartment.setText(departmentNames[0], false)
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
            lifecycleScope.launch {
                // Ищем ID отдела по имени
                val departmentRepo = ServiceLocator.getDepartmentRepository()
                val department = departmentRepo.getDepartmentByName(departmentName)

                if (department != null) {
                    val user = User(
                        firstName = firstName,
                        lastName = lastName,
                        patronymic = patronymic.ifEmpty { null },
                        login = login,
                        passwordHash = password, // Будет захешировано в репозитории
                        role = role,
                        departmentId = department.id
                    )
                    authViewModel.registerUser(user)
                } else {
                    showError("Выбранный отдел не найден")
                }
            }
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