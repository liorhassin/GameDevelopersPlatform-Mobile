package com.example.gamedevelopersplatform.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey val uid: String,
    @ColumnInfo(name = "nickname") val nickname: String,
    @ColumnInfo(name = "profileImage") val profileImage: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "birthDate") val birthDate: String,
    //@TypeConverters(Converters::class) @ColumnInfo(name = "userGames") val userGames: ArrayList<Game> = ArrayList()
)
