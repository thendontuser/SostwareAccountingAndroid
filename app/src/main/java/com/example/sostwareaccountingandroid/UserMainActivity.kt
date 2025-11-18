package com.example.sostwareaccountingandroid

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sostwareaccountingandroid.adapter.RequestAdapter
import com.example.sostwareaccountingandroid.adapter.RequestWithDetails
import com.example.sostwareaccountingandroid.databinding.ActivityUserMainBinding
import com.example.sostwareaccountingandroid.di.ServiceLocator
import com.example.sostwareaccountingandroid.entity.Device
import com.example.sostwareaccountingandroid.entity.InstallationRequest
import com.example.sostwareaccountingandroid.entity.Software
import com.example.sostwareaccountingandroid.entity.User
import kotlinx.coroutines.launch

class UserMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserMainBinding
    private lateinit var requestAdapter: RequestAdapter
    private lateinit var softwareAdapter: ArrayAdapter<String>
    private lateinit var deviceAdapter: ArrayAdapter<String>

    private var currentUser: User? = null
    private var currentUserId: Long = 0

    // Списки для хранения данных из БД
    private var softwareList: List<Software> = emptyList()
    private var deviceList: List<Device> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = intent.getLongExtra("USER_ID", 0)
        println("DEBUG: UserMainActivity для пользователя ID: $currentUserId")

        setupToolbar()
        setupRecyclerView()
        setupUI()
        setupClickListeners()
        loadUserData()
        loadDataFromDatabase()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.subtitle = "Пользователь"
    }

    private fun setupRecyclerView() {
        requestAdapter = RequestAdapter()
        binding.rvRequests.apply {
            adapter = requestAdapter
            layoutManager = LinearLayoutManager(this@UserMainActivity)
            setHasFixedSize(true)
        }
    }

    private fun setupUI() {
        // Настройка выпадающих списков
        softwareAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mutableListOf())
        binding.actvSoftware.setAdapter(softwareAdapter)

        deviceAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mutableListOf())
        binding.actvDevice.setAdapter(deviceAdapter)
    }

    private fun setupClickListeners() {
        binding.btnSubmitRequest.setOnClickListener {
            submitNewRequest()
        }

        binding.btnViewAllRequests.setOnClickListener {
            showAllRequests()
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                val userRepository = ServiceLocator.getUserRepository()
                currentUser = userRepository.getUserById(currentUserId)

                if (currentUser != null) {
                    val userFullName = getUserDisplayName(currentUser!!)
                    supportActionBar?.subtitle = userFullName
                    println("DEBUG: Загружен пользователь: $userFullName")
                } else {
                    supportActionBar?.subtitle = "Пользователь не найден"
                    println("DEBUG: Пользователь с ID $currentUserId не найден")
                }
            } catch (e: Exception) {
                println("DEBUG: Ошибка загрузки пользователя: ${e.message}")
                supportActionBar?.subtitle = "Ошибка загрузки"
                Toast.makeText(this@UserMainActivity, "Ошибка загрузки данных пользователя", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadDataFromDatabase() {
        lifecycleScope.launch {
            try {
                // Загружаем данные параллельно
                val softwareDeferred = lifecycleScope.launch { loadSoftware() }
                val devicesDeferred = lifecycleScope.launch { loadDevices() }
                val requestsDeferred = lifecycleScope.launch { loadUserRequests() }

                // Ждем завершения всех загрузок
                softwareDeferred.join()
                devicesDeferred.join()
                requestsDeferred.join()

                println("DEBUG: Все данные успешно загружены из БД")

            } catch (e: Exception) {
                println("DEBUG: Ошибка загрузки данных из БД: ${e.message}")
                e.printStackTrace()
                Toast.makeText(this@UserMainActivity, "Ошибка загрузки данных", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun loadSoftware() {
        try {
            val softwareRepository = ServiceLocator.getSoftwareRepository()
            softwareList = softwareRepository.getAllSoftware()

            val softwareNames = softwareList.map { it.name }
            softwareAdapter.clear()
            softwareAdapter.addAll(softwareNames)
            softwareAdapter.notifyDataSetChanged()

            println("DEBUG: Загружено ПО: ${softwareNames.size} элементов")
        } catch (e: Exception) {
            println("DEBUG: Ошибка загрузки ПО: ${e.message}")
            throw e
        }
    }

    private suspend fun loadDevices() {
        try {
            val deviceRepository = ServiceLocator.getDeviceRepository()
            deviceList = deviceRepository.getAllDevices()

            val deviceNames = deviceList.map { it.name }
            deviceAdapter.clear()
            deviceAdapter.addAll(deviceNames)
            deviceAdapter.notifyDataSetChanged()

            println("DEBUG: Загружено устройств: ${deviceNames.size} элементов")
        } catch (e: Exception) {
            println("DEBUG: Ошибка загрузки устройств: ${e.message}")
            throw e
        }
    }

    private suspend fun loadUserRequests() {
        try {
            val requestRepository = ServiceLocator.getInstallationRequestRepository()
            val userRequests = requestRepository.getRequestsByUserId(currentUserId)

            // Загружаем дополнительные данные
            val softwareRepository = ServiceLocator.getSoftwareRepository()
            val deviceRepository = ServiceLocator.getDeviceRepository()

            val requestsWithDetails = userRequests.map { request ->
                RequestWithDetails(
                    request = request,
                    softwareName = softwareRepository.getSoftwareById(request.softwareId)?.name
                        ?: "Неизвестно",
                    deviceName = deviceRepository.getDeviceById(request.deviceId)?.name
                        ?: "Неизвестно"
                )
            }

            requestAdapter.submitList(requestsWithDetails)
            updateEmptyState(userRequests.isEmpty())
        } catch (e: Exception) {
            println("DEBUG: Ошибка загрузки заявок: ${e.message}")
            throw e
        }
    }

    private fun submitNewRequest() {
        val softwareName = binding.actvSoftware.text.toString().trim()
        val deviceName = binding.actvDevice.text.toString().trim()
        val comment = binding.etComment.text.toString().trim()

        if (validateRequest(softwareName, deviceName)) {
            lifecycleScope.launch {
                try {
                    // Находим ID выбранного ПО и устройства
                    val selectedSoftware = softwareList.find { it.name == softwareName }
                    val selectedDevice = deviceList.find { it.name == deviceName }

                    if (selectedSoftware == null || selectedDevice == null) {
                        Toast.makeText(this@UserMainActivity, "Ошибка: неверно выбраны ПО или устройство", Toast.LENGTH_LONG).show()
                        return@launch
                    }

                    // Создаем новую заявку
                    val newRequest = InstallationRequest(
                        id = 0, // БД сама сгенерирует ID
                        softwareId = selectedSoftware.id,
                        deviceId = selectedDevice.id,
                        userId = currentUserId,
                        requestDate = System.currentTimeMillis(),
                        status = "На рассмотрении",
                        comment = if (comment.isNotEmpty()) comment else null
                    )

                    // Сохраняем в БД
                    val requestRepository = ServiceLocator.getInstallationRequestRepository()
                    requestRepository.insertRequest(newRequest)

                    // Обновляем список заявок
                    loadUserRequests()

                    // Очищаем форму
                    binding.actvSoftware.text.clear()
                    binding.actvDevice.text.clear()
                    binding.etComment.text?.clear()

                    Toast.makeText(this@UserMainActivity, "Заявка отправлена на рассмотрение!", Toast.LENGTH_LONG).show()
                    println("DEBUG: Новая заявка создана для пользователя $currentUserId")

                } catch (e: Exception) {
                    println("DEBUG: Ошибка создания заявки: ${e.message}")
                    e.printStackTrace()
                    Toast.makeText(this@UserMainActivity, "Ошибка отправки заявки", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateRequest(software: String, device: String): Boolean {
        var isValid = true

        if (software.isEmpty()) {
            binding.actvSoftware.error = "Выберите программное обеспечение"
            isValid = false
        } else {
            binding.actvSoftware.error = null
        }

        if (device.isEmpty()) {
            binding.actvDevice.error = "Выберите устройство"
            isValid = false
        } else {
            binding.actvDevice.error = null
        }

        return isValid
    }

    private fun getUserDisplayName(user: User): String {
        return if (user.patronymic != null) {
            "${user.firstName} ${user.patronymic}"
        } else {
            user.firstName
        }
    }

    private fun showAllRequests() {
        // TODO: Переход на экран со всеми заявками
        Toast.makeText(this, "Функция 'Все заявки' в разработке", Toast.LENGTH_SHORT).show()
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.tvNoRequests.visibility = android.view.View.VISIBLE
            binding.rvRequests.visibility = android.view.View.GONE
        } else {
            binding.tvNoRequests.visibility = android.view.View.GONE
            binding.rvRequests.visibility = android.view.View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.user_main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}