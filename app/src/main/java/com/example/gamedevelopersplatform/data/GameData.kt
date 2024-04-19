package com.example.gamedevelopersplatform.data

import androidx.room.Entity
//TODO - Check if changing to entity still works with the entire project structure and usage.
@Entity(tableName = "game_table")
data class GameData(
    var image:String = ""
    , var name:String = ""
    , var price:String = ""
    , var releaseDate:String = ""
    , var developerId:String = ""
    , var gameId:String = ""
)
