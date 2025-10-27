package com.example.sostwareaccountingandroid.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.sostwareaccountingandroid.entity.SoftwareInstallation
import kotlinx.coroutines.flow.Flow

@Dao
interface SoftwareInstallationDao {

    @Insert
    suspend fun insert(installation: SoftwareInstallation): Long

    @Update
    suspend fun update(installation: SoftwareInstallation)

    @Delete
    suspend fun delete(installation: SoftwareInstallation)

    @Query("SELECT * FROM software_installations WHERE deviceId = :deviceId")
    fun getInstallationsByDevice(deviceId: Long): Flow<List<SoftwareInstallation>>

    @Query("SELECT * FROM software_installations WHERE softwareId = :softwareId")
    fun getInstallationsBySoftware(softwareId: Long): Flow<List<SoftwareInstallation>>

    @Query("SELECT COUNT(*) FROM software_installations WHERE softwareId = :softwareId")
    suspend fun getInstallationCount(softwareId: Long): Int

    // Получение установленного ПО с деталями
    @Query("""
        SELECT 
            si.*,
            s.name as softwareName,
            s.version as softwareVersion,
            d.name as deviceName,
            dep.name as departmentName
        FROM software_installations si
        LEFT JOIN software s ON si.softwareId = s.id
        LEFT JOIN devices d ON si.deviceId = d.id
        LEFT JOIN departments dep ON d.departmentId = dep.id
        ORDER BY si.installationDate DESC
    """)
    fun getInstallationsWithDetails(): Flow<List<InstallationWithDetails>>

    data class InstallationWithDetails(
        @Embedded val installation: SoftwareInstallation,
        val softwareName: String,
        val softwareVersion: String,
        val deviceName: String,
        val departmentName: String?
    )
}