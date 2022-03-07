package com.leobit.pizzadelivery.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PointsDao {
    @Insert
    suspend fun insert(point: Points)

    @Update
    suspend fun update(point: Points)

    @Delete
    suspend fun delete(point: Points)

    @Query("SELECT * FROM pizza_points WHERE isDelivered=0")
    fun getAllUndeliveredPizza(): LiveData<List<Points>>

    @Query("SELECT * FROM pizza_points WHERE isDelivered=1")
    fun getAllDeliveredPizzas(): LiveData<List<Points>>

    @Query("UPDATE PIZZA_POINTS SET isDelivered=1 WHERE pointId=:idPoint")
     suspend fun updatePizzaDelivered(idPoint:Long)

     @Query("DELETE FROM pizza_points")
     suspend fun deleteAllColumns()

}