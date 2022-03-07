package com.leobit.pizzadelivery.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.os.*
import android.widget.Toast
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.leobit.pizzadelivery.ui.MapsActivity
import com.leobit.pizzadelivery.R
import com.leobit.pizzadelivery.ui.viewmodel.ServiceViewModel


class ForegroungService : LifecycleService() {

    companion object {
        var lastLiveLocation: MutableLiveData<LatLng> = MutableLiveData(LatLng(0.0, 0.0))
        var isRunning : MutableLiveData<Boolean> = MutableLiveData(true)
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mtask: Task<LocationSettingsResponse>
    private lateinit var mLocationRequest_H: LocationRequest
    private lateinit var mLocationCallback: LocationCallback
    private var mMe: LatLng = LatLng(0.0, 0.0)
    private var mServiceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null
    private val CHANNEL_ID = "Foregraoung Service Id"
    private val mChannel: NotificationChannel = NotificationChannel(
        CHANNEL_ID,
        CHANNEL_ID,
        NotificationManager.IMPORTANCE_DEFAULT
    )


    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val isRunningObserver = Observer<Boolean> {
            if (it) updateLocationTracking(it)
        }
        val handler =
          /*  HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
                start()
                mServiceLooper = looper
                serviceHandler = ServiceHandler(looper)
            }*/

       isRunning.observe(this, isRunningObserver)


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()


        val pendingIntent: PendingIntent =
            Intent(this, MapsActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0 ?: return
                for (location in p0.locations) {
                    lastLiveLocation.value= LatLng(location.latitude, location.longitude)
                }
            }
        }



        getSystemService(NotificationManager::class.java).createNotificationChannel(mChannel)

        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentTitle(getText(R.string.notification_title))
            .setContentText(getText(R.string.notification_message))
            // .setSmallIcon(R.drawable.ic_pizza)
            .setContentIntent(pendingIntent)
            .setTicker(getText(R.string.ticker_text))
            .build()


        startForeground(103, notification)


        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId
            serviceHandler?.sendMessage(msg)
        }

        return super.onStartCommand(intent, flags, startId)

    }


    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
    }


    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isRunnig: Boolean) {
        if (isRunnig) {
            mLocationRequest_H = LocationRequest().apply {
                interval = 5000L
                fastestInterval = 2000L
                priority = PRIORITY_HIGH_ACCURACY
            }
            fusedLocationClient.requestLocationUpdates(
                mLocationRequest_H,
                mLocationCallback,
                 Looper.getMainLooper()
            )
        } else {
            fusedLocationClient.removeLocationUpdates(mLocationCallback)
        }


    }


}