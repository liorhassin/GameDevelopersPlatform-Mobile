package com.example.gamedevelopersplatform.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.gamedevelopersplatform.entity.User

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun findAllByIds(userIds: List<String>): List<User>

    @Query("SELECT * FROM user WHERE uid LIKE :userId")
    fun findById(userId: String): User

    @Query("SELECT * FROM user WHERE nickname LIKE :nickname")
    fun findByName(nickname: String): User

    @Insert
    fun insertAll(vararg users: User)

    @Delete
    fun delete(user: User)
}