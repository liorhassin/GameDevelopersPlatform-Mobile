package com.example.gamedevelopersplatform

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import com.google.android.material.imageview.ShapeableImageView

//TODO - IN GAME PAGE
//initialize parameters
//load game data to view elements
//edit xml to preview and edit mode
//enable switching between via code
//only show button to switch between modes to owner of the game.
//allow edit (name, image, release date, price)
//allow deletion of the project.

class GamePageFragment : Fragment() {

    //Preview Text
    private lateinit var gameName: TextView
    private lateinit var gamePrice: TextView
    private lateinit var gameReleaseDate: TextView
    private lateinit var gameImage: ShapeableImageView

    //Edit Text Inputs


    companion object{
        private const val GAME_DATA = "gameData"
        fun newInstance(gameData: GameData) = GamePageFragment().apply {
            arguments = bundleOf(
                GAME_DATA to gameData
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_game_page, container, false)

        initializeParameters(view)

        return view
    }

    private fun initializeParameters(view: View) {
        gameName = view.findViewById(R.id.gamePagePreviewGameTitle)
        gamePrice = view.findViewById(R.id.gamePagePreviewPriceText)
        gameReleaseDate = view.findViewById(R.id.gamePagePreviewReleaseDateText)
        gameImage = view.findViewById(R.id.gamePagePreviewGameImage)
    }
}