package com.example.sostwareaccountingandroid.dao

import androidx.room.*
import com.example.sostwareaccountingandroid.entity.Device

@Dao
interface DeviceDao {

    @Query("SELECT * FROM devices ORDER BY name")
    suspend fun getAllDevices(): List<Device>

    @Query("SELECT * FROM devices WHERE id = :deviceId")
    suspend fun getDeviceById(deviceId: Long): Device?

    @Insert
    suspend fun insertDevice(device: Device): Long

    @Update
    suspend fun updateDevice(device: Device)

    @Delete
    suspend fun deleteDevice(device: Device)

    @Query("SELECT * FROM devices WHERE name LIKE :query ORDER BY name")
    suspend fun searchDevicesByName(query: String): List<Device>
}