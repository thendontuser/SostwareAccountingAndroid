package com.example.sostwareaccountingandroid.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "software",
    foreignKeys = [
        ForeignKey(
            entity = Developer::class,
            parentColumns = ["id"],
            childColumns = ["developerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Software(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val version: String,
    val licenseType: String, // "Бесплатная", "Коммерческая", "Открытая"
    val licenseStartDate: Long? = null,
    val licenseEndDate: Long? = null,

    val developerId: Long? = null,
    val logoPath: String? = null
)