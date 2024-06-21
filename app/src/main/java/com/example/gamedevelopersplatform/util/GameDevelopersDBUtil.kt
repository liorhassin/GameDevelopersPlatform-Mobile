package com.example.gamedevelopersplatform.util

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.gamedevelopersplatform.dao.GameDao
import com.example.gamedevelopersplatform.dao.UserDao
import com.example.gamedevelopersplatform.database.AppDatabase
import com.example.gamedevelopersplatform.entity.Game
import com.example.gamedevelopersplatform.entity.User
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object GameDevelopersDBUtil {

    fun saveGamesToRoom(games: List<Game>, context: Context){
        val roomDB: AppDatabase = AppDatabase.getInstance(context)
        val gameDao: GameDao = roomDB.gameDao()

        (context as? LifecycleOwner)?.lifecycleScope?.launch(Dispatchers.IO) {
            val gamesCopy = ArrayList(games)
            gamesCopy.forEach { game ->
                gameDao.insert(game)
            }
        }
    }

    fun saveGameToRoom(game: Game, context: Context) {
        val roomDB: AppDatabase = AppDatabase.getInstance(context)
        val gameDao: GameDao = roomDB.gameDao()

        (context as? LifecycleOwner)?.lifecycleScope?.launch(Dispatchers.IO) {
            gameDao.insert(game)
        }
    }

    fun updateGameDataInRoom(game: Game, context: Context) {
        val roomDB: AppDatabase = AppDatabase.getInstance(context)
        val gameDao: GameDao = roomDB.gameDao()

        (context as? LifecycleOwner)?.lifecycleScope?.launch(Dispatchers.IO) {
            gameDao.update(game)
        }
    }

    fun deleteGameDataInRoom(gameId: String, context: Context) {
        val roomDB: AppDatabase = AppDatabase.getInstance(context)
        val gameDao: GameDao = roomDB.gameDao()

        (context as? LifecycleOwner)?.lifecycleScope?.launch(Dispatchers.IO) {
            gameDao.deleteById(gameId)
        }
    }

    fun gameDataToEntity(gameData: HashMap<String, String>): Game {
        return Game(
            name = gameData["name"],
            developerId = gameData["developerId"],
            price = gameData["price"],
            gameId = gameData["gameId"]!!,
            releaseDate = gameData["releaseDate"],
            image = gameData["image"]
        )
    }

    fun saveUserToRoom(user: User, context: Context) {
        val roomDB: AppDatabase = AppDatabase.getInstance(context)
        val userDao: UserDao = roomDB.userDao()

        (context as? LifecycleOwner)?.lifecycleScope?.launch(Dispatchers.IO) {
            userDao.insert(user)
        }
    }

    fun updateUserDataInRoom(user: User, context: Context) {
        val roomDB: AppDatabase = AppDatabase.getInstance(context)
        val userDao: UserDao = roomDB.userDao()

        (context as? LifecycleOwner)?.lifecycleScope?.launch(Dispatchers.IO) {
            userDao.update(user)
        }
    }
}