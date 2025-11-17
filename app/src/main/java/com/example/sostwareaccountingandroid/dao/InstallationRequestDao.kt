package com.example.sostwareaccountingandroid.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

import com.example.sostwareaccountingandroid.entity.InstallationRequest

@Dao
interface InstallationRequestDao {

    @Query("SELECT * FROM installation_requests WHERE userId = :userId ORDER BY requestDate DESC")
    suspend fun getRequestsByUserId(userId: Long): List<InstallationRequest>

    @Query("SELECT * FROM installation_requests ORDER BY requestDate DESC")
    suspend fun getAllRequests(): List<InstallationRequest>

    @Query("SELECT * FROM installation_requests WHERE id = :requestId")
    suspend fun getRequestById(requestId: Long): InstallationRequest?

    @Insert
    suspend fun insertRequest(request: InstallationRequest): Long

    @Update
    suspend fun updateRequest(request: InstallationRequest)

    @Delete
    suspend fun deleteRequest(request: InstallationRequest)

    @Query("SELECT * FROM installation_requests WHERE status = :status ORDER BY requestDate DESC")
    suspend fun getRequestsByStatus(status: String): List<InstallationRequest>

    @Query("UPDATE installation_requests SET status = :newStatus WHERE id = :requestId")
    suspend fun updateRequestStatus(requestId: Long, newStatus: String)
}