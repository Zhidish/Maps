package com.leobit.pizzadelivery.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.leobit.pizzadelivery.db.PointsDao
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.leobit.pizzadelivery.db.Points
import com.leobit.pizzadelivery.ui.Place
import kotlinx.coroutines.launch

class PointsViewModel(val dao: PointsDao, val cluster: ClusterManager<Points>) : ViewModel() {
    var allPositions = dao.getAllUndeliveredPizza()
    internal var isPizzaPoint: Boolean = false
    var point: Points? = null
    fun addPoints(point: Points) {
        viewModelScope.launch {
            dao.insert(point)
        }
    }

    fun updatePoint() {
        viewModelScope.launch {
            point?.let {
                dao.updatePizzaDelivered(it.pointId)
            }
        }
    }

    fun getPoints() {
        allPositions = dao.getAllUndeliveredPizza()
    }

    init {
        getPoints()
    }

    internal fun checkAllPositions(latLng: LatLng) {
        allPositions.value?.forEach {
            if (getDistanceFromLatLonInKm(
                    latLng.latitude,
                    latLng.longitude,
                    it.latitude,
                    it.longitude
                ) <= 20
            ) {
                point = it
                isPizzaPoint = true
            } else {
                isPizzaPoint = false
            }

        }
    }

    fun getDistanceFromLatLonInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        var R = 6371; // Radius of the earth in km
        var dLat = deg2rad(lat2 - lat1);  // deg2rad below
        var dLon = deg2rad(lon2 - lon1);
        var a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                    Math.sin(dLon / 2) * Math.sin(dLon / 2)
        ;
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        var d = R * c; // Distance in km
        return (d * 1000.0);
    }

    fun deg2rad(deg: Double): Double {
        return deg * (Math.PI / 180)
    }

}