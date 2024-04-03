package com.example.gamedevelopersplatform

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MyGamesPageFragment : Fragment() {
    //data class Game(val image: String, val name: String, val price: String, val releaseDate: String)

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storageRef: StorageReference

    private lateinit var newRecyclerView: RecyclerView

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
                populateRecyclerView()
            },{ exception ->
                Log.e("fetchUserGamesFromDB", "Failed to fetch user games: $exception")
            })
        },{exception->
            Log.e("fetchUserGamesFromDB", "fetchUserGamesFromDB failed: $exception")
        })

//        imageId = arrayOf(
//            R.drawable.destiny_2_image,
//            R.drawable.baldurs_gate_3_image,
//            R.drawable.counter_strike_2_image,
//            R.drawable.helldivers_2_image,
//            R.drawable.warframe_image
//        )
//
//        nameId = arrayOf(
//            "Destiny 2",
//            "Baldur's Gate 3",
//            "Counter Strike 2",
//            "Helldivers 2",
//            "Warframe"
//        )
//
//        newRecyclerView = view.findViewById(R.id.myGamesPageRecyclerView)
//        newRecyclerView.layoutManager = LinearLayoutManager(requireContext())
//        newRecyclerView.setHasFixedSize(true)
//
//        newGamesList = arrayListOf<GameData>()
//        getUserData()
        return view
    }

    private fun initializeParameters(view: View) {
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        connectedUserId = firebaseAuth.currentUser?.uid.toString()
        userGamesList = arrayListOf<GameData>()
        userPageId = arguments?.getString(USER_ID_DATA_TO_FETCH).toString()

        newRecyclerView = view.findViewById(R.id.myGamesPageRecyclerView)
        newRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        newRecyclerView.setHasFixedSize(true)

    }

    //TODO - Make this function generic for util object, will be used for later features in app.
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

    //TODO - Fix loop to be inside the document.get() block to reduce populateRecycler calls.
    private fun fetchUserGamesFromDB(gamesId: List<String>, onSuccess: () -> Unit, onFailure: (exception: Exception) -> Unit){
        for(gameId in gamesId){
            val gameDocument = firestore.collection("games").document(gameId)
            gameDocument.get().addOnSuccessListener { document ->
                if(document.exists()){
                    val gameData = document.toObject<GameData>()
                    if(gameData!=null) {
                        userGamesList.add(gameData)
                    }
                }
            }.addOnFailureListener { exception ->
                Log.e("fetchUserGamesFromDB", "fetchUserGamesFromDB failed: $exception")
                onFailure(exception)
            }.addOnCompleteListener { it ->
                if(it.isSuccessful)
                    onSuccess()
            }
        }
    }

    private fun populateRecyclerView(){
        Log.d("games", userGamesList.toString())
        newRecyclerView.adapter = GamesAdapter(userGamesList)
    }
}