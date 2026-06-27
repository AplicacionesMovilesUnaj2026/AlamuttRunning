package com.aplicacionesmoviles.alamutt_running.features.run.data

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.os.*
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.aplicacionesmoviles.alamutt_running.MainActivity
import com.aplicacionesmoviles.alamutt_running.features.settings.LanguageManager
import com.aplicacionesmoviles.alamutt_running.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class TrackingNotificationService : Service() {
    private val CHANNEL_ID = "tracking_channel_v2"
    private val NOTIFICATION_ID = 1
    private var isRunning = true
    private var lastTime = "00:00"
    private var lastDistance = "0.00 km"
    private var lastPace = "--:--"

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.wrapContext(newBase))
    }

    companion object {
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_UPDATE = "UPDATE_TIME"
        const val ACTION_LOCATION_UPDATE = "LOCATION_UPDATE"
        const val EXTRA_LAT = "lat"
        const val EXTRA_LNG = "lng"
        const val EXTRA_ACCURACY = "accuracy"
        const val EXTRA_SPEED = "speed"
        const val EXTRA_BEARING = "bearing"
        const val EXTRA_TIME = "time"
        const val EXTRA_GPS_AVAILABLE = "gps_available"
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

        intent?.getStringExtra("time")?.let { lastTime = it }
        intent?.getStringExtra("distance")?.let { lastDistance = it }
        intent?.getStringExtra("pace")?.let { lastPace = it }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.notify(NOTIFICATION_ID, createNotification(lastTime, lastDistance, lastPace, isRunning))

        return START_STICKY
    }

    private fun createNotification(time: String, distance: String, pace: String, running: Boolean): Notification {
        val activityIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val nextAction = if (running) ACTION_PAUSE else ACTION_RESUME
        val actionIntent = Intent(this, TrackingNotificationService::class.java).apply { action = nextAction }

        val actionPI = PendingIntent.getService(
            this,
            if (running) 100 else 101,
            actionIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = getString(R.string.run_in_progress)
        val actionLabel = if (running) getString(R.string.pause) else getString(R.string.resume)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("$distance  ·  $pace  ·  $time")
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentIntent(activityIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(
                if (running) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                actionLabel,
                actionPI
            )
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        val soundUri = Settings.System.DEFAULT_NOTIFICATION_URI
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val channelName = getString(R.string.tracking_channel_name)
        val channel = NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            setSound(soundUri, audioAttributes)
        }
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        startForeground(NOTIFICATION_ID, createNotification("00:00", "0.00 km", "--:--", true))
        startLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationCallback?.let { fusedLocationClient?.removeLocationUpdates(it) }
    }

    private fun startLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateIntervalMillis(1000L)
            .build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    sendBroadcast(Intent(ACTION_LOCATION_UPDATE).apply {
                        putExtra(EXTRA_LAT, loc.latitude)
                        putExtra(EXTRA_LNG, loc.longitude)
                        putExtra(EXTRA_ACCURACY, loc.accuracy)
                        putExtra(EXTRA_SPEED, loc.speed)
                        putExtra(EXTRA_BEARING, loc.bearing)
                        putExtra(EXTRA_TIME, loc.time)
                    })
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                sendBroadcast(Intent(ACTION_LOCATION_UPDATE).apply {
                    putExtra(EXTRA_GPS_AVAILABLE, availability.isLocationAvailable)
                })
            }
        }
        try {
            fusedLocationClient?.requestLocationUpdates(
                request,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}