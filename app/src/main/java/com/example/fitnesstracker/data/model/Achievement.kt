package com.example.fitnesstracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievement")
data class Achievement(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val badgeType: String,
    val earnedAt: Long = System.currentTimeMillis()
)
