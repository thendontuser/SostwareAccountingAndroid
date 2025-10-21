package com.example.sostwareaccountingandroid

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.sostwareaccountingandroid.databinding.ActivityAdminMainBinding

class AdminMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminMainBinding
    private lateinit var adminViewModel: AdminViewModel
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = intent.getParcelableExtra("USER")
        adminViewModel = ViewModelProvider(this)[AdminViewModel::class.java]

        setupToolbar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.subtitle = currentUser?.let { "${it.firstName} ${it.lastName}" }
    }

    private fun setupClickListeners() {
        // Обработка кнопок отчетов
        binding.btnFullReport.setOnClickListener {
            generateFullReport()
        }

        binding.btnDepartmentReport.setOnClickListener {
            showDepartmentReportDialog()
        }

        // Обработка карточек управления данными
        binding.root.findViewById<MaterialCardView>(R.id.card_users).setOnClickListener {
            openUsersManagement()
        }

        binding.root.findViewById<MaterialCardView>(R.id.card_software).setOnClickListener {
            openSoftwareManagement()
        }

        binding.root.findViewById<MaterialCardView>(R.id.card_devices).setOnClickListener {
            openDevicesManagement()
        }

        binding.root.findViewById<MaterialCardView>(R.id.card_departments).setOnClickListener {
            openDepartmentsManagement()
        }

        binding.root.findViewById<MaterialCardView>(R.id.card_manufacturers).setOnClickListener {
            openManufacturersManagement()
        }

        binding.root.findViewById<MaterialCardView>(R.id.card_requests).setOnClickListener {
            openRequestsManagement()
        }
    }

    private fun generateFullReport() {
        val intent = Intent(this, ReportActivity::class.java)
        intent.putExtra("REPORT_TYPE", "full")
        startActivity(intent)
    }

    private fun showDepartmentReportDialog() {
        val departments = adminViewModel.getDepartments().value ?: emptyList()

        val departmentNames = departments.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Выберите отдел для отчета")
            .setItems(departmentNames) { _, which ->
                val selectedDepartment = departments[which]
                val intent = Intent(this, ReportActivity::class.java)
                intent.putExtra("REPORT_TYPE", "department")
                intent.putExtra("DEPARTMENT_ID", selectedDepartment.id)
                startActivity(intent)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun openUsersManagement() {
        val intent = Intent(this, UsersManagementActivity::class.java)
        startActivity(intent)
    }

    private fun openSoftwareManagement() {
        val intent = Intent(this, SoftwareManagementActivity::class.java)
        startActivity(intent)
    }

    private fun openDevicesManagement() {
        val intent = Intent(this, DevicesManagementActivity::class.java)
        startActivity(intent)
    }

    private fun openDepartmentsManagement() {
        val intent = Intent(this, DepartmentsManagementActivity::class.java)
        startActivity(intent)
    }

    private fun openManufacturersManagement() {
        val intent = Intent(this, ManufacturersManagementActivity::class.java)
        startActivity(intent)
    }

    private fun openRequestsManagement() {
        val intent = Intent(this, RequestsManagementActivity::class.java)
        startActivity(intent)
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