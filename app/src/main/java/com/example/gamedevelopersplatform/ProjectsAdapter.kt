package com.example.gamedevelopersplatform

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView

class ProjectsAdapter(private val projectsList: ArrayList<ProjectData>) :
    RecyclerView.Adapter<ProjectsAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.project_list_item,
        parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return projectsList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = projectsList[position]
        holder.projectImage.setImageResource(currentItem.projectImage)
        holder.projectName.text = currentItem.projectName
    }

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val projectImage: ShapeableImageView = itemView.findViewById(R.id.project_image)
        val projectName: TextView = itemView.findViewById(R.id.project_name)
    }
}