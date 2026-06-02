package com.example.fitnesstracker.data.repository

import com.example.fitnesstracker.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel

class GeminiRepository {
    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun getWorkoutSuggestion(steps: Int, calories: Float, points: Int): String {
        return try {
            val prompt = "User walked $steps steps today, burned $calories kcal, earned $points points. Give a specific 1-sentence workout suggestion to help reach 10,000 steps. Then give a 1-sentence personalized encouragement. Reply in plain text, no markdown."
            val response = model.generateContent(prompt)
            response.text ?: "Keep moving! Every step gets you closer to your goal."
        } catch (e: Exception) {
            e.printStackTrace()
            "Keep moving! Every step gets you closer to your goal."
        }
    }

    suspend fun getEncouragement(steps: Int): String {
        return try {
            val prompt = "The user has walked $steps steps. Give a short, energetic 1-sentence motivation."
            val response = model.generateContent(prompt)
            response.text ?: "Keep going, you're doing amazing!"
        } catch (e: Exception) {
            e.printStackTrace()
            "Keep going, you're doing amazing!"
        }
    }
}
