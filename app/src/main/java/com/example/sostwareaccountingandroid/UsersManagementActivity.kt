package com.example.sostwareaccountingandroid

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sostwareaccountingandroid.adapter.UsersAdapter
import com.example.sostwareaccountingandroid.databinding.ActivityUsersManagementBinding
import com.example.sostwareaccountingandroid.di.ServiceLocator
import com.example.sostwareaccountingandroid.entity.Department
import com.example.sostwareaccountingandroid.entity.User
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UsersManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUsersManagementBinding
    private lateinit var usersAdapter: UsersAdapter
    private var departments: List<Department> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        println("МЫ В UsersManagementActivity.onCreate")
        super.onCreate(savedInstanceState)
        binding = ActivityUsersManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        println("DEBUG: UsersManagementActivity создана")

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()

        loadData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Управление пользователями"
    }

    private fun setupRecyclerView() {
        println("DEBUG: Настройка RecyclerView")

        usersAdapter = UsersAdapter(
            onEditClick = { user -> editUser(user) },
            onDeleteClick = { user -> deleteUser(user) },
            onDepartmentClick = { departmentId -> filterByDepartment(departmentId) }
        )

        binding.rvUsers.apply {
            adapter = usersAdapter
            layoutManager = LinearLayoutManager(this@UsersManagementActivity)
            setHasFixedSize(true)
        }

        println("DEBUG: RecyclerView adapter установлен: ${binding.rvUsers.adapter != null}")
    }

    private fun setupClickListeners() {
        binding.btnFilterAdmins.setOnClickListener {
            println("DEBUG: Нажата кнопка 'Администраторы'")
            filterByRole("Администратор")
        }

        binding.btnFilterUsers.setOnClickListener {
            println("DEBUG: Нажата кнопка 'Пользователи'")
            filterByRole("Пользователь")
        }
    }

    private fun loadData() {
        println("DEBUG: Начало загрузки данных")

        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = android.view.View.VISIBLE

                // Загружаем отделы
                val departmentRepository = ServiceLocator.getDepartmentRepository()
                departments = departmentRepository.getAllDepartments()
                println("DEBUG: Загружено отделов: ${departments.size}")

                // После загрузки отделов загружаем пользователей
                loadAllUsers()

            } catch (e: Exception) {
                println("DEBUG: Ошибка загрузки данных: ${e.message}")
                e.printStackTrace()
                Toast.makeText(
                    this@UsersManagementActivity,
                    "Ошибка загрузки данных",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }

    private fun loadAllUsers() {
        println("DEBUG: Загрузка всех пользователей")

        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = android.view.View.VISIBLE

                val userRepository = ServiceLocator.getUserRepository()
                println("DEBUG: Репозиторий получен: $userRepository")

                // Используем новый метод для получения всех пользователей
                userRepository.getAllUsers().collectLatest { users ->
                    println("DEBUG: Получены пользователи: ${users.size}")

                    // Логируем каждого пользователя
                    for ((index, user) in users.withIndex()) {
                        println("DEBUG: Пользователь $index: ID=${user.id}, Имя=${user.firstName} ${user.lastName}, Отдел=${user.departmentId}")
                    }

                    val usersWithDepartments = users.map { user ->
                        UserWithDepartment(
                            user = user,
                            departmentName = departments.find { it.id == user.departmentId }?.name ?: "Не указан"
                        )
                    }

                    println("DEBUG: Пользователи с отделами: ${usersWithDepartments.size}")

                    runOnUiThread {
                        if (::usersAdapter.isInitialized) {
                            usersAdapter.submitList(usersWithDepartments)
                            println("DEBUG: Данные отправлены в адаптер")
                        }
                        updateEmptyState(users.isEmpty())
                    }
                }

            } catch (e: Exception) {
                println("DEBUG: Ошибка загрузки пользователей: ${e.message}")
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@UsersManagementActivity,
                        "Ошибка загрузки пользователей",
                        Toast.LENGTH_LONG).show()
                }
            } finally {
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                }
            }
        }
    }

    private fun filterByRole(role: String) {
        Toast.makeText(this, "Фильтрация по роли: $role", Toast.LENGTH_SHORT).show()
        println("DEBUG: Фильтрация по роли: $role")

        // TODO: Реализовать реальную фильтрацию
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = android.view.View.VISIBLE

                val userRepository = ServiceLocator.getUserRepository()
                userRepository.getUsersByDepartment(0).collectLatest { users ->
                    val filteredUsers = users.filter { it.role == role }
                    val usersWithDepartments = filteredUsers.map { user ->
                        UserWithDepartment(
                            user = user,
                            departmentName = departments.find { it.id == user.departmentId }?.name ?: "Не указан"
                        )
                    }

                    if (::usersAdapter.isInitialized) {
                        usersAdapter.submitList(usersWithDepartments)
                    }

                    updateEmptyState(filteredUsers.isEmpty())
                }
            } catch (e: Exception) {
                println("DEBUG: Ошибка фильтрации по роли: ${e.message}")
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }

    private fun filterByDepartment(departmentId: Long?) {
        println("DEBUG: Фильтрация по отделу: $departmentId")

        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = android.view.View.VISIBLE

                val userRepository = ServiceLocator.getUserRepository()

                userRepository.getUsersByDepartment(departmentId).collectLatest { users ->
                    val usersWithDepartments = users.map { user ->
                        UserWithDepartment(
                            user = user,
                            departmentName = departments.find { it.id == user.departmentId }?.name ?: "Не указан"
                        )
                    }

                    if (::usersAdapter.isInitialized) {
                        usersAdapter.submitList(usersWithDepartments)
                    }

                    updateEmptyState(users.isEmpty())
                }

            } catch (e: Exception) {
                println("DEBUG: Ошибка фильтрации по отделу: ${e.message}")
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }

    private fun setupDepartmentSpinner(dialog: android.app.AlertDialog) {
        val spinner = dialog.findViewById<Spinner>(R.id.spinnerDepartment)
        val departmentNames = listOf("Не указан") + departments.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departmentNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner?.adapter = adapter
    }

    private fun setupRoleSpinner(dialog: android.app.AlertDialog) {
        val spinner = dialog.findViewById<Spinner>(R.id.spinnerRole)
        val roles = listOf("Пользователь", "Администратор")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner?.adapter = adapter
    }

    private fun validateUserData(firstName: String, lastName: String, login: String, password: String): Boolean {
        var isValid = true

        if (firstName.isEmpty()) {
            Toast.makeText(this, "Введите имя", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (lastName.isEmpty()) {
            Toast.makeText(this, "Введите фамилию", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (login.isEmpty()) {
            Toast.makeText(this, "Введите логин", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (password.length < 6) {
            Toast.makeText(this, "Пароль должен быть не менее 6 символов", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun editUser(userWithDepartment: UserWithDepartment) {
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Редактировать пользователя")
            .setView(R.layout.dialog_edit_user)
            .setPositiveButton("Сохранить") { dialog, _ ->
                updateUser(userWithDepartment.user, dialog as android.app.AlertDialog)
            }
            .setNegativeButton("Отмена", null)
            .create()

        dialog.show()

        populateEditDialog(dialog, userWithDepartment)
    }

    private fun populateEditDialog(dialog: android.app.AlertDialog, userWithDepartment: UserWithDepartment) {
        dialog.findViewById<EditText>(R.id.etFirstName)?.setText(userWithDepartment.user.firstName)
        dialog.findViewById<EditText>(R.id.etLastName)?.setText(userWithDepartment.user.lastName)
        dialog.findViewById<EditText>(R.id.etPatronymic)?.setText(userWithDepartment.user.patronymic ?: "")
        dialog.findViewById<EditText>(R.id.etLogin)?.setText(userWithDepartment.user.login)

        setupRoleSpinner(dialog, userWithDepartment.user.role)
        setupDepartmentSpinner(dialog, userWithDepartment.user.departmentId)
    }

    private fun setupRoleSpinner(dialog: android.app.AlertDialog, selectedRole: String) {
        val spinner = dialog.findViewById<Spinner>(R.id.spinnerRole)
        val roles = listOf("Пользователь", "Администратор")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner?.adapter = adapter

        val position = roles.indexOfFirst { it == selectedRole }
        if (position >= 0) spinner?.setSelection(position)
    }

    private fun setupDepartmentSpinner(dialog: android.app.AlertDialog, selectedDepartmentId: Long?) {
        val spinner = dialog.findViewById<Spinner>(R.id.spinnerDepartment)
        val departmentNames = listOf("Не указан") + departments.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departmentNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner?.adapter = adapter

        val selectedDepartment = departments.find { it.id == selectedDepartmentId }
        val position = if (selectedDepartment != null) {
            departments.indexOfFirst { it.id == selectedDepartmentId } + 1 // +1 из-за "Не указан"
        } else {
            0
        }
        spinner?.setSelection(position)
    }

    private fun updateUser(user: User, dialog: android.app.AlertDialog) {
        val firstName = dialog.findViewById<EditText>(R.id.etFirstName)?.text?.toString()?.trim() ?: ""
        val lastName = dialog.findViewById<EditText>(R.id.etLastName)?.text?.toString()?.trim() ?: ""
        val patronymic = dialog.findViewById<EditText>(R.id.etPatronymic)?.text?.toString()?.trim() ?: ""
        val login = dialog.findViewById<EditText>(R.id.etLogin)?.text?.toString()?.trim() ?: ""
        val role = dialog.findViewById<Spinner>(R.id.spinnerRole)?.selectedItem?.toString() ?: "Пользователь"
        val departmentSpinner = dialog.findViewById<Spinner>(R.id.spinnerDepartment)
        val departmentName = departmentSpinner?.selectedItem?.toString()

        if (firstName.isEmpty() || lastName.isEmpty() || login.isEmpty()) {
            Toast.makeText(this, "Заполните обязательные поля", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val selectedDepartment = departments.find { it.name == departmentName }

                val updatedUser = user.copy(
                    firstName = firstName,
                    lastName = lastName,
                    patronymic = if (patronymic.isNotEmpty()) patronymic else null,
                    login = login,
                    role = role,
                    departmentId = selectedDepartment?.id
                )

                val userRepository = ServiceLocator.getUserRepository()
                val result = userRepository.updateUser(updatedUser)

                if (result.isSuccess) {
                    Toast.makeText(this@UsersManagementActivity, "Пользователь обновлен", Toast.LENGTH_SHORT).show()
                    loadAllUsers()
                } else {
                    Toast.makeText(this@UsersManagementActivity, "Ошибка: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@UsersManagementActivity, "Ошибка обновления: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteUser(userWithDepartment: UserWithDepartment) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Удаление пользователя")
            .setMessage("Удалить пользователя ${userWithDepartment.user.getFullName()}?")
            .setPositiveButton("Удалить") { _, _ ->
                confirmDeleteUser(userWithDepartment.user)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun confirmDeleteUser(user: User) {
        lifecycleScope.launch {
            try {
                val userRepository = ServiceLocator.getUserRepository()
                userRepository.deleteUser(user)

                Toast.makeText(this@UsersManagementActivity, "Пользователь удален", Toast.LENGTH_SHORT).show()
                loadAllUsers()
            } catch (e: Exception) {
                Toast.makeText(this@UsersManagementActivity, "Ошибка удаления: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.tvEmptyState.visibility = android.view.View.VISIBLE
            binding.rvUsers.visibility = android.view.View.GONE
            println("DEBUG: Нет данных, показываем Empty State")
        } else {
            binding.tvEmptyState.visibility = android.view.View.GONE
            binding.rvUsers.visibility = android.view.View.VISIBLE
            println("DEBUG: Данные есть, скрываем Empty State")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                println("DEBUG: Нажата кнопка 'Назад'")
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

data class UserWithDepartment(
    val user: User,
    val departmentName: String
)