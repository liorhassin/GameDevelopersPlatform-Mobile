package com.example.gamedevelopersplatform.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.gamedevelopersplatform.dao.GameDao
import com.example.gamedevelopersplatform.dao.UserDao
import com.example.gamedevelopersplatform.entity.Game
import com.example.gamedevelopersplatform.entity.User

@Database(entities = [Game::class, User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun userDao(): UserDao
    //abstract fun authenticationDao(): AuthenticationDao //(Maybe to keep the login information)
}