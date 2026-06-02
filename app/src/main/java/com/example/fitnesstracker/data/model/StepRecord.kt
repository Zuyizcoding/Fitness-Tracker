package com.example.fitnesstracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_record")
data class StepRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val steps: Int,
    val calories: Float,
    val points: Int
)
