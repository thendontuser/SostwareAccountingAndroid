package com.example.sostwareaccountingandroid.repository

import com.example.sostwareaccountingandroid.dao.DepartmentDao
import com.example.sostwareaccountingandroid.entity.Department

class DepartmentRepository(private val departmentDao: DepartmentDao) {

    suspend fun getAllDepartments(): List<Department> {
        return departmentDao.getAllDepartments()
    }

    suspend fun insert(department: Department): Long {
        return departmentDao.insert(department)
    }

    suspend fun update(department: Department): Unit {
        return departmentDao.update(department)
    }

    suspend fun delete(department: Department): Unit {
        return departmentDao.delete(department)
    }

    suspend fun getDepartmentByName(name: String): Department? {
        return departmentDao.getDepartmentByName(name)
    }

    suspend fun getUserCountInDepartment(departmentId: Long): Int {
        return departmentDao.getUserCountInDepartment(departmentId)
    }

    suspend fun getDeviceCountInDepartment(departmentId: Long): Int {
        return departmentDao.getDeviceCountInDepartment(departmentId)
    }
}