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
import com.example.sostwareaccountingandroid.databinding.ActivityUserMainBinding
import com.example.sostwareaccountingandroid.di.ServiceLocator
import com.example.sostwareaccountingandroid.entity.InstallationRequest
import com.example.sostwareaccountingandroid.entity.User
import kotlinx.coroutines.launch

class UserMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserMainBinding
    private lateinit var requestAdapter: RequestAdapter
    private lateinit var softwareAdapter: ArrayAdapter<String>
    private lateinit var deviceAdapter: ArrayAdapter<String>

    private var currentUser: User? = null
    private var currentUserId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            try {
            binding = ActivityUserMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            currentUserId = intent.getLongExtra("USER_ID", 0)
            println("DEBUG: UserMainActivity для пользователя ID: $currentUserId")

            setupToolbar()
            setupRecyclerView()
            setupUI()
            setupClickListeners()
            loadUserData()
            loadMockData() // Временные данные для теста

        } catch (e: Exception) {
            println("DEBUG: Ошибка в UserMainActivity: ${e.message}")
            e.printStackTrace()
            createFallbackLayout()
        }
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
                    // Обновляем тулбар с именем пользователя
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
            }
        }
    }

    private fun loadMockData() {
        // Временные данные для тестирования
        val softwareList = listOf("Microsoft Office 2021", "Adobe Photoshop CC", "IntelliJ IDEA", "Visual Studio Code")
        softwareAdapter.addAll(softwareList)

        val deviceList = listOf("Компьютер кафедры (Windows 11)", "Ноутбук преподавателя (Windows 10)", "Сервер (Linux)")
        deviceAdapter.addAll(deviceList)

        // Временные заявки для демонстрации
        val mockRequests = listOf(
            InstallationRequest(
                id = 1,
                softwareId = 1,
                deviceId = 1,
                userId = currentUserId,
                requestDate = System.currentTimeMillis() - 86400000, // вчера
                status = "На рассмотрении",
                comment = "Необходимо для учебного процесса"
            ),
            InstallationRequest(
                id = 2,
                softwareId = 2,
                deviceId = 2,
                userId = currentUserId,
                requestDate = System.currentTimeMillis() - 172800000, // 2 дня назад
                status = "Установлено",
                comment = null
            )
        )

        requestAdapter.submitList(mockRequests)
        updateEmptyState(mockRequests.isEmpty())
    }

    private fun submitNewRequest() {
        val software = binding.actvSoftware.text.toString().trim()
        val device = binding.actvDevice.text.toString().trim()
        val comment = binding.etComment.text.toString().trim()

        if (validateRequest(software, device)) {
            // TODO: Сохранить заявку в базу данных
            Toast.makeText(this, "Заявка отправлена на рассмотрение!", Toast.LENGTH_LONG).show()

            // Очищаем форму
            binding.actvSoftware.text.clear()
            binding.actvDevice.text.clear()
            binding.etComment.text?.clear()

            // Обновляем список (временно добавляем mock-заявку)
            val currentList = requestAdapter.currentList.toMutableList()
            val newRequest = InstallationRequest(
                id = (currentList.maxOfOrNull { it.id } ?: 0) + 1,
                softwareId = 1,
                deviceId = 1,
                userId = currentUserId,
                requestDate = System.currentTimeMillis(),
                status = "На рассмотрении",
                comment = if (comment.isNotEmpty()) comment else null
            )
            currentList.add(0, newRequest)
            requestAdapter.submitList(currentList)
            updateEmptyState(false)
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
        Toast.makeText(this, "Показать все заявки", Toast.LENGTH_SHORT).show()
        // TODO: Переход на экран со всеми заявками
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

    private fun createFallbackLayout() {
        val userName = if (currentUser != null) {
            getUserDisplayName(currentUser!!)
        } else {
            "Пользователь ID: $currentUserId"
        }

        val textView = android.widget.TextView(this).apply {
            text = "Добро пожаловать, $userName!"
            textSize = 18f
            setPadding(50, 50, 50, 50)
        }

        val button = android.widget.Button(this).apply {
            text = "Выйти"
            setOnClickListener { logout() }
        }

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            addView(textView)
            addView(button)
        }

        setContentView(layout)
    }
}