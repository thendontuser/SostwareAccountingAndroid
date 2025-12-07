package com.example.sostwareaccountingandroid.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.sostwareaccountingandroid.entity.Developer
import kotlinx.coroutines.flow.Flow

@Dao
interface DeveloperDao {

    @Insert
    suspend fun insert(developer: Developer): Long

    @Update
    suspend fun update(developer: Developer)

    @Delete
    suspend fun delete(developer: Developer)

    @Query("SELECT * FROM developers ORDER BY name")
    suspend fun getAllDevelopers(): List<Developer>

    @Query("SELECT * FROM developers WHERE id = :developerId")
    suspend fun getDeveloperById(developerId: Long): Developer?

    @Query("SELECT COUNT(*) FROM software WHERE developerId = :developerId")
    suspend fun getSoftwareCountByDeveloper(developerId: Long): Int
}