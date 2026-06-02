package com.example.fitnesstracker.viewmodel

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.FitnessApplication
import com.example.fitnesstracker.data.model.Achievement
import com.example.fitnesstracker.data.model.StepRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FitnessViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val repository = (application as FitnessApplication).repository

    val totalSteps: LiveData<Int> = repository.totalSteps
    val records: LiveData<List<StepRecord>> = repository.allRecords
    val achievements: LiveData<List<Achievement>> = repository.allAchievements

    fun addSteps(steps: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertStepRecord(steps)
            val currentTotal = totalSteps.value ?: 0
            repository.checkAndAwardBadges(currentTotal + steps)
        }
    }
}
