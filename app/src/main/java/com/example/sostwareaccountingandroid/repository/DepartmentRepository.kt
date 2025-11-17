package com.example.sostwareaccountingandroid.repository

import com.example.sostwareaccountingandroid.dao.DepartmentDao
import com.example.sostwareaccountingandroid.entity.Department
import kotlinx.coroutines.flow.Flow

class DepartmentRepository(private val departmentDao: DepartmentDao) {

    fun getAllDepartments(): Flow<List<Department>> {
        return departmentDao.getAllDepartments()
    }

    suspend fun getDepartmentByName(name: String): Department? {
        return departmentDao.getDepartmentByName(name)
    }
}