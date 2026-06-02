package com.example.fitnesstracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.fitnesstracker.data.model.StepRecord

@Dao
interface StepDao {
    @Insert
    fun insert(stepRecord: StepRecord)

    @Query("SELECT SUM(steps) FROM step_record")
    fun getTotalSteps(): LiveData<Int>

    @Query("SELECT * FROM step_record ORDER BY timestamp DESC")
    fun getAllRecords(): LiveData<List<StepRecord>>

    @Query("SELECT * FROM step_record ORDER BY timestamp DESC LIMIT 1")
    fun getLatestRecord(): StepRecord?
}
