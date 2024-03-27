package com.example.gamedevelopersplatform

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class HomePageFragment : Fragment() {
    private lateinit var newRecyclerView: RecyclerView
    private lateinit var newProjectsList: ArrayList<ProjectData>
    lateinit var imageId : Array<Int>
    lateinit var nameId : Array<String>

    //TODO - Compare code with Idan's code to see if its onCreate or onCreateView.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //TODO - Change from statically defined array to array drawn from database.
        imageId = arrayOf(

        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_page, container, false)
    }
}