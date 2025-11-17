package com.example.sostwareaccountingandroid.repository

import com.example.sostwareaccountingandroid.dao.DeviceDao
import com.example.sostwareaccountingandroid.entity.Device

class DeviceRepository(private val deviceDao: DeviceDao) {

    // Получить все устройства
    suspend fun getAllDevices(): List<Device> {
        return deviceDao.getAllDevices()
    }

    // Получить устройство по ID
    suspend fun getDeviceById(deviceId: Long): Device? {
        return deviceDao.getDeviceById(deviceId)
    }

    // Создать новое устройство
    suspend fun insertDevice(device: Device): Long {
        return deviceDao.insertDevice(device)
    }

    // Обновить устройство
    suspend fun updateDevice(device: Device) {
        deviceDao.updateDevice(device)
    }

    // Удалить устройство
    suspend fun deleteDevice(device: Device) {
        deviceDao.deleteDevice(device)
    }

    // Поиск устройств по названию
    suspend fun searchDevicesByName(query: String): List<Device> {
        return deviceDao.searchDevicesByName("%$query%")
    }
}