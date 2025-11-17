package com.example.sostwareaccountingandroid.repository

import com.example.sostwareaccountingandroid.dao.SoftwareDao
import com.example.sostwareaccountingandroid.entity.Software

class SoftwareRepository(private val softwareDao: SoftwareDao) {

    // Получить все программное обеспечение
    suspend fun getAllSoftware(): List<Software> {
        return softwareDao.getAllSoftware()
    }

    // Получить ПО по ID
    suspend fun getSoftwareById(softwareId: Long): Software? {
        return softwareDao.getSoftwareById(softwareId)
    }

    // Создать новое ПО
    suspend fun insertSoftware(software: Software): Long {
        return softwareDao.insertSoftware(software)
    }

    // Обновить ПО
    suspend fun updateSoftware(software: Software) {
        softwareDao.updateSoftware(software)
    }

    // Удалить ПО
    suspend fun deleteSoftware(software: Software) {
        softwareDao.deleteSoftware(software)
    }

    // Поиск ПО по названию
    suspend fun searchSoftwareByName(query: String): List<Software> {
        return softwareDao.searchSoftwareByName("%$query%")
    }

    // Получить ПО по типу лицензии
    suspend fun getSoftwareByLicenseType(licenseType: String): List<Software> {
        return softwareDao.getSoftwareByLicenseType(licenseType)
    }
}