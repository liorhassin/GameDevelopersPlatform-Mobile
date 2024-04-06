package com.example.gamedevelopersplatform

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class GamesAdapter(private val gamesList: ArrayList<GameData>, private val storageRef: StorageReference) :
    RecyclerView.Adapter<GamesAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.game_list_item,
        parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return gamesList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = gamesList[position]
        GameDevelopersAppUtil.loadImageFromDB(storageRef, currentItem.image, holder.image)
        holder.name.text = currentItem.name
    }

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val image: ShapeableImageView = itemView.findViewById(R.id.recyclerItemImage)
        val name: TextView = itemView.findViewById(R.id.recyclerItemGameName)
    }
}