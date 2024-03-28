package com.example.gamedevelopersplatform

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MyGamesPageFragment : Fragment() {
    private lateinit var newRecyclerView: RecyclerView
    private lateinit var newGamesList: ArrayList<GameData>
    lateinit var imageId : Array<Int>
    lateinit var nameId : Array<String>

    //TODO - Compare code with Idan's code to see if its onCreate or onCreateView.

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_my_games, container, false)
        imageId = arrayOf(
            R.drawable.destiny_2_image,
            R.drawable.baldurs_gate_3_image,
            R.drawable.counter_strike_2_image,
            R.drawable.helldivers_2_image,
            R.drawable.warframe_image
        )

        nameId = arrayOf(
            "Destiny 2",
            "Baldur's Gate 3",
            "Counter Strike 2",
            "Helldivers 2",
            "Warframe"
        )

        newRecyclerView = view.findViewById(R.id.myGamesPageRecyclerView)
        newRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        newRecyclerView.setHasFixedSize(true)

        newGamesList = arrayListOf<GameData>()
        getUserData()
        return view
    }

    private fun getUserData(){
        for(i in imageId.indices){
            val project = GameData(imageId[i],nameId[i])
            newGamesList.add(project)
        }
        newRecyclerView.adapter = GamesAdapter(newGamesList)
    }
}