package com.leobit.pizzadelivery.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.leobit.pizzadelivery.db.Points
import com.leobit.pizzadelivery.db.PointsDao
import com.leobit.pizzadelivery.ui.Place
import java.lang.IllegalStateException

class PointsViewModelFactory(val dao: PointsDao, val cluster: ClusterManager<Points>) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PointsViewModel::class.java))
            return PointsViewModel(dao, cluster) as T
        throw IllegalStateException("Unknown VIewModel")
    }
}