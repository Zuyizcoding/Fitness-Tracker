package com.example.fitnesstracker

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.AsyncTask
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnesstracker.adapter.BadgeAdapter
import com.example.fitnesstracker.databinding.ActivityMainBinding
import com.example.fitnesstracker.service.StepBackgroundService
import com.example.fitnesstracker.service.StepBoundService
import com.example.fitnesstracker.service.StepForegroundService
import com.example.fitnesstracker.viewmodel.FitnessViewModel
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: FitnessViewModel by viewModels()
    private val badgeAdapter = BadgeAdapter()

    private var boundService: StepBoundService.StepBinder? = null
    private var isBound = false
    private var isGeminiButtonDebounced = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            boundService = service as StepBoundService.StepBinder
            isBound = true
            Toast.makeText(this@MainActivity, "Service Bound", Toast.LENGTH_SHORT).show()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            boundService = null
            isBound = false
        }
    }

    private val stepsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Steps updated via broadcast
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        setupButtons()
        
        LocalBroadcastManager.getInstance(this).registerReceiver(
            stepsReceiver, IntentFilter("ACTION_STEPS_UPDATED")
        )
    }

    private fun setupRecyclerView() {
        binding.rvBadges.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvBadges.adapter = badgeAdapter
    }

    private fun setupObservers() {
        viewModel.totalSteps.observe(this) { steps ->
            val total = steps ?: 0
            binding.tvTotalSteps.text = "$total steps"
            binding.pbSteps.progress = if (total > 10000) 10000 else total
        }

        viewModel.achievements.observe(this) { badges ->
            val earnedList = badges.map { it.badgeType }
            badgeAdapter.updateEarnedBadges(earnedList) { newBadge ->
                Snackbar.make(binding.root, "Badge unlocked: $newBadge!", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setupButtons() {
        binding.btnStartBgService.setOnClickListener {
            startService(Intent(this, StepBackgroundService::class.java))
        }

        binding.btnStartFgService.setOnClickListener {
            val intent = Intent(this, StepForegroundService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }

        binding.btnBindService.setOnClickListener {
            if (isBound) {
                val currentSteps = boundService?.getCurrentSteps() ?: 0
                Toast.makeText(this, "Bound steps: $currentSteps", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Service not bound", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCalculate.setOnClickListener {
            val currentSteps = viewModel.totalSteps.value ?: 0
            CalculateTask().execute(currentSteps)
        }

        binding.btnGetSuggestion.setOnClickListener {
            if (isGeminiButtonDebounced) return@setOnClickListener
            debounceGeminiButton()
            
            val currentSteps = viewModel.totalSteps.value ?: 0
            val latestRecord = viewModel.records.value?.firstOrNull()
            val calories = latestRecord?.calories ?: 0f
            val points = latestRecord?.points ?: 0

            binding.pbLoading.visibility = View.VISIBLE
            val geminiRepository = com.example.fitnesstracker.data.repository.GeminiRepository()
            
            lifecycleScope.launch(Dispatchers.IO) {
                val suggestion = geminiRepository.getWorkoutSuggestion(currentSteps, calories, points)
                withContext(Dispatchers.Main) {
                    binding.pbLoading.visibility = View.GONE
                    if (suggestion.contains("Keep moving") && suggestion.length < 60) {
                        Toast.makeText(this@MainActivity, "Could not reach Gemini. Check your connection.", Toast.LENGTH_SHORT).show()
                    } else {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("Your AI Coach")
                            .setMessage(suggestion)
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            }
        }
    }

    private fun debounceGeminiButton() {
        isGeminiButtonDebounced = true
        binding.btnGetSuggestion.isEnabled = false
        lifecycleScope.launch {
            delay(10000)
            isGeminiButtonDebounced = false
            binding.btnGetSuggestion.isEnabled = true
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, StepBoundService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(stepsReceiver)
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    inner class CalculateTask : AsyncTask<Int, Void, Pair<Float, Int>>() {
        override fun onPreExecute() {
            super.onPreExecute()
            binding.pbLoading.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: Int?): Pair<Float, Int> {
            val steps = params[0] ?: 0
            Thread.sleep(1000) // simulate processing
            val calories = steps * 0.04f
            val points = steps / 100
            return Pair(calories, points)
        }

        override fun onPostExecute(result: Pair<Float, Int>?) {
            super.onPostExecute(result)
            binding.pbLoading.visibility = View.GONE
            result?.let {
                binding.tvCalories.text = "Calories: ${it.first} kcal"
                binding.tvPoints.text = "Points: ${it.second} pts"
            }
        }
    }
}
