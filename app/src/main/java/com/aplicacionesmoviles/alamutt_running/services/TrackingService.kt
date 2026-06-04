package com.aplicacionesmoviles.alamutt_running.services

import android.app.*
import android.content.Intent
import android.media.AudioAttributes
import android.os.*
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.aplicacionesmoviles.alamutt_running.MainActivity
import com.aplicacionesmoviles.alamutt_running.R

class TrackingService : Service() {
    private val CHANNEL_ID = "tracking_channel_v2"
    private val NOTIFICATION_ID = 1
    private var isRunning = true
    private var lastTime = "00:00"

    companion object {
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_UPDATE = "UPDATE_TIME"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_PAUSE) {
            isRunning = false
            sendBroadcast(Intent("CONTROL_RUN").putExtra("action", ACTION_PAUSE))
        } else if (action == ACTION_RESUME) {
            isRunning = true
            sendBroadcast(Intent("CONTROL_RUN").putExtra("action", ACTION_RESUME))
        }

        val receivedTime = intent?.getStringExtra("time")
        if (receivedTime != null) {
            lastTime = receivedTime
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.notify(NOTIFICATION_ID, createNotification(lastTime, isRunning))

        return START_STICKY
    }

    private fun createNotification(time: String, running: Boolean): Notification {
        val activityIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val nextAction = if (running) ACTION_PAUSE else ACTION_RESUME
        val actionIntent = Intent(this, TrackingService::class.java).apply { action = nextAction }

        val actionPI = PendingIntent.getService(
            this,
            if (running) 100 else 101,
            actionIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Carrera en curso")
            .setContentText("Tiempo: $time")
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentIntent(activityIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(
                if (running) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (running) "Pausar" else "Continuar",
                actionPI
            )
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = Settings.System.DEFAULT_NOTIFICATION_URI
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(CHANNEL_ID, "Seguimiento", NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(soundUri, audioAttributes)
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
        startForeground(NOTIFICATION_ID, createNotification("00:00", true))
    }

    override fun onBind(intent: Intent?): IBinder? = null
}