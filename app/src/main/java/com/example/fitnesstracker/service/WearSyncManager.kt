package com.example.fitnesstracker.service

import android.content.Context
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class WearSyncManager(private val context: Context) {
    fun syncFitnessData(steps: Int, calories: Float, points: Int, badges: List<String>) {
        val putDataMapRequest = PutDataMapRequest.create("/fitness_data").apply {
            dataMap.putInt("steps", steps)
            dataMap.putFloat("calories", calories)
            dataMap.putInt("points", points)
            dataMap.putStringArrayList("badges", ArrayList(badges))
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }

        val request = putDataMapRequest.asPutDataRequest().setUrgent()
        Wearable.getDataClient(context).putDataItem(request)
    }
}
