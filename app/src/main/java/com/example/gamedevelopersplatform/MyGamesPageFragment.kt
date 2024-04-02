package com.example.gamedevelopersplatform

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MyGamesPageFragment : Fragment() {
    data class Game(val image: String, val name: String, val price: String, val releaseDate: String)

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storageRef: StorageReference

    private lateinit var newRecyclerView: RecyclerView
    private lateinit var newGamesList: ArrayList<GameData>
    private lateinit var imageId : Array<Int>
    private lateinit var nameId : Array<String>

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

        initializeParameters()
        fetchUserGamesIdFromDB(USER_ID_DATA_TO_FETCH, { gamesId ->
            fetchUserGamesFromDB(gamesId, { gamesList ->
                populateRecyclerView(gamesList)
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

    private fun initializeParameters() {
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        connectedUserId = firebaseAuth.currentUser?.uid.toString()
        userGamesList = arrayListOf<GameData>()
        userPageId = arguments?.getString(USER_ID_DATA_TO_FETCH).toString()

    }

    //TODO - Make this function generic for util object, will be used for later features in app.
    private fun fetchUserGamesIdFromDB(userId: String, onSuccess:(List<String>) -> Unit,
        onFailure:(exception:Exception) -> Unit){
        val firestoreUserDocument = firestore.collection("users").document(userId)
        firestoreUserDocument.get().addOnSuccessListener { document ->
            if(document.exists()){
                val userGamesId = document.get("userGames") as? List<String>
                if(userGamesId!=null) {
                    Log.e("fetchedGames", userGamesId.toString())
                    onSuccess(userGamesId)
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("fetchUserGamesFromDB", "fetchUserGamesFromDB failed: $exception")
            onFailure(exception)
        }
    }

    private fun fetchUserGamesFromDB(gamesId: List<String>, onSuccess: (List<Game>) -> Unit, onFailure: (exception: Exception) -> Unit){

    }

    private fun populateRecyclerView(userGames: List<Game>){
//        for(game in userGames){
//            val gameData = GameData()
//        }
    }

    private fun getUserData(){
        for(i in imageId.indices){
            //TODO - GameData should contain gameImage, gameName, gamePrice, gameReleaseDate, gameDeveloperId, gameDeveloperName
            val project = GameData(imageId[i],nameId[i])
            newGamesList.add(project)
        }
        newRecyclerView.adapter = GamesAdapter(newGamesList)
    }
}