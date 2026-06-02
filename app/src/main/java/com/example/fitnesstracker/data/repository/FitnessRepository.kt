package com.example.fitnesstracker.data.repository

import androidx.lifecycle.LiveData
import com.example.fitnesstracker.data.dao.AchievementDao
import com.example.fitnesstracker.data.dao.StepDao
import com.example.fitnesstracker.data.model.Achievement
import com.example.fitnesstracker.data.model.StepRecord

class FitnessRepository(
    private val stepDao: StepDao,
    private val achievementDao: AchievementDao
) {
    val totalSteps: LiveData<Int> = stepDao.getTotalSteps()
    val allAchievements: LiveData<List<Achievement>> = achievementDao.getAll()
    val allRecords: LiveData<List<StepRecord>> = stepDao.getAllRecords()

    fun insertStepRecord(steps: Int) {
        val calories = steps * 0.04f
        val points = steps / 100
        val record = StepRecord(
            steps = steps,
            calories = calories,
            points = points
        )
        stepDao.insert(record)
    }

    fun checkAndAwardBadges(totalSteps: Int) {
        val milestones = listOf(
            1000 to "1000_STEPS",
            5000 to "5000_STEPS",
            10000 to "10000_STEPS"
        )

        for ((milestone, badgeType) in milestones) {
            if (totalSteps >= milestone && !achievementDao.exists(badgeType)) {
                achievementDao.insert(Achievement(badgeType = badgeType))
            }
        }
    }

    fun getLatestRecord(): StepRecord? {
        return stepDao.getLatestRecord()
    }
}
