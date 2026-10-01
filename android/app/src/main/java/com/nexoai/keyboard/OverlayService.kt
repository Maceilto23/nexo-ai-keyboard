package com.nexoai.keyboard

import android.app.*
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageView
import kotlin.math.abs

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private var bubble: ImageView? = null
    private val channelId = "nexo_overlay"
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate() {
        super.onCreate()
        createChannel()
        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Nexo AI")
            .setContentText("Assistente flutuante ativo")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
        startForeground(2206, notification)
        showBubble()
    }

    private fun showBubble() {
        if (!Settings.canDrawOverlays(this) || bubble != null) return
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val view = ImageView(this).apply {
            setImageResource(R.mipmap.ic_launcher)
            scaleType = ImageView.ScaleType.CENTER_CROP
            elevation = dp(8).toFloat()
        }
        val params = WindowManager.LayoutParams(
            dp(54), dp(54),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(14)
            y = dp(220)
        }
        var downX = 0f; var downY = 0f; var startX = 0; var startY = 0; var moved = false
        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { downX = event.rawX; downY = event.rawY; startX = params.x; startY = params.y; moved = false; true }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downX; val dy = event.rawY - downY
                    if (abs(dx) > dp(5) || abs(dy) > dp(5)) moved = true
                    params.x = startX + dx.toInt(); params.y = startY + dy.toInt()
                    wm.updateViewLayout(view, params); true
                }
                MotionEvent.ACTION_UP -> { if (!moved) openAssistant(); true }
                else -> false
            }
        }
        wm.addView(view, params)
        bubble = view
    }

    private fun openAssistant() {
        val cb = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = cb.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString().orEmpty()
        startActivity(Intent(this, ProcessTextActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("text", text)
        })
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(NotificationChannel(channelId, "Nexo AI flutuante", NotificationManager.IMPORTANCE_LOW))
        }
    }

    override fun onDestroy() {
        bubble?.let { try { wm.removeView(it) } catch (_: Exception) {} }
        bubble = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
