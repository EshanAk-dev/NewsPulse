package com.example.newsapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

// To customize spinner items
class DividerSpinnerAdapter(
    context: Context,
    resource: Int,
    objects: Array<String>
) : ArrayAdapter<String>(context, resource, objects) {

    // Customize the view for the selected item in the spinner
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, parent, false)
        }

        val textView = view?.findViewById<TextView>(android.R.id.text1)
        textView?.text = getItem(position)
        return view!!
    }

    // Customize the dropdown view with a divider between each item
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.spinner_item_with_divider, parent, false)
        }

        val textView = view?.findViewById<TextView>(R.id.spinner_item_text)
        textView?.text = getItem(position)

        return view!!
    }
}
