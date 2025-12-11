package com.example.sostwareaccountingandroid

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sostwareaccountingandroid.adapter.RequestsManagementAdapter
import com.example.sostwareaccountingandroid.databinding.ActivityRequestsManagementBinding
import com.example.sostwareaccountingandroid.di.ServiceLocator
import com.example.sostwareaccountingandroid.entity.InstallationRequest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class RequestsManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestsManagementBinding
    private lateinit var requestsAdapter: RequestsManagementAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestsManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        loadRequests()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Управление заявками"
    }

    private fun setupRecyclerView() {
        requestsAdapter = RequestsManagementAdapter(
            onApproveClick = { requestWithDetails -> approveRequest(requestWithDetails) },
            onRejectClick = { requestWithDetails -> rejectRequest(requestWithDetails) },
            onDetailsClick = { requestWithDetails -> showRequestDetails(requestWithDetails) },
            onUserClick = { userId -> showUserDetails(userId) }
        )

        binding.rvRequests.apply {
            adapter = requestsAdapter
            layoutManager = LinearLayoutManager(this@RequestsManagementActivity)
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {

        binding.btnFilterPending.setOnClickListener {
            filterByStatus("На рассмотрении")
        }

        binding.btnFilterRejected.setOnClickListener {
            filterByStatus("Отклонено")
        }
    }

    private fun loadRequests() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = android.view.View.VISIBLE

                val requestRepository = ServiceLocator.getInstallationRequestRepository()
                val allRequests = requestRepository.getAllRequests()

                val requestsWithDetails = mutableListOf<RequestFullDetails>()

                for (request in allRequests) {
                    val requestDetails = getRequestFullDetails(request)
                    if (requestDetails != null) {
                        requestsWithDetails.add(requestDetails)
                    }
                }

                requestsAdapter.submitList(requestsWithDetails)
                updateEmptyState(requestsWithDetails.isEmpty())
                updateStatistics(requestsWithDetails)

            } catch (e: Exception) {
                println("DEBUG: Ошибка загрузки заявок: ${e.message}")
                Toast.makeText(
                    this@RequestsManagementActivity,
                    "Ошибка загрузки заявок",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }

    private suspend fun getRequestFullDetails(request: InstallationRequest): RequestFullDetails? {
        return try {
            val userRepository = ServiceLocator.getUserRepository()
            val softwareRepository = ServiceLocator.getSoftwareRepository()
            val deviceRepository = ServiceLocator.getDeviceRepository()

            val user = userRepository.getUserById(request.userId)
            val software = softwareRepository.getSoftwareById(request.softwareId)
            val device = deviceRepository.getDeviceById(request.deviceId)

            if (user != null && software != null && device != null) {
                RequestFullDetails(
                    request = request,
                    userName = user.getFullName(),
                    userLogin = user.login,
                    userRole = user.role,
                    softwareName = software.name,
                    softwareVersion = software.version,
                    deviceName = device.name,
                    deviceOS = device.osName,
                    deviceRAM = device.ramSize
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun filterByStatus(status: String) {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = android.view.View.VISIBLE

                val requestRepository = ServiceLocator.getInstallationRequestRepository()
                val filteredRequests = requestRepository.getRequestsByStatus(status)

                val requestsWithDetails = mutableListOf<RequestFullDetails>()

                for (request in filteredRequests) {
                    val requestDetails = getRequestFullDetails(request)
                    if (requestDetails != null) {
                        requestsWithDetails.add(requestDetails)
                    }
                }

                requestsAdapter.submitList(requestsWithDetails)
                updateEmptyState(requestsWithDetails.isEmpty())

            } catch (e: Exception) {
                println("DEBUG: Ошибка фильтрации по статусу: ${e.message}")
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }

    private fun updateStatistics(requestsWithDetails: List<RequestFullDetails>) {
        val totalCount = requestsWithDetails.size
        val pendingCount = requestsWithDetails.count { it.request.status == "На рассмотрении" }
        val approvedCount = requestsWithDetails.count { it.request.status == "Установлено" }
        val rejectedCount = requestsWithDetails.count { it.request.status == "Отклонено" }

        binding.tvStats.text = "Всего: $totalCount | На рассмотрении: $pendingCount | Установлено: $approvedCount | Отклонено: $rejectedCount"
    }

    private fun approveRequest(requestFullDetails: RequestFullDetails) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Одобрить заявку")
            .setMessage("""
                Одобрить заявку на установку ПО?
                
                Пользователь: ${requestFullDetails.userName}
                ПО: ${requestFullDetails.softwareName} ${requestFullDetails.softwareVersion}
                Устройство: ${requestFullDetails.deviceName}
                
                Комментарий пользователя: ${requestFullDetails.request.comment ?: "нет"}
            """.trimIndent())
            .setPositiveButton("Одобрить") { _, _ ->
                updateRequestStatus(requestFullDetails.request.id, "Установлено", "Заявка одобрена")
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun rejectRequest(requestFullDetails: RequestFullDetails) {
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Отклонить заявку")
            .setMessage("Отклонить заявку от ${requestFullDetails.userName}?")
            .setView(R.layout.dialog_reject_request)
            .setPositiveButton("Отклонить") { dialogInterface, _ ->
                val reason = (dialogInterface as android.app.AlertDialog)
                    .findViewById<android.widget.EditText>(R.id.etRejectReason)?.text?.toString() ?: ""
                updateRequestStatus(requestFullDetails.request.id, "Отклонено", reason)
            }
            .setNegativeButton("Отмена", null)
            .create()

        dialog.show()
    }

    private fun updateRequestStatus(requestId: Long, newStatus: String, adminComment: String) {
        lifecycleScope.launch {
            try {
                val requestRepository = ServiceLocator.getInstallationRequestRepository()
                requestRepository.updateRequestStatus(requestId, newStatus)

                // TODO: Здесь можно добавить запись в SoftwareInstallations при одобрении
                if (newStatus == "Установлено") {
                    // Получаем детали заявки для создания записи об установке
                    val request = requestRepository.getRequestById(requestId)
                    if (request != null) {
                        // TODO: Создать запись в software_installations
                        // val softwareInstallation = SoftwareInstallation(
                        //     softwareId = request.softwareId,
                        //     deviceId = request.deviceId,
                        //     installationDate = System.currentTimeMillis()
                        // )
                        // softwareInstallationRepository.insert(softwareInstallation)
                    }
                }

                Toast.makeText(
                    this@RequestsManagementActivity,
                    "Статус заявки обновлен: $newStatus",
                    Toast.LENGTH_SHORT
                ).show()
                loadRequests()

            } catch (e: Exception) {
                Toast.makeText(
                    this@RequestsManagementActivity,
                    "Ошибка обновления статуса: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showRequestDetails(requestFullDetails: RequestFullDetails) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val requestDate = dateFormat.format(Date(requestFullDetails.request.requestDate))

        val detailsText = """
            ДЕТАЛИ ЗАЯВКИ
            
            Статус: ${requestFullDetails.request.status}
            Дата подачи: $requestDate
            
            ПОЛЬЗОВАТЕЛЬ
            ФИО: ${requestFullDetails.userName}
            Логин: ${requestFullDetails.userLogin}
            Роль: ${requestFullDetails.userRole}
            
            ПРОГРАММНОЕ ОБЕСПЕЧЕНИЕ
            Название: ${requestFullDetails.softwareName}
            Версия: ${requestFullDetails.softwareVersion}
            
            УСТРОЙСТВО
            Название: ${requestFullDetails.deviceName}
            ОС: ${requestFullDetails.deviceOS}
            RAM: ${requestFullDetails.deviceRAM} ГБ
            
            КОММЕНТАРИИ
            Пользователь: ${requestFullDetails.request.comment ?: "нет"}
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Детали заявки")
            .setMessage(detailsText)
            .setPositiveButton("Закрыть", null)
            .setNeutralButton("Экспорт") { _, _ ->
                shareRequestDetails(detailsText)
            }
            .show()
    }

    private fun showUserDetails(userId: Long) {
        lifecycleScope.launch {
            try {
                val userRepository = ServiceLocator.getUserRepository()
                val user = userRepository.getUserById(userId)

                if (user != null) {
                    val departmentRepository = ServiceLocator.getDepartmentRepository()
                    val departmentName = if (user.departmentId != null) {
                        val department = departmentRepository.getDepartmentByName("") // Нужен метод getDepartmentById
                        department?.name ?: "Не указан"
                    } else {
                        "Не указан"
                    }

                    val userDetails = """
                        ИНФОРМАЦИЯ О ПОЛЬЗОВАТЕЛЕ
                        
                        ФИО: ${user.getFullName()}
                        Логин: ${user.login}
                        Роль: ${user.role}
                        Отдел: $departmentName
                        
                        ID пользователя: $userId
                    """.trimIndent()

                    androidx.appcompat.app.AlertDialog.Builder(this@RequestsManagementActivity)
                        .setTitle("Информация о пользователе")
                        .setMessage(userDetails)
                        .setPositiveButton("Закрыть", null)
                        .show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@RequestsManagementActivity, "Ошибка загрузки информации", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareRequestDetails(detailsText: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Детали заявки на установку ПО")
            putExtra(Intent.EXTRA_TEXT, detailsText)
        }
        startActivity(Intent.createChooser(intent, "Поделиться деталями заявки"))
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.tvEmptyState.visibility = android.view.View.VISIBLE
            binding.rvRequests.visibility = android.view.View.GONE
        } else {
            binding.tvEmptyState.visibility = android.view.View.GONE
            binding.rvRequests.visibility = android.view.View.VISIBLE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

// Data class для полной информации о заявке
data class RequestFullDetails(
    val request: InstallationRequest,
    val userName: String,
    val userLogin: String,
    val userRole: String,
    val softwareName: String,
    val softwareVersion: String,
    val deviceName: String,
    val deviceOS: String,
    val deviceRAM: Int
)