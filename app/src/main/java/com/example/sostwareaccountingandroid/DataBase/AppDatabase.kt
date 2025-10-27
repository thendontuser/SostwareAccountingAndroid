package com.example.sostwareaccountingandroid.DataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

import com.example.sostwareaccountingandroid.dao.DepartmentDao
import com.example.sostwareaccountingandroid.dao.DeveloperDao
import com.example.sostwareaccountingandroid.dao.DeviceDao
import com.example.sostwareaccountingandroid.dao.InstallationRequestDao
import com.example.sostwareaccountingandroid.dao.SoftwareDao
import com.example.sostwareaccountingandroid.dao.SoftwareInstallationDao
import com.example.sostwareaccountingandroid.dao.UserDao

import com.example.sostwareaccountingandroid.entity.User
import com.example.sostwareaccountingandroid.entity.Department
import com.example.sostwareaccountingandroid.entity.Developer
import com.example.sostwareaccountingandroid.entity.Software
import com.example.sostwareaccountingandroid.entity.Device
import com.example.sostwareaccountingandroid.entity.InstallationRequest
import com.example.sostwareaccountingandroid.entity.SoftwareInstallation

@Database(
    entities = [
        User::class,
        Department::class,
        Developer::class,
        Software::class,
        Device::class,
        InstallationRequest::class,
        SoftwareInstallation::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun departmentDao(): DepartmentDao
    abstract fun developerDao(): DeveloperDao
    abstract fun softwareDao(): SoftwareDao
    abstract fun deviceDao(): DeviceDao
    abstract fun installationRequestDao(): InstallationRequestDao
    abstract fun softwareInstallationDao(): SoftwareInstallationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "software_tracking.db"
                )
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}