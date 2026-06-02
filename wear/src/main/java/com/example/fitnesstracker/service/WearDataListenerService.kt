package com.example.fitnesstracker.service

import android.content.Intent
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class WearDataListenerService : WearableListenerService() {
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/fitness_data") {
                val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                val dataMap = dataMapItem.dataMap
                
                WearDataStore.steps = dataMap.getInt("steps")
                WearDataStore.badges = dataMap.getStringArrayList("badges") ?: emptyList()
                
                val intent = Intent("ACTION_UPDATE_WATCH_FACE")
                sendBroadcast(intent)
            }
        }
    }
}
