package com.example.gamedevelopersplatform.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.gamedevelopersplatform.entity.User

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAll(): List<User>

    @Query("SELECT * FROM users WHERE userId IN (:userIds)")
    fun getAllByIds(userIds: List<String>): List<User>

    @Query("SELECT * FROM users WHERE userId LIKE :userId")
    fun getById(userId: String): User

    @Query("SELECT * FROM users WHERE nickname LIKE :nickname")
    fun getByName(nickname: String): User

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<User>)

    @Query("DELETE FROM users WHERE userId = :userId")
    fun deleteById(userId: String)

    //Update section:
    @Query("UPDATE users SET nickname = :nickname WHERE userId = :userId")
    fun updateName(userId: String, nickname: String)

    @Query("UPDATE users SET profileImage = :image WHERE userId = :userId")
    fun updateImage(userId: String, image: String)

    @Query("UPDATE users SET birthDate = :birthDate WHERE userId = :userId")
    fun updateBirthdate(userId: String, birthDate: String)

    @Transaction
    fun update(user: User){
        user.nickname?.let { updateName(user.userId, it) }
        user.profileImage?.let { updateImage(user.userId, it) }
        user.birthDate?.let { updateBirthdate(user.userId, it) }
    }
}