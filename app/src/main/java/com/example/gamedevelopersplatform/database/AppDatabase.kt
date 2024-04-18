package com.example.gamedevelopersplatform.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.gamedevelopersplatform.dao.GameDao
import com.example.gamedevelopersplatform.entity.Game

@Database(entities = [Game::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    //TODO - After gameDao works well with RoomDatabase
    //abstract fun userDao(): UserDao
    //abstract fun authenticationDao(): AuthenticationDao //(Maybe to keep the login information)
}