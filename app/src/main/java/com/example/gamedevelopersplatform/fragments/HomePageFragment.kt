package com.example.gamedevelopersplatform.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gamedevelopersplatform.data.GameData
import com.example.gamedevelopersplatform.util.GameDevelopersAppUtil
import com.example.gamedevelopersplatform.R
import com.example.gamedevelopersplatform.dao.GameDao
import com.example.gamedevelopersplatform.database.AppDatabase
import com.example.gamedevelopersplatform.entity.Game
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class HomePageFragment : Fragment() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageRef: StorageReference

    private lateinit var roomDB: AppDatabase
    private lateinit var gameDao: GameDao

    private lateinit var recyclerView: RecyclerView
    private lateinit var gamesList: ArrayList<GameData>

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home_page, container, false)

        initializeParameters(view)
        fetchGamesFromDB {
            GameDevelopersAppUtil.populateRecyclerView(
                recyclerView,
                gamesList,
                storageRef,
                requireActivity(),
                R.id.homePageLayout
            )
            //GameDevelopersAppUtil.saveGamesToRoom(GameDevelopersAppUtil.convertGamesDataToGamesList(gamesList))
        }

        return view
    }

    private fun initializeParameters(view: View){
        firestore = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        roomDB = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "GameDevelopersPlatform-Room").build()
        gameDao = roomDB.gameDao()

        gamesList = arrayListOf()

        recyclerView = view.findViewById(R.id.homePageRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
    }

    private fun fetchGamesFromDB(onSuccess: () -> Unit){
        firestore.collection("games").get().addOnSuccessListener { documents ->
            documents.documents.iterator().forEach { gameDocument ->
                val gameData = gameDocument.toObject<GameData>()
                if(gameData!=null) {
                    gameData.gameId = gameDocument.id
                    gamesList.add(gameData)
                }
            }
        }.addOnCompleteListener{
            if(it.isSuccessful) onSuccess()
        }
    }

}