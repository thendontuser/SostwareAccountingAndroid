package com.example.sostwareaccountingandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sostwareaccountingandroid.entity.Device
import com.example.sostwareaccountingandroid.entity.InstallationRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.example.sostwareaccountingandroid.entity.Software

class RequestViewModel : ViewModel() {

    private val _softwareList = MutableStateFlow<List<Software>>(emptyList())
    val softwareList: StateFlow<List<Software>> = _softwareList.asStateFlow()

    private val _deviceList = MutableStateFlow<List<Device>>(emptyList())
    val deviceList: StateFlow<List<Device>> = _deviceList.asStateFlow()

    private val _submissionState = MutableStateFlow<SubmissionState>(SubmissionState.Idle)
    val submissionState: StateFlow<SubmissionState> = _submissionState.asStateFlow()

    fun loadAllSoftware() {
        viewModelScope.launch {
            // TODO: Загрузить ПО из базы данных
            _softwareList.value = emptyList() // временно
        }
    }

    fun loadAllDevices() {
        viewModelScope.launch {
            // TODO: Загрузить устройства из базы данных
            _deviceList.value = emptyList() // временно
        }
    }

    fun submitRequest(request: InstallationRequest) {
        viewModelScope.launch {
            _submissionState.value = SubmissionState.Loading
            try {
                // TODO: Сохранить заявку в базу данных
                _submissionState.value = SubmissionState.Success
            } catch (e: Exception) {
                _submissionState.value = SubmissionState.Error(e.message ?: "Ошибка при отправке заявки")
            }
        }
    }

    sealed class SubmissionState {
        object Idle : SubmissionState()
        object Loading : SubmissionState()
        object Success : SubmissionState()
        data class Error(val message: String) : SubmissionState()
    }
}