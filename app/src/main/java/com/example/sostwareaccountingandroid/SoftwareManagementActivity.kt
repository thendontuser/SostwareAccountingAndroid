package com.example.sostwareaccountingandroid

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sostwareaccountingandroid.adapter.SoftwareAdapter
import com.example.sostwareaccountingandroid.databinding.ActivitySoftwareManagementBinding
import com.example.sostwareaccountingandroid.di.ServiceLocator
import com.example.sostwareaccountingandroid.entity.Developer
import com.example.sostwareaccountingandroid.entity.Software
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class SoftwareManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySoftwareManagementBinding
    private lateinit var softwareAdapter: SoftwareAdapter
    private var developers: List<Developer> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySoftwareManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        println("DEBUG: SoftwareManagementActivity создана")

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()

        lifecycleScope.launch {
            loadData()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Управление программным обеспечением"
    }

    private fun setupRecyclerView() {
        println("DEBUG: Настройка RecyclerView для ПО")

        softwareAdapter = SoftwareAdapter(
            onEditClick = { software -> editSoftware(software) },
            onDeleteClick = { software -> deleteSoftware(software) },
            onDeveloperClick = { developerId -> filterByDeveloper(developerId) }
        )

        binding.rvSoftware.apply {
            adapter = softwareAdapter
            layoutManager = LinearLayoutManager(this@SoftwareManagementActivity)
            setHasFixedSize(true)
        }

        println("DEBUG: RecyclerView настроен")
    }

    private fun setupClickListeners() {
        binding.fabAddSoftware.setOnClickListener {
            println("DEBUG: Нажата кнопка добавления ПО")
            showAddSoftwareDialog()
        }

        binding.btnFilterFree.setOnClickListener {
            println("DEBUG: Фильтр по бесплатным лицензиям")
            filterByLicenseType("Бесплатная")
        }

        binding.btnFilterCommercial.setOnClickListener {
            println("DEBUG: Фильтр по коммерческим лицензиям")
            filterByLicenseType("Коммерческая")
        }
    }

    private suspend fun loadData() {
        println("DEBUG: Начало загрузки данных ПО")

        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = android.view.View.VISIBLE

                // Загружаем производителей
                val developerRepository = ServiceLocator.getDeveloperRepository()
                println("DEBUG: Получение разработчиков...")
                developers = developerRepository.getAllDevelopers()
                println("DEBUG: Загружено разработчиков: ${developers.size}")

                // Логируем разработчиков
                for ((index, developer) in developers.withIndex()) {
                    println("DEBUG: Разработчик $index: ID=${developer.id}, Name=${developer.name}")
                }

                // Загружаем все ПО
                loadAllSoftware()

            } catch (e: Exception) {
                println("DEBUG: Ошибка загрузки данных: ${e.message}")
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(
                        this@SoftwareManagementActivity,
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
    }

    private fun loadAllSoftware() {
        println("DEBUG: Загрузка всего ПО")

        lifecycleScope.launch {
            try {
                println("=== НАЧАЛО ЗАГРУЗКИ ПО ===")
                binding.progressBar.visibility = android.view.View.VISIBLE

                // Проверяем ServiceLocator
                println("DEBUG: Получение SoftwareRepository из ServiceLocator")
                val softwareRepository = ServiceLocator.getSoftwareRepository()
                println("DEBUG: Репозиторий ПО получен: ${softwareRepository != null}")

                // Пытаемся получить данные
                println("DEBUG: Вызов getAllSoftware()")
                val softwareList = softwareRepository.getAllSoftware()
                println("DEBUG: Получено ПО: ${softwareList.size}")

                // Логируем каждую программу для отладки
                for ((index, software) in softwareList.withIndex()) {
                    println("DEBUG: ПО $index: ID=${software.id}, Name=${software.name}, Version=${software.version}, DeveloperId=${software.developerId}")
                }

                println("DEBUG: Загружено разработчиков: ${developers.size}")
                for ((index, dev) in developers.withIndex()) {
                    println("DEBUG: Разработчик $index: ID=${dev.id}, Name=${dev.name}")
                }

                val softwareWithDevelopers = softwareList.map { software ->
                    SoftwareWithDeveloper(
                        software = software,
                        developerName = developers.find { it.id == software.developerId }?.name ?: "Не указан"
                    )
                }

                println("DEBUG: ПО с производителями: ${softwareWithDevelopers.size}")

                runOnUiThread {
                    if (::softwareAdapter.isInitialized) {
                        println("DEBUG: Адаптер инициализирован, отправляем данные")
                        softwareAdapter.submitList(softwareWithDevelopers)
                        println("DEBUG: Данные отправлены в адаптер")
                    } else {
                        println("ERROR: Адаптер не инициализирован!")
                    }
                    updateEmptyState(softwareList.isEmpty())

                    // Обновляем статистику
                    updateStatistics(softwareList)
                    println("=== ЗАГРУЗКА ПО УСПЕШНО ЗАВЕРШЕНА ===")
                }

            } catch (e: Exception) {
                println("DEBUG: Ошибка загрузки ПО: ${e.message}")
                println("DEBUG: Тип исключения: ${e.javaClass.name}")
                e.printStackTrace()

                runOnUiThread {
                    Toast.makeText(
                        this@SoftwareManagementActivity,
                        "Ошибка загрузки ПО: ${e.message}",
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

    private fun updateStatistics(softwareList: List<Software>) {
        val totalCount = softwareList.size
        val freeCount = softwareList.count { it.licenseType == "Бесплатная" }
        val commercialCount = softwareList.count { it.licenseType == "Коммерческая" }
        val openSourceCount = softwareList.count { it.licenseType == "Открытая" }

        binding.tvStats.text = "Всего: $totalCount | Бесплатных: $freeCount | Коммерческих: $commercialCount | Открытых: $openSourceCount"

        println("DEBUG: Статистика обновлена: Всего=$totalCount, Бесплатных=$freeCount, Коммерческих=$commercialCount")
    }

    private fun filterByLicenseType(licenseType: String) {
        println("DEBUG: Фильтрация по типу лицензии: $licenseType")

        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = android.view.View.VISIBLE

                val softwareRepository = ServiceLocator.getSoftwareRepository()

                // Просто вызываем метод
                val filteredSoftware = softwareRepository.getSoftwareByLicenseType(licenseType)
                println("DEBUG: Отфильтровано ПО: ${filteredSoftware.size}")

                val softwareWithDevelopers = filteredSoftware.map { software ->
                    SoftwareWithDeveloper(
                        software = software,
                        developerName = developers.find { it.id == software.developerId }?.name ?: "Не указан"
                    )
                }

                runOnUiThread {
                    if (::softwareAdapter.isInitialized) {
                        softwareAdapter.submitList(softwareWithDevelopers)
                    }
                    updateEmptyState(filteredSoftware.isEmpty())

                    // Обновляем статистику для отфильтрованного списка
                    updateStatistics(filteredSoftware)
                }

            } catch (e: Exception) {
                println("DEBUG: Ошибка фильтрации по типу лицензии: ${e.message}")
                e.printStackTrace()
            } finally {
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                }
            }
        }
    }

    private fun filterByDeveloper(developerId: Long?) {
        println("DEBUG: Фильтрация по производителю: $developerId")

        if (developerId == null) {
            loadAllSoftware()
            return
        }

        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = android.view.View.VISIBLE

                val softwareRepository = ServiceLocator.getSoftwareRepository()

                // Получаем все ПО и фильтруем
                val allSoftware = softwareRepository.getAllSoftware()
                val filteredSoftware = allSoftware.filter { it.developerId == developerId }
                println("DEBUG: Найдено ПО у производителя: ${filteredSoftware.size}")

                val softwareWithDevelopers = filteredSoftware.map { software ->
                    SoftwareWithDeveloper(
                        software = software,
                        developerName = developers.find { it.id == software.developerId }?.name ?: "Не указан"
                    )
                }

                runOnUiThread {
                    if (::softwareAdapter.isInitialized) {
                        softwareAdapter.submitList(softwareWithDevelopers)
                    }
                    updateEmptyState(filteredSoftware.isEmpty())

                    // Обновляем статистику для отфильтрованного списка
                    updateStatistics(filteredSoftware)
                }

            } catch (e: Exception) {
                println("DEBUG: Ошибка фильтрации по производителю: ${e.message}")
                e.printStackTrace()
            } finally {
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                }
            }
        }
    }

    private fun showAddSoftwareDialog() {
        println("DEBUG: Показ диалога добавления ПО")

        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_add_software, null)

        // Настраиваем выпадающие списки
        setupLicenseTypeSpinner(dialogView)
        setupDevelopersSpinner(dialogView)

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Добавить программное обеспечение")
            .setView(dialogView)
            .setPositiveButton("Добавить") { dialogInterface, _ ->
                saveNewSoftware(dialogView)
                dialogInterface.dismiss()
            }
            .setNegativeButton("Отмена") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun setupLicenseTypeSpinner(dialogView: android.view.View) {
        val spinnerLicense = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.spinnerLicenseType)
        val licenseTypes = arrayOf("Бесплатная", "Коммерческая", "Открытая")
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, licenseTypes)
        spinnerLicense?.setAdapter(adapter)
        spinnerLicense?.setText(licenseTypes[0], false)
    }

    private fun setupDevelopersSpinner(dialogView: android.view.View) {
        val spinnerDeveloper = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.spinnerDeveloper)
        val developerNames = listOf("Не указан") + developers.map { it.name }
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, developerNames)
        spinnerDeveloper?.setAdapter(adapter)
        spinnerDeveloper?.setText(developerNames[0], false)
    }

    private fun saveNewSoftware(dialogView: android.view.View) {
        val name = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etName)?.text?.toString() ?: ""
        val version = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etVersion)?.text?.toString() ?: ""
        val licenseType = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.spinnerLicenseType)?.text?.toString() ?: "Бесплатная"
        val licenseStartStr = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etLicenseStart)?.text?.toString()
        val licenseEndStr = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etLicenseEnd)?.text?.toString()
        val developerName = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.spinnerDeveloper)?.text?.toString()
        val logoPath = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etLogoPath)?.text?.toString()

        if (validateSoftwareData(name, version)) {
            lifecycleScope.launch {
                try {
                    val selectedDeveloper = developers.find { it.name == developerName }

                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    val licenseStartDate = licenseStartStr?.let { dateFormat.parse(it)?.time }
                    val licenseEndDate = licenseEndStr?.let { dateFormat.parse(it)?.time }

                    val newSoftware = Software(
                        name = name,
                        version = version,
                        licenseType = licenseType,
                        licenseStartDate = licenseStartDate,
                        licenseEndDate = licenseEndDate,
                        developerId = selectedDeveloper?.id,
                        logoPath = if (logoPath?.isNotEmpty() == true) logoPath else null
                    )

                    val softwareRepository = ServiceLocator.getSoftwareRepository()
                    val softwareId = softwareRepository.insertSoftware(newSoftware)

                    if (softwareId > 0) {
                        Toast.makeText(this@SoftwareManagementActivity, "ПО добавлено", Toast.LENGTH_SHORT).show()
                        loadAllSoftware()
                    } else {
                        Toast.makeText(this@SoftwareManagementActivity, "Ошибка добавления ПО", Toast.LENGTH_LONG).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(this@SoftwareManagementActivity, "Ошибка сохранения: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateSoftwareData(name: String, version: String): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            Toast.makeText(this, "Введите название ПО", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (version.isEmpty()) {
            Toast.makeText(this, "Введите версию ПО", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun editSoftware(softwareWithDeveloper: SoftwareWithDeveloper) {
        println("DEBUG: Редактирование ПО: ${softwareWithDeveloper.software.id} - ${softwareWithDeveloper.software.name}")

        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_edit_software, null)

        // Заполняем данные ПО
        populateEditDialog(dialogView, softwareWithDeveloper)

        // Настраиваем выпадающие списки
        setupLicenseTypeSpinner(dialogView)
        setupDevelopersSpinner(dialogView, softwareWithDeveloper.software.developerId)

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Редактировать ПО")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { dialogInterface, _ ->
                updateSoftware(softwareWithDeveloper.software, dialogView)
                dialogInterface.dismiss()
            }
            .setNegativeButton("Отмена") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun populateEditDialog(dialogView: android.view.View, softwareWithDeveloper: SoftwareWithDeveloper) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etName)?.setText(softwareWithDeveloper.software.name)
        dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etVersion)?.setText(softwareWithDeveloper.software.version)

        val spinnerLicense = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.spinnerLicenseType)
        spinnerLicense?.setText(softwareWithDeveloper.software.licenseType, false)

        if (softwareWithDeveloper.software.licenseStartDate != null) {
            val startDate = dateFormat.format(softwareWithDeveloper.software.licenseStartDate!!)
            dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etLicenseStart)?.setText(startDate)
        }

        if (softwareWithDeveloper.software.licenseEndDate != null) {
            val endDate = dateFormat.format(softwareWithDeveloper.software.licenseEndDate!!)
            dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etLicenseEnd)?.setText(endDate)
        }

        dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etLogoPath)?.setText(softwareWithDeveloper.software.logoPath ?: "")
    }

    private fun setupDevelopersSpinner(dialogView: android.view.View, selectedDeveloperId: Long?) {
        val spinnerDeveloper = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.spinnerDeveloper)
        val developerNames = listOf("Не указан") + developers.map { it.name }
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, developerNames)
        spinnerDeveloper?.setAdapter(adapter)

        val selectedDeveloper = developers.find { it.id == selectedDeveloperId }
        val selectedName = selectedDeveloper?.name ?: "Не указан"
        spinnerDeveloper?.setText(selectedName, false)
    }

    private fun updateSoftware(software: Software, dialogView: android.view.View) {
        val name = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etName)?.text?.toString() ?: ""
        val version = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etVersion)?.text?.toString() ?: ""
        val licenseType = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.spinnerLicenseType)?.text?.toString() ?: "Бесплатная"
        val licenseStartStr = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etLicenseStart)?.text?.toString()
        val licenseEndStr = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etLicenseEnd)?.text?.toString()
        val developerName = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.spinnerDeveloper)?.text?.toString()
        val logoPath = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etLogoPath)?.text?.toString()

        lifecycleScope.launch {
            try {
                val selectedDeveloper = developers.find { it.name == developerName }

                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                val licenseStartDate = licenseStartStr?.let { dateFormat.parse(it)?.time }
                val licenseEndDate = licenseEndStr?.let { dateFormat.parse(it)?.time }

                val updatedSoftware = software.copy(
                    name = name,
                    version = version,
                    licenseType = licenseType,
                    licenseStartDate = licenseStartDate,
                    licenseEndDate = licenseEndDate,
                    developerId = selectedDeveloper?.id,
                    logoPath = if (logoPath?.isNotEmpty() == true) logoPath else null
                )

                val softwareRepository = ServiceLocator.getSoftwareRepository()
                softwareRepository.updateSoftware(updatedSoftware)

                Toast.makeText(this@SoftwareManagementActivity, "ПО обновлено", Toast.LENGTH_SHORT).show()
                loadAllSoftware()

            } catch (e: Exception) {
                Toast.makeText(this@SoftwareManagementActivity, "Ошибка обновления: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteSoftware(softwareWithDeveloper: SoftwareWithDeveloper) {
        println("DEBUG: Удаление ПО: ${softwareWithDeveloper.software.id}")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Удаление ПО")
            .setMessage("Удалить программное обеспечение \"${softwareWithDeveloper.software.name}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                confirmDeleteSoftware(softwareWithDeveloper.software)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun confirmDeleteSoftware(software: Software) {
        lifecycleScope.launch {
            try {
                val softwareRepository = ServiceLocator.getSoftwareRepository()
                softwareRepository.deleteSoftware(software)

                Toast.makeText(this@SoftwareManagementActivity, "ПО удалено", Toast.LENGTH_SHORT).show()
                loadAllSoftware()

            } catch (e: Exception) {
                Toast.makeText(this@SoftwareManagementActivity, "Ошибка удаления: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.tvEmptyState.visibility = android.view.View.VISIBLE
            binding.rvSoftware.visibility = android.view.View.GONE
            println("DEBUG: Нет данных ПО, показываем Empty State")
        } else {
            binding.tvEmptyState.visibility = android.view.View.GONE
            binding.rvSoftware.visibility = android.view.View.VISIBLE
            println("DEBUG: Данные ПО есть, скрываем Empty State")
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

data class SoftwareWithDeveloper(
    val software: Software,
    val developerName: String
)