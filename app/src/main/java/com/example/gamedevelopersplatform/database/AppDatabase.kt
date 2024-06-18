package com.example.gamedevelopersplatform.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.gamedevelopersplatform.dao.CurrencyDao
import com.example.gamedevelopersplatform.dao.GameDao
import com.example.gamedevelopersplatform.dao.UserDao
import com.example.gamedevelopersplatform.entity.Currency
import com.example.gamedevelopersplatform.entity.Game
import com.example.gamedevelopersplatform.entity.User
import com.example.gamedevelopersplatform.type_converters.Converters

@Database(entities = [Game::class, User::class, Currency::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun userDao(): UserDao
    abstract fun currencyDao(): CurrencyDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context:Context): AppDatabase{
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database").fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}