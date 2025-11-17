package com.example.sostwareaccountingandroid.repository

import com.example.sostwareaccountingandroid.dao.InstallationRequestDao
import com.example.sostwareaccountingandroid.entity.InstallationRequest

class InstallationRequestRepository(private val installationRequestDao: InstallationRequestDao) {

    // Получить все заявки пользователя
    suspend fun getRequestsByUserId(userId: Long): List<InstallationRequest> {
        return installationRequestDao.getRequestsByUserId(userId)
    }

    // Получить все заявки (для администратора)
    suspend fun getAllRequests(): List<InstallationRequest> {
        return installationRequestDao.getAllRequests()
    }

    // Получить заявку по ID
    suspend fun getRequestById(requestId: Long): InstallationRequest? {
        return installationRequestDao.getRequestById(requestId)
    }

    // Создать новую заявку
    suspend fun insertRequest(request: InstallationRequest): Long {
        return installationRequestDao.insertRequest(request)
    }

    // Обновить заявку
    suspend fun updateRequest(request: InstallationRequest) {
        installationRequestDao.updateRequest(request)
    }

    // Удалить заявку
    suspend fun deleteRequest(request: InstallationRequest) {
        installationRequestDao.deleteRequest(request)
    }

    // Получить заявки по статусу
    suspend fun getRequestsByStatus(status: String): List<InstallationRequest> {
        return installationRequestDao.getRequestsByStatus(status)
    }

    // Обновить статус заявки
    suspend fun updateRequestStatus(requestId: Long, newStatus: String) {
        installationRequestDao.updateRequestStatus(requestId, newStatus)
    }
}