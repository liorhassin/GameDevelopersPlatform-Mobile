package com.example.gamedevelopersplatform.data

data class UserData(
    var birthDate: String = "",
    var email: String = "",
    var nickname: String = "",
    var profileImage: String = "",
    var userGames: ArrayList<String> = arrayListOf()
)
