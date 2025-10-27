package com.example.sostwareaccountingandroid.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "devices",
    foreignKeys = [
        ForeignKey(
            entity = Department::class,
            parentColumns = ["id"],
            childColumns = ["departmentId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Device(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val osName: String,
    val ipAddress: String? = null,
    val ramSize: Int, // в ГБ

    val departmentId: Long? = null,
)