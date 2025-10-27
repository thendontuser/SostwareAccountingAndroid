package com.example.sostwareaccountingandroid.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "departments")
data class Department(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
)