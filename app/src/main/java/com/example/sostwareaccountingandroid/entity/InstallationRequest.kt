package com.example.sostwareaccountingandroid.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "installation_requests",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Software::class,
            parentColumns = ["id"],
            childColumns = ["softwareId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Device::class,
            parentColumns = ["id"],
            childColumns = ["deviceId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class InstallationRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val softwareId: Long,
    val deviceId: Long,
    val userId: Long,

    val requestDate: Long = System.currentTimeMillis(),
    val status: String, // "На рассмотрении", "Установлено", "Отклонено"
    val comment: String? = null,
)