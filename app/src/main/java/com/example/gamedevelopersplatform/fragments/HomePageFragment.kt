package com.example.gamedevelopersplatform.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gamedevelopersplatform.entity.Game
import com.example.gamedevelopersplatform.util.GameDevelopersAppUtil
import com.example.gamedevelopersplatform.R
import com.example.gamedevelopersplatform.database.AppDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomePageFragment : Fragment() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageRef: StorageReference

    private lateinit var recyclerView: RecyclerView
    private lateinit var gamesList: ArrayList<Game>

    private lateinit var roomDatabase: AppDatabase

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
            GameDevelopersAppUtil.saveGamesToRoom(gamesList, requireContext())
        }

        return view
    }

    private fun initializeParameters(view: View){
        firestore = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        gamesList = arrayListOf()

        recyclerView = view.findViewById(R.id.homePageRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        roomDatabase = AppDatabase.getInstance(this.requireContext())
    }

    private fun fetchGamesFromDB(onSuccess: () -> Unit){
        firestore.collection("games").get()
            .addOnSuccessListener { documents ->
                documents.documents.iterator().forEach { gameDocument ->
                    val gameData = gameDocument.toObject<Game>()
                    if (gameData != null) {
                        gameData.gameId = gameDocument.id
                        gamesList.add(gameData)
                    }
                }
            }
            .addOnFailureListener {
                CoroutineScope(Dispatchers.IO).launch {
                    gamesList = ArrayList(roomDatabase.gameDao().getAll())
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                }
            }
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onSuccess()
                }
            }
    }

}