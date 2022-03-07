package com.leobit.pizzadelivery.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

@Entity(tableName = "pizza_points")
data class Points(
    @PrimaryKey(autoGenerate = true)
    var pointId:Long = 0L,
    @ColumnInfo(name="pizza_name")
    var pizza: String,
    @ColumnInfo(name="price")
    var price:Long,
    @ColumnInfo(name="isDelivered")
    var isDelivered: Boolean,
    @ColumnInfo(name="latitude")
    var latitude : Double,
    @ColumnInfo(name="longitude")
    var longitude : Double

    ) :ClusterItem{

    override fun getPosition() = LatLng(latitude, longitude)

    override fun getTitle()= if(isDelivered) "Delivered" else "Undelivered"

    override fun getSnippet() = pizza


}