package com.example.sostwareaccountingandroid.repository

import com.example.sostwareaccountingandroid.dao.DeveloperDao
import com.example.sostwareaccountingandroid.entity.Developer
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DeveloperRepositoryTest {

    @Mock
    private lateinit var mockDeveloperDao: DeveloperDao

    private lateinit var developerRepository: DeveloperRepository

    @Before
    fun setUp() {
        developerRepository = DeveloperRepository(mockDeveloperDao)
    }

    @Test
    fun `insertDeveloper should return generated id`() = runTest {
        // Arrange
        val developer = Developer(
            name = "Новая компания",
            companyType = "Корпорация",
            location = "test"
        )
        val expectedId = 100L
        `when`(mockDeveloperDao.insert(developer)).thenReturn(expectedId)

        // Act
        val result = developerRepository.insertDeveloper(developer)

        // Assert
        assertEquals(expectedId, result)
        verify(mockDeveloperDao).insert(developer)
    }

    @Test
    fun `updateDeveloper should call dao update method`() = runTest {
        // Arrange
        val developer = Developer(
            id = 1,
            name = "Обновленная Microsoft",
            companyType = "Корпорация",
            location = "test"
        )

        // Act
        developerRepository.updateDeveloper(developer)

        // Assert
        verify(mockDeveloperDao).update(developer)
    }

    @Test
    fun `deleteDeveloper should call dao delete method`() = runTest {
        // Arrange
        val developer = Developer(
            id = 1,
            name = "Удаляемая компания",
            companyType = "Корпорация",
            location = "test"
        )

        // Act
        developerRepository.deleteDeveloper(developer)

        // Assert
        verify(mockDeveloperDao).delete(developer)
    }
}