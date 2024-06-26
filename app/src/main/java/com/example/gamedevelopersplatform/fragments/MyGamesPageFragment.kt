package com.example.gamedevelopersplatform.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gamedevelopersplatform.entity.Game
import com.example.gamedevelopersplatform.util.GameDevelopersGeneralUtil
import com.example.gamedevelopersplatform.R
import com.example.gamedevelopersplatform.database.AppDatabase
import com.example.gamedevelopersplatform.util.GameDevelopersDBUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyGamesPageFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storageRef: StorageReference

    private lateinit var recyclerView: RecyclerView
    private lateinit var titleText: TextView
    private lateinit var developerName: String

    private lateinit var connectedUserId: String
    private lateinit var userGamesList: ArrayList<Game>
    private lateinit var userPageId: String

    private lateinit var roomDatabase: AppDatabase

    companion object{
        private const val USER_ID_DATA_TO_FETCH = "userIdDataToFetch"
        fun newInstance(userIdDataToFetch: String) = MyGamesPageFragment().apply {
            arguments = bundleOf(
                USER_ID_DATA_TO_FETCH to userIdDataToFetch
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_my_games, container, false)


        initializeParameters(view)

        fetchUserGamesIdFromDB(userPageId, { gamesId ->
            fetchUserGamesFromDB(gamesId, {
                GameDevelopersGeneralUtil.populateRecyclerView(
                    recyclerView,
                    userGamesList,
                    storageRef,
                    requireActivity(),
                    R.id.myGamesPageLayout
                )
                if(connectedUserId != USER_ID_DATA_TO_FETCH) titleText.text = "$developerName Games"
                GameDevelopersDBUtil.saveGamesToRoom(userGamesList, requireContext())
            },{ exception ->
                Log.e("fetchUserGamesFromDB", "Failed to fetch user games: $exception")
            })
        },{ exception->
            Log.e("fetchUserGamesIdFromDB", "fetchUserGamesIdFromDB failed: $exception")
        })

        return view
    }

    private fun initializeParameters(view: View) {
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        connectedUserId = firebaseAuth.currentUser?.uid.toString()
        userGamesList = arrayListOf()
        userPageId = arguments?.getString(USER_ID_DATA_TO_FETCH).toString()
        developerName = "Developer Games"

        recyclerView = view.findViewById(R.id.myGamesPageRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        titleText = view.findViewById(R.id.myGamesPageTitle)

        roomDatabase = AppDatabase.getInstance(this.requireContext())
    }

    private fun fetchUserGamesIdFromDB(userId: String, onSuccess:(List<String>) -> Unit,
        onFailure:(exception:Exception) -> Unit){
        val firestoreUserDocument = firestore.collection("users").document(userId)
        firestoreUserDocument.get().addOnSuccessListener { document ->
            if(document.exists()){
                val userGamesId = document.get("userGames") as? List<String>
                developerName = document.get("nickname").toString()
                if(userGamesId!=null) {
                    onSuccess(userGamesId)
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("fetchUserGamesFromDB", "fetchUserGamesFromDB failed: $exception")
            onFailure(exception)
        }
    }

    private fun fetchUserGamesFromDB(gamesIdList: List<String>, onSuccess: () -> Unit, onFailure: (exception: Exception) -> Unit) {
        val gamesDocument = firestore.collection("games")
        gamesIdList.forEach { gameId ->
            gamesDocument.document(gameId).get().addOnSuccessListener { gameDocument ->
                val gameData = gameDocument.toObject<Game>()
                if (gameData != null) {
                    gameData.gameId = gameDocument.id
                    userGamesList.add(gameData)
                }
            }.addOnFailureListener {
                GameDevelopersDBUtil.getGamesByDeveloperId(userPageId, this@MyGamesPageFragment.requireContext())
            }.addOnCompleteListener {
                if (it.isSuccessful) onSuccess()
            }
        }
    }
}