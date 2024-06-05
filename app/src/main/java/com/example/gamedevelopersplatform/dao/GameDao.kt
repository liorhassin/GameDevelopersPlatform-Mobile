package com.example.gamedevelopersplatform.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

    @Query("DELETE FROM games WHERE gameId = :gameId")
    fun deleteById(gameId: String)


    //Update section:
    @Query("UPDATE games SET name = :name WHERE gameId = :gameId")
    fun updateName(gameId: String, name: String)

    @Query("UPDATE games SET image = :image WHERE gameId = :gameId")
    fun updateImage(gameId: String, image: String)

    @Query("UPDATE games SET price = :price WHERE gameId = :gameId")
    fun updatePrice(gameId: String, price: String)

    @Query("UPDATE games SET releaseDate = :releaseDate WHERE gameId = :gameId")
    fun updateReleaseDate(gameId: String, releaseDate: String)

    @Transaction
    fun update(game: Game){
        game.name?.let { updateName(game.gameId, it) }
        game.image?.let { updateImage(game.gameId, it) }
        game.price?.let { updatePrice(game.gameId, it) }
        game.releaseDate?.let { updateReleaseDate(game.gameId, it) }
    }

}