package com.leobit.pizzadelivery.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.leobit.pizzadelivery.R
import com.leobit.pizzadelivery.db.Points

class MarkerInfoAdapter(
    private val contex: Context
) : GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(p0: Marker): View? {
        val place = p0.tag as Points
        val view = LayoutInflater.from(contex).inflate(R.layout.marker_info,null)
        view.findViewById<TextView>(R.id.text_view_title).text=place.pizza
        view.findViewById<TextView>(R.id.text_view_address).text = place.price.toString()
        return view
    }

    override fun getInfoWindow(p0: Marker): View? {



        return null
    }


}