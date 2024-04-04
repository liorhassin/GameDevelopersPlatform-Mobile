package com.example.gamedevelopersplatform

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MyGamesPageFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storageRef: StorageReference

    private lateinit var recyclerView: RecyclerView
    private lateinit var titleText: TextView

    private lateinit var connectedUserId: String
    private lateinit var userGamesList: ArrayList<GameData>
    private lateinit var userPageId: String

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
                GameDevelopersAppUtil.populateRecyclerView(recyclerView, userGamesList)
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

        recyclerView = view.findViewById(R.id.myGamesPageRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        //TODO - Check if works after implementing Game Page.
        titleText = view.findViewById(R.id.myGamesPageTitle)
        if(connectedUserId == USER_ID_DATA_TO_FETCH) titleText.text = "My Games"
        else "Other User"

    }

    private fun fetchUserGamesIdFromDB(userId: String, onSuccess:(List<String>) -> Unit,
        onFailure:(exception:Exception) -> Unit){
        val firestoreUserDocument = firestore.collection("users").document(userId)
        firestoreUserDocument.get().addOnSuccessListener { document ->
            if(document.exists()){
                val userGamesId = document.get("userGames") as? List<String>
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
                val gameData = gameDocument.toObject<GameData>()
                if (gameData != null) userGamesList.add(gameData)
            }.addOnFailureListener { exception ->
                onFailure(exception)
            }.addOnCompleteListener {
                if (it.isSuccessful) onSuccess()
            }
        }
    }
}