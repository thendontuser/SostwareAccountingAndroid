package com.example.sostwareaccountingandroid

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.sostwareaccountingandroid.databinding.ActivityCreateRequestBinding

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

        currentUser = intent.getParcelableExtra("USER")
        requestViewModel = ViewModelProvider(this)[RequestViewModel::class.java]

        setupUI()
        setupClickListeners()
        setupObservers()
        loadData()
    }

    private fun setupUI() {
        softwareAdapter = ArrayAdapter(this, R.layout.dropdown_item, mutableListOf())
        binding.actvSoftware.setAdapter(softwareAdapter)

        deviceAdapter = ArrayAdapter(this, R.layout.dropdown_item, mutableListOf())
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
        requestViewModel.softwareList.observe(this) { software ->
            softwareList = software
            val softwareNames = software.map { "${it.name} v${it.version}" }
            softwareAdapter.clear()
            softwareAdapter.addAll(softwareNames)
        }

        requestViewModel.deviceList.observe(this) { devices ->
            deviceList = devices
            val deviceNames = devices.map { "${it.name} (${it.osName})" }
            deviceAdapter.clear()
            deviceAdapter.addAll(deviceNames)
        }

        requestViewModel.submissionResult.observe(this) { result ->
            binding.progressBar.visibility = View.GONE

            when (result) {
                is SubmissionResult.Success -> {
                    showSuccess("Заявка успешно отправлена!")
                    Handler(Looper.getMainLooper()).postDelayed({
                        finish()
                    }, 1500)
                }
                is SubmissionResult.Error -> {
                    showError(result.message)
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
            .setBackgroundTint(ContextCompat.getColor(this, R.color.success_color))
            .show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}