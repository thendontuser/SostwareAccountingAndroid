package com.example.sostwareaccountingandroid.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = Department::class,
            parentColumns = ["id"],
            childColumns = ["departmentId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
@kotlinx.parcelize.Parcelize
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val firstName: String,
    val lastName: String,
    val patronymic: String? = null,

    @ColumnInfo(index = true)
    val login: String,

    val passwordHash: String,
    val role: String, // "Администратор", "Пользователь"

    val departmentId: Long? = null,
) : Parcelable {
    fun getFullName(): String {
        return if (patronymic != null) {
            "$lastName $firstName $patronymic"
        } else {
            "$lastName $firstName"
        }
    }
}