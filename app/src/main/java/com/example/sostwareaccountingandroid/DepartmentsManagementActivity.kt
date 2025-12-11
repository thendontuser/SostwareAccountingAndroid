package com.example.sostwareaccountingandroid

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sostwareaccountingandroid.adapter.DepartmentsAdapter
import com.example.sostwareaccountingandroid.databinding.ActivityDepartmentsManagementBinding
import com.example.sostwareaccountingandroid.di.ServiceLocator
import com.example.sostwareaccountingandroid.entity.Department
import kotlinx.coroutines.launch

class DepartmentsManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDepartmentsManagementBinding
    private lateinit var departmentsAdapter: DepartmentsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDepartmentsManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        println("DEBUG: DepartmentsManagementActivity создана")

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()

        // Инициализируем ServiceLocator если нужно
        initServiceLocator()

        // Запускаем загрузку данных в корутине
        lifecycleScope.launch {
            loadDepartments()
        }
    }

    private fun initServiceLocator() {
        try {
            // Проверяем, инициализирован ли ServiceLocator
            ServiceLocator.getDepartmentRepository()
        } catch (e: IllegalStateException) {
            println("DEBUG: ServiceLocator не инициализирован, инициализируем...")
            ServiceLocator.initialize(applicationContext)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Управление отделами"
    }

    private fun setupRecyclerView() {
        println("DEBUG: Настройка RecyclerView для отделов")

        departmentsAdapter = DepartmentsAdapter(
            onEditClick = { department -> editDepartment(department) },
            onDeleteClick = { department -> deleteDepartment(department) }
        )

        binding.rvDepartments.apply {
            adapter = departmentsAdapter
            layoutManager = LinearLayoutManager(this@DepartmentsManagementActivity)
            setHasFixedSize(true)
        }

        println("DEBUG: RecyclerView настроен")
    }

    private fun setupClickListeners() {
        binding.fabAddDepartment.setOnClickListener {
            println("DEBUG: Нажата кнопка добавления отдела")
            showAddDepartmentDialog()
        }
    }

    private suspend fun loadDepartments() {
        println("DEBUG: Загрузка отделов")

        try {
            runOnUiThread {
                binding.progressBar.visibility = android.view.View.VISIBLE
            }

            val departmentRepository = ServiceLocator.getDepartmentRepository()
            println("DEBUG: Получение всех отделов...")

            // Просто получаем List<Department>
            val departments = departmentRepository.getAllDepartments()
            println("DEBUG: Получено отделов: ${departments.size}")

            // Логируем каждый отдел для отладки
            for ((index, department) in departments.withIndex()) {
                println("DEBUG: Отдел $index: ID=${department.id}, Name='${department.name}'")
            }

            val departmentsWithStats = mutableListOf<DepartmentWithStats>()

            for (department in departments) {
                val userCount = departmentRepository.getUserCountInDepartment(department.id)
                val deviceCount = departmentRepository.getDeviceCountInDepartment(department.id)

                departmentsWithStats.add(
                    DepartmentWithStats(
                        department = department,
                        userCount = userCount,
                        deviceCount = deviceCount
                    )
                )
            }

            println("DEBUG: Отделы со статистикой: ${departmentsWithStats.size}")

            runOnUiThread {
                departmentsAdapter.submitList(departmentsWithStats)
                updateEmptyState(departments.isEmpty())
                updateStatistics(departmentsWithStats)
            }

        } catch (e: Exception) {
            println("DEBUG: Ошибка загрузки отделов: ${e.message}")
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(
                    this@DepartmentsManagementActivity,
                    "Ошибка загрузки данных: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } finally {
            runOnUiThread {
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }

    private fun sortDepartmentsByName() {
        val currentList = departmentsAdapter.currentList.toMutableList()
        val sortedList = currentList.sortedBy { it.department.name }
        departmentsAdapter.submitList(sortedList)
        Toast.makeText(this, "Отсортировано по названию", Toast.LENGTH_SHORT).show()
        println("DEBUG: Отсортировано по названию, элементов: ${sortedList.size}")
    }

    private fun sortDepartmentsByUserCount() {
        val currentList = departmentsAdapter.currentList.toMutableList()
        val sortedList = currentList.sortedByDescending { it.userCount }
        departmentsAdapter.submitList(sortedList)
        Toast.makeText(this, "Отсортировано по количеству сотрудников", Toast.LENGTH_SHORT).show()
        println("DEBUG: Отсортировано по сотрудникам, элементов: ${sortedList.size}")
    }

    private fun updateStatistics(departmentsWithStats: List<DepartmentWithStats>) {
        val totalDepartments = departmentsWithStats.size
        val totalUsers = departmentsWithStats.sumOf { it.userCount }
        val totalDevices = departmentsWithStats.sumOf { it.deviceCount }

        val avgUsersPerDepartment = if (totalDepartments > 0) {
            totalUsers / totalDepartments
        } else {
            0
        }

        val avgDevicesPerDepartment = if (totalDepartments > 0) {
            totalDevices / totalDepartments
        } else {
            0
        }

        binding.tvStats.text = "Отделов: $totalDepartments | Сотрудников: $totalUsers | Устройств: $totalDevices | Ср. сотрудников: $avgUsersPerDepartment | Ср. устройств: $avgDevicesPerDepartment"

        println("DEBUG: Статистика обновлена: Отделов=$totalDepartments, Сотрудников=$totalUsers, Устройств=$totalDevices")
    }

    private fun showAddDepartmentDialog() {
        println("DEBUG: Показ диалога добавления отдела")

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Добавить отдел")
            .setView(R.layout.dialog_add_department)
            .setPositiveButton("Добавить") { dialogInterface, _ ->
                saveNewDepartment(dialogInterface as android.app.AlertDialog)
            }
            .setNegativeButton("Отмена") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun saveNewDepartment(dialog: android.app.AlertDialog) {
        val name = dialog.findViewById<android.widget.EditText>(R.id.etDepartmentName)?.text?.toString() ?: ""

        if (validateDepartmentData(name)) {
            lifecycleScope.launch {
                try {
                    val newDepartment = Department(name = name)
                    val departmentRepository = ServiceLocator.getDepartmentRepository()
                    val departmentId = departmentRepository.insert(newDepartment)

                    if (departmentId > 0) {
                        Toast.makeText(this@DepartmentsManagementActivity, "Отдел добавлен", Toast.LENGTH_SHORT).show()
                        loadDepartments()
                    } else {
                        Toast.makeText(this@DepartmentsManagementActivity, "Ошибка добавления отдела", Toast.LENGTH_LONG).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(this@DepartmentsManagementActivity, "Ошибка сохранения: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateDepartmentData(name: String): Boolean {
        if (name.isEmpty()) {
            Toast.makeText(this, "Введите название отдела", Toast.LENGTH_SHORT).show()
            return false
        }

        if (name.length < 2) {
            Toast.makeText(this, "Название отдела должно быть не менее 2 символов", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun editDepartment(departmentWithStats: DepartmentWithStats) {
        println("DEBUG: Редактирование отдела: ${departmentWithStats.department.id} - ${departmentWithStats.department.name}")

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Редактировать отдел")
            .setView(R.layout.dialog_edit_department)
            .setPositiveButton("Сохранить") { dialogInterface, _ ->
                updateDepartment(departmentWithStats.department, dialogInterface as android.app.AlertDialog)
            }
            .setNegativeButton("Отмена") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()

        // Заполняем данные отдела
        dialog.findViewById<android.widget.EditText>(R.id.etDepartmentName)?.setText(departmentWithStats.department.name)
    }

    private fun updateDepartment(department: Department, dialog: android.app.AlertDialog) {
        val name = dialog.findViewById<android.widget.EditText>(R.id.etDepartmentName)?.text?.toString() ?: ""

        if (validateDepartmentData(name)) {
            lifecycleScope.launch {
                try {
                    val updatedDepartment = department.copy(name = name)
                    val departmentRepository = ServiceLocator.getDepartmentRepository()
                    departmentRepository.update(updatedDepartment)

                    Toast.makeText(this@DepartmentsManagementActivity, "Отдел обновлен", Toast.LENGTH_SHORT).show()
                    loadDepartments()

                } catch (e: Exception) {
                    Toast.makeText(this@DepartmentsManagementActivity, "Ошибка обновления: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun deleteDepartment(departmentWithStats: DepartmentWithStats) {
        println("DEBUG: Удаление отдела: ${departmentWithStats.department.id}")

        // Проверяем, есть ли сотрудники или устройства в отделе
        if (departmentWithStats.userCount > 0 || departmentWithStats.deviceCount > 0) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Невозможно удалить отдел")
                .setMessage("""
                    Невозможно удалить отдел "${departmentWithStats.department.name}"!
                    
                    В отделе находится:
                    • Сотрудников: ${departmentWithStats.userCount}
                    • Устройств: ${departmentWithStats.deviceCount}
                    
                    Переместите сотрудников и устройства в другие отделы перед удалением.
                """.trimIndent())
                .setPositiveButton("ОК", null)
                .show()
            return
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Удаление отдела")
            .setMessage("Удалить отдел \"${departmentWithStats.department.name}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                confirmDeleteDepartment(departmentWithStats.department)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun confirmDeleteDepartment(department: Department) {
        lifecycleScope.launch {
            try {
                val departmentRepository = ServiceLocator.getDepartmentRepository()
                departmentRepository.delete(department)

                Toast.makeText(this@DepartmentsManagementActivity, "Отдел удален", Toast.LENGTH_SHORT).show()
                loadDepartments()

            } catch (e: Exception) {
                Toast.makeText(this@DepartmentsManagementActivity, "Ошибка удаления: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.tvEmptyState.visibility = android.view.View.VISIBLE
            binding.rvDepartments.visibility = android.view.View.GONE
            println("DEBUG: Нет данных отделов, показываем Empty State")
        } else {
            binding.tvEmptyState.visibility = android.view.View.GONE
            binding.rvDepartments.visibility = android.view.View.VISIBLE
            println("DEBUG: Данные отделов есть, скрываем Empty State")
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

// Data class для отображения отдела со статистикой
data class DepartmentWithStats(
    val department: Department,
    val userCount: Int,
    val deviceCount: Int
)