package com.example.fitnesstracker.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.example.fitnesstracker.FitnessApplication

class StepBoundService : Service() {
    private val binder = StepBinder()
    private var currentSteps = 0
    private var currentCalories = 0f
    private var currentPoints = 0

    inner class StepBinder : Binder() {
        fun getCurrentSteps(): Int = currentSteps
        fun getCalories(): Float = currentCalories
        fun getPoints(): Int = currentPoints
    }

    override fun onBind(intent: Intent?): IBinder {
        // Read latest record asynchronously and cache it
        val repository = (application as FitnessApplication).repository
        Thread {
            val latest = repository.getLatestRecord()
            if (latest != null) {
                currentSteps = latest.steps
                currentCalories = latest.calories
                currentPoints = latest.points
            }
        }.start()
        
        return binder
    }
}
