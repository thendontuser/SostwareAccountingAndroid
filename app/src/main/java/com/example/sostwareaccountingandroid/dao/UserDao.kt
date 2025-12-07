package com.example.sostwareaccountingandroid.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.sostwareaccountingandroid.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Long): User?

    @Query("SELECT * FROM users WHERE login = :login")
    suspend fun getUserByLogin(login: String): User?

    @Query("SELECT * FROM users WHERE (:departmentId IS NULL) OR (departmentId = :departmentId) OR (:departmentId = 0)")
    fun getUsersByDepartment(departmentId: Long?): Flow<List<User>>

    @Query("""
        SELECT * FROM users 
        WHERE 
            (:departmentId IS NULL AND departmentId IS NULL) OR 
            (departmentId = :departmentId) OR 
            (:departmentId = 0)
    """)
    fun getUsersByDepartment2(departmentId: Long?): Flow<List<User>>

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE role = :role")
    fun getUsersByRole(role: String): Flow<List<User>>

    @Query("SELECT COUNT(*) FROM users WHERE login = :login")
    suspend fun checkLoginExists(login: String): Int
}