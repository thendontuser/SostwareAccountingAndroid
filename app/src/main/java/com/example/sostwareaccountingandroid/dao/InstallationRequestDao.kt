package com.example.sostwareaccountingandroid.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.sostwareaccountingandroid.entity.InstallationRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface InstallationRequestDao {

    @Insert
    suspend fun insert(request: InstallationRequest): Long

    @Update
    suspend fun update(request: InstallationRequest)

    @Delete
    suspend fun delete(request: InstallationRequest)

    @Query("SELECT * FROM installation_requests WHERE userId = :userId ORDER BY requestDate DESC")
    fun getRequestsByUser(userId: Long): Flow<List<InstallationRequest>>

    @Query("SELECT * FROM installation_requests WHERE status = :status ORDER BY requestDate DESC")
    fun getRequestsByStatus(status: String): Flow<List<InstallationRequest>>

    @Query("SELECT * FROM installation_requests ORDER BY requestDate DESC")
    fun getAllRequests(): Flow<List<InstallationRequest>>

    @Query("UPDATE installation_requests SET status = :status WHERE id = :requestId")
    suspend fun updateRequestStatus(requestId: Long, status: String)

    // Получение заявок с полной информацией
    @Query("""
        SELECT 
            ir.*,
            u.firstName || ' ' || u.lastName as userName,
            d.name as deviceName,
            s.name as softwareName,
            dep.name as departmentName
        FROM installation_requests ir
        LEFT JOIN users u ON ir.userId = u.id
        LEFT JOIN devices d ON ir.deviceId = d.id
        LEFT JOIN software s ON ir.softwareId = s.id
        LEFT JOIN departments dep ON u.departmentId = dep.id
        ORDER BY ir.requestDate DESC
    """)
    fun getRequestsWithDetails(): Flow<List<RequestWithDetails>>

    data class RequestWithDetails(
        @Embedded val request: InstallationRequest,
        val userName: String,
        val deviceName: String,
        val softwareName: String,
        val departmentName: String?
    )
}