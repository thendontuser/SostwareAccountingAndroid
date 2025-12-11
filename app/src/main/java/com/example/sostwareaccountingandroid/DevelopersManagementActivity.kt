package com.example.sostwareaccountingandroid

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sostwareaccountingandroid.adapter.DevelopersAdapter
import com.example.sostwareaccountingandroid.databinding.ActivityDevelopersManagementBinding
import com.example.sostwareaccountingandroid.di.ServiceLocator
import com.example.sostwareaccountingandroid.entity.Developer
import kotlinx.coroutines.launch

class DevelopersManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDevelopersManagementBinding
    private lateinit var developersAdapter: DevelopersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDevelopersManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        println("DEBUG: DevelopersManagementActivity создана")

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()

        lifecycleScope.launch {
            loadDevelopers()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Управление производителями"
    }

    private fun setupRecyclerView() {
        println("DEBUG: Настройка RecyclerView для производителей")

        developersAdapter = DevelopersAdapter(
            onEditClick = { developer -> editDeveloper(developer) },
            onDeleteClick = { developer -> deleteDeveloper(developer) }
        )

        binding.rvDevelopers.apply {
            adapter = developersAdapter
            layoutManager = LinearLayoutManager(this@DevelopersManagementActivity)
            setHasFixedSize(true)
        }

        println("DEBUG: RecyclerView настроен")
    }

    private fun setupClickListeners() {
        binding.fabAddDeveloper.setOnClickListener {
            println("DEBUG: Нажата кнопка добавления производителя")
            showAddDeveloperDialog()
        }
    }

    private fun loadDevelopers() {
        println("DEBUG: Загрузка производителей")

        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = android.view.View.VISIBLE

                val developerRepository = ServiceLocator.getDeveloperRepository()
                // Просто получаем List<Developer>
                val developers = developerRepository.getAllDevelopers()
                println("DEBUG: Получено производителей: ${developers.size}")

                // Логируем каждого производителя для отладки
                for ((index, developer) in developers.withIndex()) {
                    println("DEBUG: Производитель $index: ${developer.id} - ${developer.name}")
                }

                val developersWithStats = mutableListOf<DeveloperWithStats>()

                for (developer in developers) {
                    val softwareCount = developerRepository.getSoftwareCountByDeveloper(developer.id)

                    developersWithStats.add(
                        DeveloperWithStats(
                            developer = developer,
                            softwareCount = softwareCount
                        )
                    )
                }

                println("DEBUG: Производители со статистикой: ${developersWithStats.size}")

                runOnUiThread {
                    developersAdapter.submitList(developersWithStats)
                    updateEmptyState(developers.isEmpty())
                    updateStatistics(developersWithStats)
                }

            } catch (e: Exception) {
                println("DEBUG: Ошибка загрузки производителей: ${e.message}")
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(
                        this@DevelopersManagementActivity,
                        "Ошибка загрузки данных",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                }
            }
        }
    }

    private fun filterByCompanyType(companyType: String) {
        println("DEBUG: Фильтрация по типу компании: $companyType")

        // Получаем текущий список
        val currentList = developersAdapter.currentList.toMutableList()
        val filteredList = currentList.filter { it.developer.companyType == companyType }

        println("DEBUG: Отфильтровано производителей: ${filteredList.size}")

        developersAdapter.submitList(filteredList)
        updateEmptyState(filteredList.isEmpty())

        // Обновляем статистику для отфильтрованного списка
        updateStatistics(filteredList)
    }

    private fun updateStatistics(developersWithStats: List<DeveloperWithStats>) {
        val totalDevelopers = developersWithStats.size
        val totalSoftware = developersWithStats.sumOf { it.softwareCount }

        val oooCount = developersWithStats.count { it.developer.companyType == "ООО" }
        val ipCount = developersWithStats.count { it.developer.companyType == "ИП" }
        val corporationCount = developersWithStats.count { it.developer.companyType == "Корпорация" }

        val avgSoftwarePerDeveloper = if (totalDevelopers > 0) {
            totalSoftware / totalDevelopers
        } else {
            0
        }

        binding.tvStats.text = "Производителей: $totalDevelopers | Программ: $totalSoftware | ООО: $oooCount | ИП: $ipCount | Корпораций: $corporationCount | Ср. программ: $avgSoftwarePerDeveloper"

        println("DEBUG: Статистика обновлена: Производителей=$totalDevelopers, Программ=$totalSoftware")
    }

    private fun showAddDeveloperDialog() {
        println("DEBUG: Показ диалога добавления производителя")

        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_add_developer, null)

        // Настраиваем выпадающий список типов компаний
        setupCompanyTypeSpinner(dialogView)

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Добавить производителя")
            .setView(dialogView)
            .setPositiveButton("Добавить") { dialogInterface, _ ->
                saveNewDeveloper(dialogView)
                dialogInterface.dismiss()
            }
            .setNegativeButton("Отмена") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun setupCompanyTypeSpinner(dialogView: android.view.View) {
        val spinnerCompanyType = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.spinnerCompanyType)
        val companyTypes = arrayOf("ООО", "ИП", "Корпорация", "Другое")
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, companyTypes)
        spinnerCompanyType?.setAdapter(adapter)
        spinnerCompanyType?.setText(companyTypes[0], false)
    }

    private fun saveNewDeveloper(dialogView: android.view.View) {
        val name = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etName)?.text?.toString() ?: ""
        val companyType = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.spinnerCompanyType)?.text?.toString() ?: "ООО"
        val location = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etLocation)?.text?.toString()

        if (validateDeveloperData(name)) {
            lifecycleScope.launch {
                try {
                    val newDeveloper = Developer(
                        name = name,
                        companyType = companyType,
                        location = if (location?.isNotEmpty() == true) location else null
                    )

                    val developerRepository = ServiceLocator.getDeveloperRepository()
                    val developerId = developerRepository.insertDeveloper(newDeveloper)

                    if (developerId > 0) {
                        Toast.makeText(this@DevelopersManagementActivity, "Производитель добавлен", Toast.LENGTH_SHORT).show()
                        loadDevelopers()
                    } else {
                        Toast.makeText(this@DevelopersManagementActivity, "Ошибка добавления производителя", Toast.LENGTH_LONG).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(this@DevelopersManagementActivity, "Ошибка сохранения: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateDeveloperData(name: String): Boolean {
        if (name.isEmpty()) {
            Toast.makeText(this, "Введите название производителя", Toast.LENGTH_SHORT).show()
            return false
        }

        if (name.length < 2) {
            Toast.makeText(this, "Название производителя должно быть не менее 2 символов", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun editDeveloper(developerWithStats: DeveloperWithStats) {
        println("DEBUG: Редактирование производителя: ${developerWithStats.developer.id} - ${developerWithStats.developer.name}")

        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_edit_developer, null)

        // Заполняем данные производителя
        populateEditDialog(dialogView, developerWithStats)

        // Настраиваем выпадающий список типов компаний
        setupCompanyTypeSpinner(dialogView)

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Редактировать производителя")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { dialogInterface, _ ->
                updateDeveloper(developerWithStats.developer, dialogView)
                dialogInterface.dismiss()
            }
            .setNegativeButton("Отмена") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun populateEditDialog(dialogView: android.view.View, developerWithStats: DeveloperWithStats) {
        dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etName)?.setText(developerWithStats.developer.name)
        dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.spinnerCompanyType)?.setText(developerWithStats.developer.companyType, false)
        dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etLocation)?.setText(developerWithStats.developer.location ?: "")
    }

    private fun updateDeveloper(developer: Developer, dialogView: android.view.View) {
        val name = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etName)?.text?.toString() ?: ""
        val companyType = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.spinnerCompanyType)?.text?.toString() ?: "ООО"
        val location = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etLocation)?.text?.toString()

        lifecycleScope.launch {
            try {
                val updatedDeveloper = developer.copy(
                    name = name,
                    companyType = companyType,
                    location = if (location?.isNotEmpty() == true) location else null
                )

                val developerRepository = ServiceLocator.getDeveloperRepository()
                developerRepository.updateDeveloper(updatedDeveloper)

                Toast.makeText(this@DevelopersManagementActivity, "Производитель обновлен", Toast.LENGTH_SHORT).show()
                loadDevelopers()

            } catch (e: Exception) {
                Toast.makeText(this@DevelopersManagementActivity, "Ошибка обновления: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteDeveloper(developerWithStats: DeveloperWithStats) {
        println("DEBUG: Удаление производителя: ${developerWithStats.developer.id}")

        // Проверяем, есть ли программное обеспечение у производителя
        if (developerWithStats.softwareCount > 0) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Невозможно удалить производителя")
                .setMessage("""
                    Невозможно удалить производителя "${developerWithStats.developer.name}"!
                    
                    У производителя есть программное обеспечение:
                    • Программ: ${developerWithStats.softwareCount}
                    
                    Удалите или переместите ПО перед удалением производителя.
                """.trimIndent())
                .setPositiveButton("ОК", null)
                .show()
            return
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Удаление производителя")
            .setMessage("Удалить производителя \"${developerWithStats.developer.name}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                confirmDeleteDeveloper(developerWithStats.developer)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun confirmDeleteDeveloper(developer: Developer) {
        lifecycleScope.launch {
            try {
                val developerRepository = ServiceLocator.getDeveloperRepository()
                developerRepository.deleteDeveloper(developer)

                Toast.makeText(this@DevelopersManagementActivity, "Производитель удален", Toast.LENGTH_SHORT).show()
                loadDevelopers()

            } catch (e: Exception) {
                Toast.makeText(this@DevelopersManagementActivity, "Ошибка удаления: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.tvEmptyState.visibility = android.view.View.VISIBLE
            binding.rvDevelopers.visibility = android.view.View.GONE
            println("DEBUG: Нет данных производителей, показываем Empty State")
        } else {
            binding.tvEmptyState.visibility = android.view.View.GONE
            binding.rvDevelopers.visibility = android.view.View.VISIBLE
            println("DEBUG: Данные производителей есть, скрываем Empty State")
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

// Data class для отображения производителя со статистикой
data class DeveloperWithStats(
    val developer: Developer,
    val softwareCount: Int
)