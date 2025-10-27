package com.example.sostwareaccountingandroid.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.sostwareaccountingandroid.entity.Department
import kotlinx.coroutines.flow.Flow

@Dao
interface DepartmentDao {

    @Insert
    suspend fun insert(department: Department): Long

    @Update
    suspend fun update(department: Department)

    @Delete
    suspend fun delete(department: Department)

    @Query("SELECT * FROM departments ORDER BY name")
    fun getAllDepartments(): Flow<List<Department>>

    @Query("SELECT * FROM departments WHERE id = :departmentId")
    suspend fun getDepartmentById(departmentId: Long): Department?

    @Query("SELECT COUNT(*) FROM users WHERE departmentId = :departmentId")
    suspend fun getUserCountInDepartment(departmentId: Long): Int

    @Query("SELECT COUNT(*) FROM devices WHERE departmentId = :departmentId")
    suspend fun getDeviceCountInDepartment(departmentId: Long): Int
}