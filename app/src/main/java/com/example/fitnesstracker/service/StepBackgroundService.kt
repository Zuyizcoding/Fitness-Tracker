package com.example.fitnesstracker.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.fitnesstracker.FitnessApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.random.Random

class StepBackgroundService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    private val stepRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return
            val randomSteps = Random.nextInt(50, 151)
            
            serviceScope.launch {
                val repository = (application as FitnessApplication).repository
                repository.insertStepRecord(randomSteps)
                
                // Get updated total steps. Note: totalSteps LiveData might not be immediately updated on this background thread,
                // so we can query total by observing or just tracking. For simplicity in background, we rely on repository.
                val currentTotal = repository.totalSteps.value ?: 0
                val newTotal = currentTotal + randomSteps
                repository.checkAndAwardBadges(newTotal)

                val intent = Intent("ACTION_STEPS_UPDATED")
                intent.putExtra("total_steps", newTotal)
                LocalBroadcastManager.getInstance(this@StepBackgroundService).sendBroadcast(intent)
            }
            
            handler.postDelayed(this, 10000) // Every 10 seconds
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
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
}
