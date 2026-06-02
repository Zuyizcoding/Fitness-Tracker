package com.example.fitnesstracker

import android.app.Application
import com.example.fitnesstracker.data.AppDatabase
import com.example.fitnesstracker.data.repository.FitnessRepository

class FitnessApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { FitnessRepository(database.stepDao(), database.achievementDao()) }
}
