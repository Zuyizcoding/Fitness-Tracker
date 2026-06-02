package com.example.fitnesstracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.fitnesstracker.FitnessApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.random.Random

class StepForegroundService : Service() {
    private val CHANNEL_ID = "step_foreground_channel"
    private val NOTIFICATION_ID = 1
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private var quoteIndex = 0
    private lateinit var wearSyncManager: WearSyncManager

    private val quotes = listOf(
        "Keep going!",
        "Every step counts!",
        "You're doing great!",
        "Don't give up!",
        "Feel the burn!"
    )

    private val stepRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return
            val randomSteps = Random.nextInt(50, 151)
            
            serviceScope.launch {
                val repository = (application as FitnessApplication).repository
                repository.insertStepRecord(randomSteps)
                
                val currentTotal = repository.totalSteps.value ?: 0
                val newTotal = currentTotal + randomSteps
                repository.checkAndAwardBadges(newTotal)

                val intent = Intent("ACTION_STEPS_UPDATED")
                intent.putExtra("total_steps", newTotal)
                LocalBroadcastManager.getInstance(this@StepForegroundService).sendBroadcast(intent)
                
                // Update notification
                val quote = quotes[quoteIndex % quotes.size]
                quoteIndex++
                updateNotification(newTotal, quote)

                // WearOS Sync
                val latest = repository.getLatestRecord()
                val earnedBadges = repository.allAchievements.value?.map { it.badgeType } ?: emptyList()
                if (latest != null) {
                    wearSyncManager.syncFitnessData(
                        steps = newTotal,
                        calories = latest.calories,
                        points = latest.points,
                        badges = earnedBadges
                    )
                }
            }
            
            handler.postDelayed(this, 10000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        wearSyncManager = WearSyncManager(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            startForeground(NOTIFICATION_ID, buildNotification(0, quotes[0]))
            handler.post(stepRunnable)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        handler.removeCallbacks(stepRunnable)
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Step Tracker",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(steps: Int, quote: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Steps today: $steps | $quote")
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(steps: Int, quote: String) {
        val notification = buildNotification(steps, quote)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }
}
