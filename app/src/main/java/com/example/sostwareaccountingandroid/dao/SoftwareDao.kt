package com.example.sostwareaccountingandroid.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.sostwareaccountingandroid.entity.Software
import kotlinx.coroutines.flow.Flow

@Dao
interface SoftwareDao {

    @Insert
    suspend fun insert(software: Software): Long

    @Update
    suspend fun update(software: Software)

    @Delete
    suspend fun delete(software: Software)

    @Query("SELECT * FROM software WHERE id = :softwareId")
    suspend fun getSoftwareById(softwareId: Long): Software?

    @Query("SELECT * FROM software WHERE developerId = :developerId")
    fun getSoftwareByDeveloper(developerId: Long): Flow<List<Software>>

    @Query("SELECT * FROM software WHERE name LIKE '%' || :query || '%'")
    fun searchSoftware(query: String): Flow<List<Software>>

    // Получение ПО с информацией о производителе
    @Query("""
        SELECT software.*, developers.name as developerName 
        FROM software 
        LEFT JOIN developers ON software.developerId = developers.id
        ORDER BY software.name
    """)
    fun getSoftwareWithDeveloper(): Flow<List<SoftwareWithDeveloper>>

    data class SoftwareWithDeveloper(
        @Embedded val software: Software,
        val developerName: String?
    )
}