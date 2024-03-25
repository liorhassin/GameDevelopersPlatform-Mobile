package com.example.gamedevelopersplatform

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //Loop:
        //If Signed in -> HomePage

        //else -> SignInPage

        //changeFragment(HomeFragment())
    }
}