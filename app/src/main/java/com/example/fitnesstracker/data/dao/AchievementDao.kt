package com.example.fitnesstracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.fitnesstracker.data.model.Achievement

@Dao
interface AchievementDao {
    @Insert
    fun insert(achievement: Achievement)

    @Query("SELECT * FROM achievement ORDER BY earnedAt DESC")
    fun getAll(): LiveData<List<Achievement>>

    @Query("SELECT EXISTS(SELECT 1 FROM achievement WHERE badgeType = :badgeType LIMIT 1)")
    fun exists(badgeType: String): Boolean
}
