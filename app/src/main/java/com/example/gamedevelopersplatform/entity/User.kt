package com.example.gamedevelopersplatform.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.gamedevelopersplatform.type_converters.Converters

@Entity(tableName = "users")
data class User(
    @PrimaryKey var userId: String = ""
    , @ColumnInfo(name = "birthDate") var birthDate: String = ""
    , @ColumnInfo(name = "email") var email: String = ""
    , @ColumnInfo(name = "nickname") var nickname: String = ""
    , @ColumnInfo(name = "profileImage") var profileImage: String = ""
    , @ColumnInfo(name = "userGames") @TypeConverters(Converters::class) var userGames: ArrayList<String> = arrayListOf()
)
