package com.example.sostwareaccountingandroid

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.sostwareaccountingandroid.databinding.ActivityCreateRequestBinding
import com.example.sostwareaccountingandroid.entity.Device
import com.example.sostwareaccountingandroid.entity.InstallationRequest
import com.example.sostwareaccountingandroid.entity.Software
import com.example.sostwareaccountingandroid.entity.User
import com.example.sostwareaccountingandroid.viewmodel.RequestViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class CreateRequestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateRequestBinding
    private lateinit var requestViewModel: RequestViewModel
    private lateinit var softwareAdapter: ArrayAdapter<String>
    private lateinit var deviceAdapter: ArrayAdapter<String>

    private var softwareList = listOf<Software>()
    private var deviceList = listOf<Device>()
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = if (intent.hasExtra("USER")) {
            intent.getParcelableExtra("USER", User::class.java)
        } else {
            null
        }
        requestViewModel = RequestViewModel() // Просто создаем экземпляр

        setupUI()
        setupClickListeners()
        setupObservers()
        loadData()
    }

    private fun setupUI() {
        // Используем стандартный layout вместо dropdown_item
        softwareAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mutableListOf())
        binding.actvSoftware.setAdapter(softwareAdapter)

        deviceAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mutableListOf())
        binding.actvDevice.setAdapter(deviceAdapter)
    }

    private fun setupClickListeners() {
        binding.btnSubmit.setOnClickListener {
            submitRequest()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        // Используем collect для StateFlow вместо observe
        lifecycleScope.launch {
            requestViewModel.softwareList.collect { software ->
                softwareList = software
                val softwareNames = software.map { "${it.name} v${it.version}" }
                softwareAdapter.clear()
                softwareAdapter.addAll(softwareNames)
            }
        }

        lifecycleScope.launch {
            requestViewModel.deviceList.collect { devices ->
                deviceList = devices
                val deviceNames = devices.map { "${it.name} (${it.osName})" }
                deviceAdapter.clear()
                deviceAdapter.addAll(deviceNames)
            }
        }

        lifecycleScope.launch {
            requestViewModel.submissionState.collect { result ->
                binding.progressBar.visibility = View.GONE

                when (result) {
                    is RequestViewModel.SubmissionState.Success -> {
                        showSuccess("Заявка успешно отправлена!")
                        Handler(Looper.getMainLooper()).postDelayed({
                            finish()
                        }, 1500)
                    }
                    is RequestViewModel.SubmissionState.Error -> {
                        showError(result.message)
                    }
                    else -> {
                        // Обработка других состояний
                    }
                }
            }
        }
    }

    private fun loadData() {
        requestViewModel.loadAllSoftware()
        requestViewModel.loadAllDevices()
    }

    private fun submitRequest() {
        val softwareName = binding.actvSoftware.text.toString().trim()
        val deviceName = binding.actvDevice.text.toString().trim()
        val comment = binding.etComment.text.toString().trim()

        if (validateInput(softwareName, deviceName)) {
            val selectedSoftware = softwareList.find {
                "${it.name} v${it.version}" == softwareName
            }
            val selectedDevice = deviceList.find {
                "${it.name} (${it.osName})" == deviceName
            }

            if (selectedSoftware != null && selectedDevice != null && currentUser != null) {
                binding.progressBar.visibility = View.VISIBLE

                val request = InstallationRequest(
                    softwareId = selectedSoftware.id,
                    deviceId = selectedDevice.id,
                    userId = currentUser!!.id,
                    requestDate = System.currentTimeMillis(),
                    status = "На рассмотрении",
                    comment = comment.ifEmpty { null }
                )

                requestViewModel.submitRequest(request)
            } else {
                showError("Ошибка при выборе данных")
            }
        }
    }

    private fun validateInput(software: String, device: String): Boolean {
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

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.gray)) // Используем существующий цвет
            .show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}