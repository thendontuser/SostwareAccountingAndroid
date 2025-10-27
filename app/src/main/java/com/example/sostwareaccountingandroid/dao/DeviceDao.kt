package com.example.sostwareaccountingandroid.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.sostwareaccountingandroid.entity.Device
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {

    @Insert
    suspend fun insert(device: Device): Long

    @Update
    suspend fun update(device: Device)

    @Delete
    suspend fun delete(device: Device)

    @Query("SELECT * FROM devices WHERE id = :deviceId")
    suspend fun getDeviceById(deviceId: Long): Device?

    @Query("SELECT * FROM devices WHERE departmentId = :departmentId")
    fun getDevicesByDepartment(departmentId: Long): Flow<List<Device>>

    // Получение устройств с информацией об отделе
    @Query("""
        SELECT devices.*, departments.name as departmentName 
        FROM devices 
        LEFT JOIN departments ON devices.departmentId = departments.id
        ORDER BY devices.name
    """)
    fun getDevicesWithDepartment(): Flow<List<DeviceWithDepartment>>

    data class DeviceWithDepartment(
        @Embedded val device: Device,
        val departmentName: String?
    )
}