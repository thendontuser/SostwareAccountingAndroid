package com.example.sostwareaccountingandroid

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sostwareaccountingandroid.databinding.ActivityAdminMainBinding
import com.example.sostwareaccountingandroid.di.ServiceLocator
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class AdminMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupNavigation()
        setupClickListeners()
        loadStatistics()

        debugViewIds()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Админ-панель"
    }

    private fun setupNavigation() {
        println("=== DEBUG SETUP NAVIGATION ===")

        val cards = listOf(
            binding.cardUsers,
            binding.cardSoftware,
            binding.cardDevices,
            binding.cardDepartments,
            binding.cardDevelopers,
            binding.cardRequests
        )

        cards.forEach { card ->
            card?.setOnClickListener {
                println("DEBUG: КАРТОЧКА НАЖАТА")
                when (card) {
                    binding.cardUsers -> navigateToUsersManagement()
                    binding.cardSoftware -> navigateToSoftwareManagement()
                    binding.cardDevices -> navigateToDevicesManagement()
                    binding.cardDepartments -> navigateToDepartmentsManagement()
                    binding.cardDevelopers -> navigateToDevelopersManagement()
                    binding.cardRequests -> navigateToRequestsManagement()
                }
            }
        }

        println("=== DEBUG SETUP NAVIGATION ЗАВЕРШЕН ===")
    }

    private fun setupClickListeners() {
        binding.btnFullReport.setOnClickListener {
            println("DEBUG: Нажата кнопка полного отчета")
            generateFullReport()
        }

        binding.btnDepartmentReport.setOnClickListener {
            println("DEBUG: Нажата кнопка отчета по отделу")
            generateDepartmentReport()
        }
    }

    private fun debugViewIds() {
        val ids = listOf(
            R.id.cardUsers,
            R.id.cardSoftware,
            R.id.cardDevices,
            R.id.cardDepartments,
            R.id.cardDevelopers,
            R.id.cardRequests,
            R.id.btnFullReport,
            R.id.btnDepartmentReport
        )

        ids.forEach { id ->
            val view = binding.root.findViewById<android.view.View>(id)
            println("DEBUG: View с id ${resources.getResourceEntryName(id)} найдена: ${view != null}")
        }
    }

    private fun loadStatistics() {
        lifecycleScope.launch {
            try {
                val userRepository = ServiceLocator.getUserRepository()
                val softwareRepository = ServiceLocator.getSoftwareRepository()
                val deviceRepository = ServiceLocator.getDeviceRepository()
                val requestRepository = ServiceLocator.getInstallationRequestRepository()
                val departmentRepository = ServiceLocator.getDepartmentRepository()
                val developerRepository = ServiceLocator.getDeveloperRepository()

                val softwareCount = softwareRepository.getAllSoftware().size
                val deviceCount = deviceRepository.getAllDevices().size
                val requestCount = requestRepository.getAllRequests().size
                val pendingRequests = requestRepository.getRequestsByStatus("На рассмотрении").size

                updateStatisticsUI(softwareCount, deviceCount, requestCount, pendingRequests)

                println("DEBUG: Статистика загружена успешно")

            } catch (e: Exception) {
                println("DEBUG: Ошибка загрузки статистики: ${e.message}")
                Toast.makeText(this@AdminMainActivity, "Ошибка загрузки статистики", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateStatisticsUI(softwareCount: Int, deviceCount: Int, requestCount: Int, pendingRequests: Int) {
        // Можно добавить TextView для отображения статистики в интерфейсе
        // Например, в toolbar или отдельной карточке
        supportActionBar?.subtitle = "ПО: $softwareCount | Устройства: $deviceCount | Заявки: $requestCount"
    }

    // Методы навигации
    private fun navigateToUsersManagement() {
        println("МЫ В МЕТОДЕ navigateToUsersManagement")
        val intent = Intent(this, UsersManagementActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToSoftwareManagement() {
        val intent = Intent(this, SoftwareManagementActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToDevicesManagement() {
        val intent = Intent(this, DevicesManagementActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToDepartmentsManagement() {
        val intent = Intent(this, DepartmentsManagementActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToDevelopersManagement() {
        val intent = Intent(this, DevelopersManagementActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToRequestsManagement() {
        val intent = Intent(this, RequestsManagementActivity::class.java)
        startActivity(intent)
    }

    private fun generateFullReport() {
        lifecycleScope.launch {
            try {
                val report = buildFullReport()
                showReportDialog(report)
            } catch (e: Exception) {
                Toast.makeText(this@AdminMainActivity, "Ошибка генерации отчета", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun buildFullReport(): String {
        val requestRepository = ServiceLocator.getInstallationRequestRepository()
        val softwareRepository = ServiceLocator.getSoftwareRepository()
        val deviceRepository = ServiceLocator.getDeviceRepository()
        val departmentRepository = ServiceLocator.getDepartmentRepository()
        val developerRepository = ServiceLocator.getDeveloperRepository()

        val totalRequests = requestRepository.getAllRequests().size
        val pendingRequests = requestRepository.getRequestsByStatus("На рассмотрении").size
        val approvedRequests = requestRepository.getRequestsByStatus("Установлено").size
        val rejectedRequests = requestRepository.getRequestsByStatus("Отклонено").size
        val totalSoftware = softwareRepository.getAllSoftware().size
        val totalDevices = deviceRepository.getAllDevices().size

        // Получаем отделы
        val departments = departmentRepository.getAllDepartments()
        //val departments = mutableListOf<com.example.sostwareaccountingandroid.entity.Department>()
        /*departmentsFlow.collect { departmentsList ->
            departments.addAll(departmentsList)
        }*/

        return """
            === ПОЛНЫЙ ОТЧЕТ ПО УЧЕТУ ПО ===
            
            Общая статистика:
            • Всего заявок: $totalRequests
            • На рассмотрении: $pendingRequests
            • Установлено: $approvedRequests
            • Отклонено: $rejectedRequests
            
            Ресурсы:
            • Программное обеспечение: $totalSoftware
            • Устройства: $totalDevices
            • Отделы: ${departments.size}
            
            Отчет сформирован: ${java.text.SimpleDateFormat("dd.MM.yyyy HH:mm").format(java.util.Date())}
        """.trimIndent()
    }

    private fun generateDepartmentReport() {
        // TODO: Реализовать выбор отдела и генерацию отчета
        Toast.makeText(this, "Выбор отдела для отчета", Toast.LENGTH_SHORT).show()
    }

    private fun showReportDialog(report: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Полный отчет ПО")
            .setMessage(report)
            .setPositiveButton("Закрыть", null)
            .setNeutralButton("Поделиться") { _, _ ->
                shareReport(report)
            }
            .show()
    }

    private fun shareReport(report: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Отчет по учету ПО")
            putExtra(Intent.EXTRA_TEXT, report)
        }
        startActivity(Intent.createChooser(intent, "Поделиться отчетом"))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.admin_main_menu, menu)
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