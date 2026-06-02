package com.example.fitnesstracker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.view.SurfaceHolder

class WatchFaceService : CanvasWatchFaceService() {

    override fun onCreateEngine(): Engine {
        return Engine()
    }

    inner class Engine : CanvasWatchFaceService.Engine() {
        private lateinit var textPaint: Paint
        private lateinit var progressPaint: Paint
        private lateinit var backgroundPaint: Paint
        private lateinit var badgeEarnedPaint: Paint
        private lateinit var badgeLockedPaint: Paint
        
        private val updateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                invalidate()
            }
        }

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)
            setWatchFaceStyle(
                WatchFaceStyle.Builder(this@WatchFaceService)
                    .setAcceptsTapEvents(true)
                    .build()
            )

            backgroundPaint = Paint().apply { color = Color.parseColor("#1A1A2E") }
            textPaint = Paint().apply {
                color = Color.WHITE
                textSize = 40f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            progressPaint = Paint().apply {
                color = Color.GREEN
                style = Paint.Style.STROKE
                strokeWidth = 10f
                isAntiAlias = true
            }
            badgeEarnedPaint = Paint().apply {
                color = Color.parseColor("#FFD700")
                isAntiAlias = true
            }
            badgeLockedPaint = Paint().apply {
                color = Color.GRAY
                isAntiAlias = true
            }
        }
        
        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                val filter = IntentFilter("ACTION_UPDATE_WATCH_FACE")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
                } else {
                    registerReceiver(updateReceiver, filter)
                }
            } else {
                try { unregisterReceiver(updateReceiver) } catch (e: Exception) {}
            }
        }

        override fun onDraw(canvas: Canvas, bounds: Rect) {
            val steps = WearDataStore.steps
            val badges = WearDataStore.badges

            // Background
            canvas.drawRect(bounds, backgroundPaint)

            val centerX = bounds.exactCenterX()
            val centerY = bounds.exactCenterY()

            // Progress Circle
            val progress = (steps / 10000f).coerceIn(0f, 1f)
            val radius = bounds.width() / 2f - 20f
            canvas.drawArc(
                centerX - radius, centerY - radius,
                centerX + radius, centerY + radius,
                -90f, 360f * progress, false, progressPaint
            )

            // Center Text
            canvas.drawText("$steps steps", centerX, centerY, textPaint)

            // Badge Icons (bottom arc)
            val badgeTypes = listOf("1000_STEPS", "5000_STEPS", "10000_STEPS")
            val spacing = 30f
            val startX = centerX - spacing
            val badgeY = centerY + 50f

            for ((index, type) in badgeTypes.withIndex()) {
                val paint = if (badges.contains(type)) badgeEarnedPaint else badgeLockedPaint
                canvas.drawCircle(startX + index * spacing, badgeY, 10f, paint)
            }
        }
    }
}

object WearDataStore {
    var steps: Int = 0
    var badges: List<String> = emptyList()
}
