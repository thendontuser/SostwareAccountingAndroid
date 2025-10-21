package com.example.sostwareaccountingandroid

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.sostwareaccountingandroid.databinding.ActivityRegistrationBinding

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var departmentAdapter: ArrayAdapter<String>
    private var departments = listOf<Department>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        setupUI()
        setupClickListeners()
        setupObservers()
        loadDepartments()
    }

    private fun setupUI() {
        // Настройка выпадающего списка для ролей
        val roles = arrayOf("Сотрудник/Преподаватель", "Администратор")
        val roleAdapter = ArrayAdapter(this, R.layout.dropdown_item, roles)
        binding.actvRole.setAdapter(roleAdapter)

        // Настройка адаптера для отделов
        departmentAdapter = ArrayAdapter(this, R.layout.dropdown_item, mutableListOf())
        binding.actvDepartment.setAdapter(departmentAdapter)
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
        val lastName = binding.etLastName.text.toString().trim()
        val firstName = binding.etFirstName.text.toString().trim()
        val patronymic = binding.etPatronymic.text.toString().trim()
        val login = binding.etLogin.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val role = binding.actvRole.text.toString().trim()
        val departmentName = binding.actvDepartment.text.toString().trim()

        if (validateInput(lastName, firstName, login, password, role, departmentName)) {
            val selectedDepartment = departments.find { it.name == departmentName }

            if (selectedDepartment != null) {
                binding.progressBar.visibility = View.VISIBLE

                val user = User(
                    firstName = firstName,
                    lastName = lastName,
                    patronymic = patronymic,
                    login = login,
                    password = password,
                    role = if (role == "Администратор") "Администратор" else "Пользователь",
                    departmentId = selectedDepartment.id
                )

                userViewModel.registerUser(user)
            } else {
                showError("Выберите отдел из списка")
            }
        }
    }

    private fun validateInput(
        lastName: String,
        firstName: String,
        login: String,
        password: String,
        role: String,
        department: String
    ): Boolean {
        var isValid = true

        if (lastName.isEmpty()) {
            binding.etLastName.error = "Введите фамилию"
            isValid = false
        }

        if (firstName.isEmpty()) {
            binding.etFirstName.error = "Введите имя"
            isValid = false
        }

        if (login.isEmpty()) {
            binding.etLogin.error = "Введите логин"
            isValid = false
        }

        if (password.length < 6) {
            binding.etPassword.error = "Пароль должен содержать минимум 6 символов"
            isValid = false
        }

        if (role.isEmpty()) {
            binding.actvRole.error = "Выберите роль"
            isValid = false
        }

        if (department.isEmpty()) {
            binding.actvDepartment.error = "Выберите отдел"
            isValid = false
        }

        return isValid
    }

    private fun loadDepartments() {
        userViewModel.getAllDepartments().observe(this) { departmentsList ->
            departments = departmentsList
            val departmentNames = departmentsList.map { it.name }
            departmentAdapter.clear()
            departmentAdapter.addAll(departmentNames)
        }
    }

    private fun setupObservers() {
        userViewModel.registrationResult.observe(this) { result ->
            binding.progressBar.visibility = View.GONE

            when (result) {
                is RegistrationResult.Success -> {
                    showSuccess("Регистрация успешна!")
                    Handler(Looper.getMainLooper()).postDelayed({
                        finish()
                    }, 1500)
                }
                is RegistrationResult.Error -> {
                    showError(result.message)
                }
            }
        }
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.success_color))
            .show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}