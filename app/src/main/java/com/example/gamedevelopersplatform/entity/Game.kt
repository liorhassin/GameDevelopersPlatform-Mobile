package com.example.gamedevelopersplatform.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class Game(
    @PrimaryKey val gameId: String,
    @ColumnInfo(name = "name") var name: String?,
    @ColumnInfo(name = "image") var image: String?,
    @ColumnInfo(name = "price") var price: String?,
    @ColumnInfo(name = "releaseDate") var releaseDate: String?,
    @ColumnInfo(name = "developerId") var developerId: String?
)