package com.example.gamedevelopersplatform.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import com.example.gamedevelopersplatform.entity.Game

@Dao
interface GameDao {
    @Query("SELECT * FROM game")
    fun getAll(): List<Game>

    @Query("SELECT * FROM game WHERE gid IN (:gameIds)")
    fun findAllByIds(gameIds: List<String>): List<Game>

    @Query("SELECT * FROM game WHERE gid LIKE :gameId")
    fun findById(gameId: String): Game

    @Query("SELECT * FROM game WHERE name LIKE :name")
    fun findByName(name: String): Game

    @Delete
    fun delete(game: Game)
}