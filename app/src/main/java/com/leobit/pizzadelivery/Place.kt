package com.leobit.pizzadelivery

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class Place(
    val name: String,
    val latLng: LatLng,
    val address: String,
    val rating: Float
 ) : ClusterItem{
    override fun getPosition() = latLng

    override fun getTitle()= name

    override fun getSnippet() =name


}