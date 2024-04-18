package com.example.gamedevelopersplatform.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<User>)

    @Delete
    fun delete(user: User)

    @Update
    fun update(user: User)
}