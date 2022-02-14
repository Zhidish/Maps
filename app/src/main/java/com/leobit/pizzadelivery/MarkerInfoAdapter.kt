package com.leobit.pizzadelivery

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import org.w3c.dom.Text

class MarkerInfoAdapter(
    private val contex: Context
) : GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(p0: Marker): View? {
        val place = p0.tag as Place
        val view = LayoutInflater.from(contex).inflate(R.layout.marker_info,null)
        view.findViewById<TextView>(R.id.text_view_title).text=place.name
        view.findViewById<TextView>(R.id.text_view_address).text = place.address.toString()

        return view
    }

    override fun getInfoWindow(p0: Marker): View? {

        return null


    }


}