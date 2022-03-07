package com.leobit.pizzadelivery.ui

import android.content.Context
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.leobit.pizzadelivery.R
import com.leobit.pizzadelivery.db.Points

class PlaceRender(
    private val context : Context,
    map: GoogleMap,
    clusteredManager : ClusterManager<Points>
) :DefaultClusterRenderer<Points>(context,map,clusteredManager) {
    private val bicycleIcon: BitmapDescriptor by lazy {
        val color = ContextCompat.getColor(
            context,
            R.color.black
        )
        BitmapHelper.vectorToBitMap(
            context,
            R.drawable.ic_pizza_slice_solid,
            color
        )
    }

    /**
     * Method called before the cluster item (the marker) is rendered.
     * This is where marker options should be set.
     */
    override fun onBeforeClusterItemRendered(
        item: Points,
        markerOptions: MarkerOptions
    ) {
        markerOptions.title(item.pizza)
            .position(item.position)
            .icon(bicycleIcon)
    }

    /**
     * Method called right after the cluster item (the marker) is rendered.
     * This is where properties for the Marker object should be set.
     */
    override fun onClusterItemRendered(clusterItem: Points, marker: Marker) {
        marker.tag = clusterItem
    }
}