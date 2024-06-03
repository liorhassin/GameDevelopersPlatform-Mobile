package com.example.gamedevelopersplatform.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gamedevelopersplatform.entity.Game

@Dao
interface GameDao {
    @Query("SELECT * FROM games")
    fun getAll(): List<Game>

    @Query("SELECT * FROM games WHERE gameId IN (:gameIds)")
    fun findAllByIds(gameIds: List<String>): List<Game>

    @Query("SELECT * FROM games WHERE gameId LIKE :gameId")
    fun findById(gameId: String): Game?

    @Query("SELECT * FROM games WHERE name LIKE :name")
    fun findByName(name: String): Game?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(game: Game)

    @Delete
    @Query("DELETE FROM games WHERE gameId = :gameId")
    fun deleteById(gameId: String)

    @Update
    //TODO - @Query() - Complete later, Query to update all parameters.
    fun update(game: Game)
}