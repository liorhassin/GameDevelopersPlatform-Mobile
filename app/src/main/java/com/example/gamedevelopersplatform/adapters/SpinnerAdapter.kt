package com.example.gamedevelopersplatform.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.gamedevelopersplatform.R

class CustomSpinnerAdapter(
    context: Context,
    textViewResourceId: Int,
    private val items: Array<String>
) : ArrayAdapter<String>(context, textViewResourceId, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_list_item, parent, false)
        val textView = view as TextView
        textView.text = items[position]
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_list_item, parent, false)
        val textView = view as TextView
        textView.text = items[position]
        return view
    }
}