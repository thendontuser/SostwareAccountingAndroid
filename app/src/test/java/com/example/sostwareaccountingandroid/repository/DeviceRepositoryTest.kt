package com.example.sostwareaccountingandroid.repository

import com.example.sostwareaccountingandroid.dao.DeviceDao
import com.example.sostwareaccountingandroid.entity.Device
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DeviceRepositoryTest {

    @Mock
    private lateinit var mockDeviceDao: DeviceDao

    private lateinit var deviceRepository: DeviceRepository

    @Before
    fun setUp() {
        deviceRepository = DeviceRepository(mockDeviceDao)
    }

    @Test
    fun `insertDevice should return generated id`() = runTest {
        // Arrange
        val device = Device(
            name = "Новый компьютер",
            osName = "test",
            ipAddress = "test",
            ramSize = 16,
            departmentId = 3
        )
        val expectedId = 100L
        `when`(mockDeviceDao.insertDevice(device)).thenReturn(expectedId)

        // Act
        val result = deviceRepository.insertDevice(device)

        // Assert
        assertEquals(expectedId, result)
        verify(mockDeviceDao).insertDevice(device)
    }

    @Test
    fun `updateDevice should call dao update method`() = runTest {
        // Arrange
        val device = Device(
            id = 1,
            name = "Обновленный компьютер",
            osName = "test",
            ipAddress = "test",
            ramSize = 16,
            departmentId = 3
        )

        // Act
        deviceRepository.updateDevice(device)

        // Assert
        verify(mockDeviceDao).updateDevice(device)
    }

    @Test
    fun `deleteDevice should call dao delete method`() = runTest {
        // Arrange
        val device = Device(
            id = 1,
            name = "Удаляемый компьютер",
            osName = "test",
            ipAddress = "test",
            ramSize = 16,
            departmentId = 3
        )

        // Act
        deviceRepository.deleteDevice(device)

        // Assert
        verify(mockDeviceDao).deleteDevice(device)
    }
}