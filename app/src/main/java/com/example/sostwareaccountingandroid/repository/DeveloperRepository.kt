package com.example.sostwareaccountingandroid.repository

import com.example.sostwareaccountingandroid.dao.DeveloperDao
import com.example.sostwareaccountingandroid.entity.Developer

class DeveloperRepository(private val developerDao: DeveloperDao) {

    suspend fun getAllDevelopers(): List<Developer> {
        return developerDao.getAllDevelopers()
    }

    suspend fun getDeveloperById(developerId: Long): Developer? {
        return developerDao.getDeveloperById(developerId)
    }

    suspend fun insertDeveloper(developer: Developer): Long {
        return developerDao.insert(developer)
    }

    suspend fun updateDeveloper(developer: Developer) {
        developerDao.update(developer)
    }

    suspend fun deleteDeveloper(developer: Developer) {
        developerDao.delete(developer)
    }

    suspend fun getSoftwareCountByDeveloper(developerId: Long): Int {
        return developerDao.getSoftwareCountByDeveloper(developerId)
    }
}