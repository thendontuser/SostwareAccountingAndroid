package com.example.sostwareaccountingandroid.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "software_installations",
    foreignKeys = [
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
    ],
    indices = [
        Index(value = ["softwareId", "deviceId"], unique = true)
    ]
)
data class SoftwareInstallation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val softwareId: Long,
    val deviceId: Long,
    val installationDate: Long = System.currentTimeMillis(),
)