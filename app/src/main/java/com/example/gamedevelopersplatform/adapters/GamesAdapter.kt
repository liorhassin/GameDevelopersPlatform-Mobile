package com.example.gamedevelopersplatform.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.gamedevelopersplatform.R
import com.example.gamedevelopersplatform.entity.Game
import com.example.gamedevelopersplatform.fragments.GamePageFragment
import com.example.gamedevelopersplatform.util.GameDevelopersDBUtil
import com.example.gamedevelopersplatform.util.GameDevelopersGeneralUtil
import com.example.gamedevelopersplatform.util.GameDevelopersImageUtil
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.storage.StorageReference

class GamesAdapter(private val gamesList: ArrayList<Game>, private val storageRef: StorageReference, private val fragmentActivity: FragmentActivity, private val currentLayoutId: Int) :
    RecyclerView.Adapter<GamesAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.game_list_item,
        parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return gamesList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = gamesList[position]

        GameDevelopersImageUtil.loadImageFromDB(storageRef, currentItem.image,
            GameDevelopersGeneralUtil.GAMES_IMAGES_PATH, holder.image)

        holder.name.text = currentItem.name

        holder.gamePageButton.setOnClickListener {
            GameDevelopersGeneralUtil.changeFragmentFromFragment(fragmentActivity, currentLayoutId,
                GamePageFragment.newInstance(currentItem))
        }
    }

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val image: ShapeableImageView = itemView.findViewById(R.id.recyclerItemImage)
        val name: TextView = itemView.findViewById(R.id.recyclerItemGameName)
        val gamePageButton: Button = itemView.findViewById(R.id.recyclerItemGamePageButton)
    }
}