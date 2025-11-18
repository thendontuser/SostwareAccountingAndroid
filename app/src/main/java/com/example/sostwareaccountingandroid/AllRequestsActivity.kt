package com.example.sostwareaccountingandroid

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sostwareaccountingandroid.adapter.RequestAdapter
import com.example.sostwareaccountingandroid.databinding.ActivityAllRequestsBinding
import com.example.sostwareaccountingandroid.di.ServiceLocator
import com.example.sostwareaccountingandroid.adapter.RequestWithDetails
import kotlinx.coroutines.launch

class AllRequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllRequestsBinding
    private lateinit var requestAdapter: RequestAdapter
    private var currentUserId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = intent.getLongExtra("USER_ID", 0)

        setupToolbar()
        setupRecyclerView()
        loadAllRequests()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Все мои заявки"
    }

    private fun setupRecyclerView() {
        requestAdapter = RequestAdapter()
        binding.rvAllRequests.apply {
            adapter = requestAdapter
            layoutManager = LinearLayoutManager(this@AllRequestsActivity)
            setHasFixedSize(true)
        }
    }

    private fun loadAllRequests() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = android.view.View.VISIBLE

                val requestRepository = ServiceLocator.getInstallationRequestRepository()
                val userRequests = requestRepository.getRequestsByUserId(currentUserId)

                // Загружаем дополнительные данные (названия ПО и устройств)
                val softwareRepository = ServiceLocator.getSoftwareRepository()
                val deviceRepository = ServiceLocator.getDeviceRepository()

                val requestsWithDetails = userRequests.map { request ->
                    RequestWithDetails(
                        request = request,
                        softwareName = softwareRepository.getSoftwareById(request.softwareId)?.name ?: "Неизвестно",
                        deviceName = deviceRepository.getDeviceById(request.deviceId)?.name ?: "Неизвестно"
                    )
                }

                requestAdapter.submitList(requestsWithDetails)
                updateEmptyState(requestsWithDetails.isEmpty())

            } catch (e: Exception) {
                println("DEBUG: Ошибка загрузки всех заявок: ${e.message}")
                e.printStackTrace()
                android.widget.Toast.makeText(
                    this@AllRequestsActivity,
                    "Ошибка загрузки заявок",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.tvEmptyState.visibility = android.view.View.VISIBLE
            binding.rvAllRequests.visibility = android.view.View.GONE
        } else {
            binding.tvEmptyState.visibility = android.view.View.GONE
            binding.rvAllRequests.visibility = android.view.View.VISIBLE
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