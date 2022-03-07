package com.leobit.pizzadelivery.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Points::class], version = 6,
 exportSchema = true)
abstract class PointsDataBase : RoomDatabase() {
    abstract val pointsDao:PointsDao
    /* autoMigrations = [AutoMigration (from = 1, to = 2)]*/
     companion object{
         @Volatile
         private var INSTANCE: PointsDataBase? = null

         fun getInstance(context: Context): PointsDataBase {
             synchronized(this) {
                 var instance = INSTANCE
                 if (instance == null) {
                     instance = Room.databaseBuilder(
                         context.applicationContext,
                         PointsDataBase::class.java,
                         "points_database"
                     )
                         .fallbackToDestructiveMigration()
                         .build()
                     INSTANCE = instance
                 }
                 return instance
             }


         }
     }


}