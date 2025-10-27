package com.example.sostwareaccountingandroid.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "developers")
data class Developer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val companyType: String, // "ООО", "ИП", "Корпорация" и т.д.
    val location: String? = null,
)