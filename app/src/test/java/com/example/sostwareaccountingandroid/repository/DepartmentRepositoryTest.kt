package com.example.sostwareaccountingandroid.repository

import com.example.sostwareaccountingandroid.dao.DepartmentDao
import com.example.sostwareaccountingandroid.entity.Department
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DepartmentRepositoryTest {

    @Mock
    private lateinit var mockDepartmentDao: DepartmentDao

    private lateinit var departmentRepository: DepartmentRepository

    @Before
    fun setUp() {
        departmentRepository = DepartmentRepository(mockDepartmentDao)
    }

    @Test
    fun `insert should return generated id`() = runTest {
        // Arrange
        val department = Department(name = "Новый отдел")
        val expectedId = 42L
        `when`(mockDepartmentDao.insert(department)).thenReturn(expectedId)

        // Act
        val result = departmentRepository.insert(department)

        // Assert
        assertEquals(expectedId, result)
        verify(mockDepartmentDao).insert(department)
    }

    @Test
    fun `update should call dao update method`() = runTest {
        // Arrange
        val department = Department(id = 1, name = "Обновленный отдел")

        // Act
        departmentRepository.update(department)

        // Assert
        verify(mockDepartmentDao).update(department)
    }

    @Test
    fun `delete should call dao delete method`() = runTest {
        // Arrange
        val department = Department(id = 1, name = "Удаляемый отдел")

        // Act
        departmentRepository.delete(department)

        // Assert
        verify(mockDepartmentDao).delete(department)
    }
}