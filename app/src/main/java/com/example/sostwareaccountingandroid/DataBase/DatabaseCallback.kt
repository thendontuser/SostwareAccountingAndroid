package com.example.sostwareaccountingandroid.DataBase

import androidx.room.RoomDatabase
import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.sostwareaccountingandroid.entity.Department
import com.example.sostwareaccountingandroid.entity.Developer
import com.example.sostwareaccountingandroid.entity.Device
import com.example.sostwareaccountingandroid.entity.Software
import com.example.sostwareaccountingandroid.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            populateInitialData()
        }
    }

    private suspend fun populateInitialData() {
        val database = AppDatabase.getInstance(context)

        // Создание отделов
        val departments = listOf(
            Department(id = 1, name = "Факультет информационных технологий и радиоэлектроники"),
            Department(id = 2, name = "Машиностроительный факультет"),
            Department(id = 3, name = "Гуманитарный факультет"),
            Department(4, name = "Юридический факультет")
        )

        departments.forEach { department ->
            database.departmentDao().insert(department)
        }

        // Создание производителей
        val developers = listOf(
            Developer(name = "Microsoft", companyType = "Корпорация", location = "США"),
            Developer(name = "Adobe", companyType = "Корпорация", location = "США"),
            Developer(name = "JetBrains", companyType = "ООО", location = "Россия"),
            Developer(name = "Apache Foundation", companyType = "Некоммерческая", location = "США")
        )

        developers.forEach { developer ->
            database.developerDao().insert(developer)
        }

        // Создание администратора по умолчанию
        val adminUser = User(
            firstName = "Администратор",
            lastName = "Системы",
            login = "admin",
            passwordHash = PasswordHasher.hashPassword("admin123"),
            role = "Администратор",
            departmentId = 1
        )

        database.userDao().insert(adminUser)

        // Создание тестового ПО
        val softwareList = listOf(
            Software(
                name = "Microsoft Office",
                version = "2021",
                licenseType = "Коммерческая",
                licenseStartDate = System.currentTimeMillis(),
                licenseEndDate = System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000,
                developerId = 1
            ),
            Software(
                name = "Adobe Photoshop",
                version = "CC 2023",
                licenseType = "Подписка",
                licenseStartDate = System.currentTimeMillis(),
                licenseEndDate = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
                developerId = 2
            ),
            Software(
                name = "IntelliJ IDEA",
                version = "2023.2",
                licenseType = "Бесплатная",
                developerId = 3
            )
        )

        softwareList.forEach { software ->
            database.softwareDao().insert(software)
        }

        // Создание тестовых устройств
        val devices = listOf(
            Device(
                name = "Компьютер кафедры ИТ-101",
                osName = "Windows 11",
                ipAddress = "192.168.1.101",
                ramSize = 16,
                departmentId = 1
            ),
            Device(
                name = "Ноутбук преподавателя",
                osName = "Windows 10",
                ipAddress = "192.168.1.102",
                ramSize = 8,
                departmentId = 1
            )
        )

        devices.forEach { device ->
            database.deviceDao().insert(device)
        }
    }
}