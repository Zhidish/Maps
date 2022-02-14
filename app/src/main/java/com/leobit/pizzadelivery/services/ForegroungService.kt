package com.leobit.pizzadelivery.services

import android.app.*
import android.content.Intent
import android.os.*
import android.widget.Toast
import com.leobit.pizzadelivery.MapsActivity
import com.leobit.pizzadelivery.R


class ForegroungService : Service() {
    private var serviceLooper: Looper? = null
    private var serviceHandler:Handler? = null
    private val CHANNEL_ID = "Foregraoung Service Id"
    private val channel : NotificationChannel = NotificationChannel(
        CHANNEL_ID,
        CHANNEL_ID,
        NotificationManager.IMPORTANCE_DEFAULT
    )
    private inner class ServiceHandler(looper: Looper):Handler(looper){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
        }
    }

    override fun onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()
            // Get the HandlerThread's Looper and use it for our Handler
           serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()

        val pendingIntent: PendingIntent =
            Intent(this, MapsActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getText(R.string.notification_title))
            .setContentText(getText(R.string.notification_message))
           // .setSmallIcon(R.drawable.ic_pizza)
            .setContentIntent(pendingIntent)
            .setTicker(getText(R.string.ticker_text))
            .build()

// Notification ID cannot be 0.
        startForeground(103, notification)



        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId
            serviceHandler?.sendMessage(msg)
        }

        // If we get killed, after returning from here, restart
        return START_STICKY
    }



    override fun onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
    }



    override fun onBind(intent: Intent?) = null

}