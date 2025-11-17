package com.example.sostwareaccountingandroid.dao

import androidx.room.*
import com.example.sostwareaccountingandroid.entity.Software

@Dao
interface SoftwareDao {

    @Query("SELECT * FROM software ORDER BY name")
    suspend fun getAllSoftware(): List<Software>

    @Query("SELECT * FROM software WHERE id = :softwareId")
    suspend fun getSoftwareById(softwareId: Long): Software?

    @Insert
    suspend fun insertSoftware(software: Software): Long

    @Update
    suspend fun updateSoftware(software: Software)

    @Delete
    suspend fun deleteSoftware(software: Software)

    @Query("SELECT * FROM software WHERE name LIKE :query ORDER BY name")
    suspend fun searchSoftwareByName(query: String): List<Software>

    @Query("SELECT * FROM software WHERE licenseType = :licenseType ORDER BY name")
    suspend fun getSoftwareByLicenseType(licenseType: String): List<Software>
}