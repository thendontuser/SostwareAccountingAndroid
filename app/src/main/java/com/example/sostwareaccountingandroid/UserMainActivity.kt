package com.example.sostwareaccountingandroid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.sostwareaccountingandroid.databinding.ActivityUserMainBinding

class UserMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserMainBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var requestAdapter: InstallationRequestAdapter
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = intent.getParcelableExtra("USER")
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        loadUserRequests()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.subtitle = currentUser?.let { "${it.firstName} ${it.lastName}" }
    }

    private fun setupRecyclerView() {
        requestAdapter = InstallationRequestAdapter { request ->
            // Обработка клика на заявку
            showRequestDetails(request)
        }

        binding.rvMyRequests.apply {
            adapter = requestAdapter
            layoutManager = LinearLayoutManager(this@UserMainActivity)
            addItemDecoration(DividerItemDecoration(this@UserMainActivity, DividerItemDecoration.VERTICAL))
        }
    }

    private fun setupClickListeners() {
        // Обработка нажатия на карточку отчета
        binding.root.findViewById<MaterialCardView>(R.id.card_report).setOnClickListener {
            generateDepartmentReport()
        }

        binding.btnCreateRequest.setOnClickListener {
            createNewRequest()
        }
    }

    private fun loadUserRequests() {
        currentUser?.let { user ->
            userViewModel.getUserRequests(user.id).observe(this) { requests ->
                if (requests.isEmpty()) {
                    binding.tvNoRequests.visibility = View.VISIBLE
                    binding.rvMyRequests.visibility = View.GONE
                } else {
                    binding.tvNoRequests.visibility = View.GONE
                    binding.rvMyRequests.visibility = View.VISIBLE
                    requestAdapter.submitList(requests)
                }
            }
        }
    }

    private fun generateDepartmentReport() {
        currentUser?.let { user ->
            val intent = Intent(this, ReportActivity::class.java)
            intent.putExtra("DEPARTMENT_ID", user.departmentId)
            intent.putExtra("REPORT_TYPE", "department")
            startActivity(intent)
        }
    }

    private fun createNewRequest() {
        val intent = Intent(this, CreateRequestActivity::class.java)
        intent.putExtra("USER", currentUser)
        startActivity(intent)
    }

    private fun showRequestDetails(request: InstallationRequest) {
        // Диалог с деталями заявки
        RequestDetailsDialog.newInstance(request).show(supportFragmentManager, "request_details")
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