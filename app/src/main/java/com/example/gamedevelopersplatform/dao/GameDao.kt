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
//    @Query("SELECT * FROM game")
//    fun getAll(): ArrayList<Game>
//
//    @Query("SELECT * FROM game WHERE gid IN (:gameIds)")
//    fun findAllByIds(gameIds: ArrayList<String>): ArrayList<Game>
//
//    @Query("SELECT * FROM game WHERE gid LIKE :gameId")
//    fun findById(gameId: String): Game
//
//    @Query("SELECT * FROM game WHERE name LIKE :name")
//    fun findByName(name: String): Game
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insert(game: Game)
//
////    @Insert(onConflict = OnConflictStrategy.REPLACE)
////    fun insertAll(games: ArrayList<Game>)
//
//    @Delete
//    fun delete(game: Game)
//
//    @Update
//    fun update(game: Game)
}