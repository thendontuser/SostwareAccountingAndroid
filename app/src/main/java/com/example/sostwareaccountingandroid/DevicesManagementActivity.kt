package com.example.sostwareaccountingandroid

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sostwareaccountingandroid.adapter.DevicesAdapter
import com.example.sostwareaccountingandroid.databinding.ActivityDevicesManagementBinding
import com.example.sostwareaccountingandroid.di.ServiceLocator
import com.example.sostwareaccountingandroid.entity.Department
import com.example.sostwareaccountingandroid.entity.Device
import kotlinx.coroutines.launch

class DevicesManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDevicesManagementBinding
    private lateinit var devicesAdapter: DevicesAdapter
    private var departments: List<Department> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDevicesManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        println("DEBUG: DevicesManagementActivity создана")

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()

        // Запускаем загрузку данных в корутине
        lifecycleScope.launch {
            loadData()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Управление устройствами"
    }

    private fun setupRecyclerView() {
        println("DEBUG: Настройка RecyclerView для устройств")

        devicesAdapter = DevicesAdapter(
            onEditClick = { device -> editDevice(device) },
            onDeleteClick = { device -> deleteDevice(device) },
            onDepartmentClick = { departmentId -> filterByDepartment(departmentId) }
        )

        binding.rvDevices.apply {
            adapter = devicesAdapter
            layoutManager = LinearLayoutManager(this@DevicesManagementActivity)
            setHasFixedSize(true)
        }

        println("DEBUG: RecyclerView настроен")
    }

    private fun setupClickListeners() {
        binding.fabAddDevice.setOnClickListener {
            println("DEBUG: Нажата кнопка добавления устройства")
            showAddDeviceDialog()
        }

        binding.btnFilterAll.setOnClickListener {
            println("DEBUG: Нажата кнопка 'Все'")
            lifecycleScope.launch {
                loadAllDevices()
            }
        }

        binding.btnFilterWindows.setOnClickListener {
            println("DEBUG: Фильтр по Windows")
            lifecycleScope.launch {
                filterByOS("Windows")
            }
        }

        binding.btnFilterLinux.setOnClickListener {
            println("DEBUG: Фильтр по Linux")
            lifecycleScope.launch {
                filterByOS("Linux")
            }
        }
    }

    private fun loadData() {
        println("DEBUG: Начало загрузки данных устройств")

        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = android.view.View.VISIBLE

                // Загружаем отделы
                val departmentRepository = ServiceLocator.getDepartmentRepository()
                println("DEBUG: Получение отделов...")
                departments = departmentRepository.getAllDepartments()
                println("DEBUG: Загружено отделов: ${departments.size}")

                // Логируем отделы для отладки
                for ((index, department) in departments.withIndex()) {
                    println("DEBUG: Отдел $index: ID=${department.id}, Name=${department.name}")
                }

                // Загружаем все устройства
                loadAllDevices()

            } catch (e: Exception) {
                println("DEBUG: Ошибка загрузки данных: ${e.message}")
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(
                        this@DevicesManagementActivity,
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

    private suspend fun loadAllDevices() {
        println("DEBUG: Загрузка всех устройств")

        lifecycleScope.launch {
            try {
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                }

                val deviceRepository = ServiceLocator.getDeviceRepository()
                println("DEBUG: Получение всех устройств...")
                val deviceList = deviceRepository.getAllDevices()
                println("DEBUG: Получено устройств: ${deviceList.size}")

                // Логируем каждое устройство для отладки
                for ((index, device) in deviceList.withIndex()) {
                    println("DEBUG: Устройство $index: ID=${device.id}, Name='${device.name}', OS='${device.osName}'")
                }

                val devicesWithDepartments = deviceList.map { device ->
                    DeviceWithDepartment(
                        device = device,
                        departmentName = departments.find { it.id == device.departmentId }?.name ?: "Не указан"
                    )
                }

                println("DEBUG: Устройства с отделами: ${devicesWithDepartments.size}")

                runOnUiThread {
                    if (::devicesAdapter.isInitialized) {
                        devicesAdapter.submitList(devicesWithDepartments)
                        println("DEBUG: Данные отправлены в адаптер")
                    }
                    updateEmptyState(deviceList.isEmpty())

                    // Обновляем статистику
                    updateStatistics(deviceList)
                }

            } catch (e: Exception) {
                println("DEBUG: Ошибка загрузки устройств: ${e.message}")
                e.printStackTrace()
            } finally {
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                }
            }
        }
    }

    private fun updateStatistics(deviceList: List<Device>) {
        val totalCount = deviceList.size
        val windowsCount = deviceList.count { it.osName.contains("Windows", true) }
        val linuxCount = deviceList.count { it.osName.contains("Linux", true) }
        val macCount = deviceList.count { it.osName.contains("macOS", true) || it.osName.contains("Mac", true) }

        val avgRam = if (deviceList.isNotEmpty()) {
            deviceList.map { it.ramSize }.average().toInt()
        } else {
            0
        }

        binding.tvStats.text = "Всего: $totalCount | Windows: $windowsCount | Linux: $linuxCount | macOS: $macCount | Средняя RAM: ${avgRam}ГБ"

        println("DEBUG: Статистика обновлена: Всего=$totalCount, Windows=$windowsCount, Linux=$linuxCount")
    }

    private suspend fun filterByOS(osName: String) {
        println("DEBUG: Фильтрация по ОС: $osName")

        try {
            runOnUiThread {
                binding.progressBar.visibility = android.view.View.VISIBLE
            }

            val deviceRepository = ServiceLocator.getDeviceRepository()
            val allDevices = deviceRepository.getAllDevices()
            val filteredDevices = allDevices.filter { it.osName.contains(osName, true) }

            println("DEBUG: Отфильтровано устройств: ${filteredDevices.size}")

            val devicesWithDepartments = filteredDevices.map { device ->
                DeviceWithDepartment(
                    device = device,
                    departmentName = departments.find { it.id == device.departmentId }?.name ?: "Не указан"
                )
            }

            runOnUiThread {
                if (::devicesAdapter.isInitialized) {
                    devicesAdapter.submitList(devicesWithDepartments)
                }
                updateEmptyState(filteredDevices.isEmpty())

                // Обновляем статистику для отфильтрованного списка
                updateStatistics(filteredDevices)
            }

        } catch (e: Exception) {
            println("DEBUG: Ошибка фильтрации по ОС: ${e.message}")
            e.printStackTrace()
        } finally {
            runOnUiThread {
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }

    private fun filterByDepartment(departmentId: Long?) {
        println("DEBUG: Фильтрация по отделу: $departmentId")

        if (departmentId == null) {
            lifecycleScope.launch {
                loadAllDevices()
            }
            return
        }

        lifecycleScope.launch {
            try {
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                }

                val deviceRepository = ServiceLocator.getDeviceRepository()
                val allDevices = deviceRepository.getAllDevices()
                val filteredDevices = allDevices.filter { it.departmentId == departmentId }

                println("DEBUG: Найдено устройств в отделе: ${filteredDevices.size}")

                val devicesWithDepartments = filteredDevices.map { device ->
                    DeviceWithDepartment(
                        device = device,
                        departmentName = departments.find { it.id == device.departmentId }?.name ?: "Не указан"
                    )
                }

                runOnUiThread {
                    if (::devicesAdapter.isInitialized) {
                        devicesAdapter.submitList(devicesWithDepartments)
                    }
                    updateEmptyState(filteredDevices.isEmpty())
                }

            } catch (e: Exception) {
                println("DEBUG: Ошибка фильтрации по отделу: ${e.message}")
                e.printStackTrace()
            } finally {
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                }
            }
        }
    }

    private fun showAddDeviceDialog() {
        println("DEBUG: Показ диалога добавления устройства")

        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_add_device, null)

        // Настраиваем выпадающий список отделов
        setupDepartmentsSpinner(dialogView)

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Добавить устройство")
            .setView(dialogView)
            .setPositiveButton("Добавить") { dialogInterface, _ ->
                saveNewDevice(dialogView)
                dialogInterface.dismiss()
            }
            .setNegativeButton("Отмена") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun setupDepartmentsSpinner(dialogView: android.view.View) {
        val spinnerDepartment = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.spinnerDepartment)
        val departmentNames = listOf("Не указан") + departments.map { it.name }
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, departmentNames)
        spinnerDepartment?.setAdapter(adapter)
        spinnerDepartment?.setText(departmentNames[0], false)
    }

    private fun saveNewDevice(dialogView: android.view.View) {
        val name = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etName)?.text?.toString() ?: ""
        val osName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etOS)?.text?.toString() ?: ""
        val ipAddress = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etIP)?.text?.toString()
        val ramSizeStr = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etRAM)?.text?.toString() ?: ""
        val departmentName = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.spinnerDepartment)?.text?.toString()

        if (validateDeviceData(name, osName, ramSizeStr)) {
            lifecycleScope.launch {
                try {
                    val selectedDepartment = departments.find { it.name == departmentName }
                    val ramSize = ramSizeStr.toIntOrNull() ?: 0

                    val newDevice = Device(
                        name = name,
                        osName = osName,
                        ipAddress = if (ipAddress?.isNotEmpty() == true) ipAddress else null,
                        ramSize = ramSize,
                        departmentId = selectedDepartment?.id
                    )

                    val deviceRepository = ServiceLocator.getDeviceRepository()
                    val deviceId = deviceRepository.insertDevice(newDevice)

                    if (deviceId > 0) {
                        Toast.makeText(this@DevicesManagementActivity, "Устройство добавлено", Toast.LENGTH_SHORT).show()
                        loadAllDevices()
                    } else {
                        Toast.makeText(this@DevicesManagementActivity, "Ошибка добавления устройства", Toast.LENGTH_LONG).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(this@DevicesManagementActivity, "Ошибка сохранения: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateDeviceData(name: String, osName: String, ramSizeStr: String): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            Toast.makeText(this, "Введите название устройства", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (osName.isEmpty()) {
            Toast.makeText(this, "Введите название ОС", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (ramSizeStr.isEmpty()) {
            Toast.makeText(this, "Введите объем RAM", Toast.LENGTH_SHORT).show()
            isValid = false
        } else {
            val ramSize = ramSizeStr.toIntOrNull()
            if (ramSize == null || ramSize <= 0) {
                Toast.makeText(this, "Введите корректный объем RAM", Toast.LENGTH_SHORT).show()
                isValid = false
            }
        }

        return isValid
    }

    private fun editDevice(deviceWithDepartment: DeviceWithDepartment) {
        println("DEBUG: Редактирование устройства: ${deviceWithDepartment.device.id} - ${deviceWithDepartment.device.name}")

        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_edit_device, null)

        // Заполняем данные устройства
        populateEditDialog(dialogView, deviceWithDepartment)

        // Настраиваем выпадающий список отделов
        setupDepartmentsSpinner(dialogView, deviceWithDepartment.device.departmentId)

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Редактировать устройство")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { dialogInterface, _ ->
                updateDevice(deviceWithDepartment.device, dialogView)
                dialogInterface.dismiss()
            }
            .setNegativeButton("Отмена") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun populateEditDialog(dialogView: android.view.View, deviceWithDepartment: DeviceWithDepartment) {
        dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etName)?.setText(deviceWithDepartment.device.name)
        dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etOS)?.setText(deviceWithDepartment.device.osName)
        dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etIP)?.setText(deviceWithDepartment.device.ipAddress ?: "")
        dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etRAM)?.setText(deviceWithDepartment.device.ramSize.toString())
    }

    private fun setupDepartmentsSpinner(dialogView: android.view.View, selectedDepartmentId: Long?) {
        val spinnerDepartment = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.spinnerDepartment)
        val departmentNames = listOf("Не указан") + departments.map { it.name }
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, departmentNames)
        spinnerDepartment?.setAdapter(adapter)

        val selectedDepartment = departments.find { it.id == selectedDepartmentId }
        val selectedName = selectedDepartment?.name ?: "Не указан"
        spinnerDepartment?.setText(selectedName, false)
    }

    private fun updateDevice(device: Device, dialogView: android.view.View) {
        val name = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etName)?.text?.toString() ?: ""
        val osName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etOS)?.text?.toString() ?: ""
        val ipAddress = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etIP)?.text?.toString()
        val ramSizeStr = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etRAM)?.text?.toString() ?: ""
        val departmentName = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.spinnerDepartment)?.text?.toString()

        lifecycleScope.launch {
            try {
                val selectedDepartment = departments.find { it.name == departmentName }
                val ramSize = ramSizeStr.toIntOrNull() ?: 0

                val updatedDevice = device.copy(
                    name = name,
                    osName = osName,
                    ipAddress = if (ipAddress?.isNotEmpty() == true) ipAddress else null,
                    ramSize = ramSize,
                    departmentId = selectedDepartment?.id
                )

                val deviceRepository = ServiceLocator.getDeviceRepository()
                deviceRepository.updateDevice(updatedDevice)

                Toast.makeText(this@DevicesManagementActivity, "Устройство обновлено", Toast.LENGTH_SHORT).show()
                loadAllDevices()

            } catch (e: Exception) {
                Toast.makeText(this@DevicesManagementActivity, "Ошибка обновления: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteDevice(deviceWithDepartment: DeviceWithDepartment) {
        println("DEBUG: Удаление устройства: ${deviceWithDepartment.device.id}")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Удаление устройства")
            .setMessage("Удалить устройство \"${deviceWithDepartment.device.name}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                confirmDeleteDevice(deviceWithDepartment.device)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun confirmDeleteDevice(device: Device) {
        lifecycleScope.launch {
            try {
                val deviceRepository = ServiceLocator.getDeviceRepository()
                deviceRepository.deleteDevice(device)

                Toast.makeText(this@DevicesManagementActivity, "Устройство удалено", Toast.LENGTH_SHORT).show()
                loadAllDevices()

            } catch (e: Exception) {
                Toast.makeText(this@DevicesManagementActivity, "Ошибка удаления: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.tvEmptyState.visibility = android.view.View.VISIBLE
            binding.rvDevices.visibility = android.view.View.GONE
            println("DEBUG: Нет данных устройств, показываем Empty State")
        } else {
            binding.tvEmptyState.visibility = android.view.View.GONE
            binding.rvDevices.visibility = android.view.View.VISIBLE
            println("DEBUG: Данные устройств есть, скрываем Empty State")
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

// Data class для отображения устройства с названием отдела
data class DeviceWithDepartment(
    val device: Device,
    val departmentName: String
)